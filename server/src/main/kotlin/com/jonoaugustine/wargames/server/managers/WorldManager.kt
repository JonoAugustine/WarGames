package com.jonoaugustine.wargames.server.managers

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.ecs.GameState.STARTING
import com.jonoaugustine.wargames.common.ecs.GameStateContainer
import com.jonoaugustine.wargames.common.ecs.components.MapCmpnt
import com.jonoaugustine.wargames.common.ecs.components.PlayerCmpnt
import com.jonoaugustine.wargames.common.ecs.notDone
import com.jonoaugustine.wargames.common.ecs.systems.CollisionSystem
import com.jonoaugustine.wargames.common.ecs.updateInterval
import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.server.ecs.ServerReplicationSystem
import com.jonoaugustine.wargames.server.ecs.actions.ActionDistributor
import com.jonoaugustine.wargames.server.ecs.actions.MoveUnitHandler
import com.jonoaugustine.wargames.server.ecs.actions.SpawnUnitHandler
import com.jonoaugustine.wargames.server.ecs.actions.actionDistributorOf
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
private val threadContext = newSingleThreadContext("world.loops")
private val worldScope = CoroutineScope(threadContext)
private var worlds = mapOf<MatchID, World>()
private var worldJobs = mapOf<MatchID, Job>()
private var actionDistributorMap = mapOf<MatchID, ActionDistributor>()
private val mutex = Mutex()

suspend fun <T> useDistributor(id: MatchID, block: (ActionDistributor) -> T): Result<T> =
  mutex.withLock {
    actionDistributorMap[id]
      ?.let { Result.success(block(it)) }
      ?: Result.failure(Error("distributor for $id not found"))
  }

suspend infix fun Pair<ID, Action>.queueTo(mid: MatchID): Result<Unit> =
  useDistributor(mid) { it.queue(this) }

suspend fun getWorldJob(id: MatchID): Job? = mutex.withLock { worldJobs[id] }

suspend fun killWorld(id: MatchID): Job? = mutex.withLock {
  val job = worldJobs[id]
  job?.cancel()
  worlds -= id
  return job
}

suspend fun getWorld(id: MatchID): World? = mutex.withLock { worlds[id] }

suspend fun startWorld(match: Match): Unit = serverWorld(match.id)
  .apply {
    entity { it += MapCmpnt(WgColor.Grass) }
    match.players.values
      .forEach { p -> entity { it += PlayerCmpnt(p.id, p.user.name, p.color) } }
  }
  .also { mutex.withLock { worlds += match.id to it } }
  .also { registerActionDistributor(match.id, it) }
  .let { launchWorldLoop(match.id, it) }
  .let { mutex.withLock { worldJobs += match.id to it } }

fun serverWorld(mid: MatchID): World =
  configureWorld(1000) {
    injectables {
      add(worldScope)
      add<MatchID>(mid)
      add(GameStateContainer(STARTING))
    }
    systems {
      add(ServerReplicationSystem())
      add(CollisionSystem())
    }
  }

suspend fun registerActionDistributor(mid: MatchID, world: World): Unit =
  actionDistributorOf(world) {
    SpawnUnitHandler()
    MoveUnitHandler()
  }
    .let { mutex.withLock { actionDistributorMap += mid to it } }

suspend fun launchWorldLoop(mid: MatchID, world: World) = worldScope.launch {
  while (world.inject<GameStateContainer>().notDone) {
    useDistributor(mid) { it.distribute() }
    world.update(updateInterval.toFloat())
    delay(updateInterval.seconds)
  }
  world.dispose()
}
