package com.jonoaugustine.wargames.server.managers

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.ecs.*
import com.jonoaugustine.wargames.common.ecs.components.MapCmpnt
import com.jonoaugustine.wargames.common.ecs.components.PlayerCmpnt
import com.jonoaugustine.wargames.common.ecs.entities.addBattleUnitOf
import com.jonoaugustine.wargames.common.ecs.systems.CollisionSystem
import com.jonoaugustine.wargames.common.ecs.systems.MovementSystem
import com.jonoaugustine.wargames.common.ecs.systems.PathingSystem
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.server.ecs.ServerReplicationSystem
import com.jonoaugustine.wargames.server.ecs.actions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val logger = LoggerFactory.getLogger("World Manager")

@OptIn(DelicateCoroutinesApi::class)
private val threadContext = newFixedThreadPoolContext(4, "world.loops")
private val worldScope = CoroutineScope(threadContext)
private var worlds = mapOf<LobbyID, World>()
private var worldJobs = mapOf<LobbyID, Job>()
private var actionDistributors = mapOf<LobbyID, ActionDistributor>()
private val mutex = Mutex()

suspend fun <T> useDistributor(id: LobbyID, block: (ActionDistributor) -> T): Result<T> =
  mutex.withLock {
    actionDistributors[id]
      ?.let { Result.success(block(it)) }
      ?: Result.failure(Error("distributor for $id not found"))
  }

suspend infix fun Pair<ID, Action>.queueTo(lid: LobbyID): Result<Unit> =
  useDistributor(lid) { it.queue(this) }

suspend fun getWorldJob(id: LobbyID): Job? = mutex.withLock { worldJobs[id] }

suspend fun killWorld(id: LobbyID): Job? = mutex.withLock {
  val job = worldJobs[id]
  job?.cancel()
  worlds -= id
  return job
}

suspend fun getWorld(id: LobbyID): World? = mutex.withLock { worlds[id] }

suspend fun startWorld(lobby: Lobby): World =
  serverWorld(lobby)
    .apply { entity { it += MapCmpnt(WgColor.Grass) } }
    .apply { gameStateContainer(GameState.PLANNING, lobby.mapSize) }
    .apply { mutex.withLock { actionDistributors += lobby.id to addDistributor() } }
    .apply { addPlayerEntities(lobby.players.values) }
    .also { mutex.withLock { worlds += lobby.id to it } }
    .apply { mutex.withLock { worldJobs += lobby.id to launchLoop(lobby.id) } }
    // TODO remove test unit
    .apply {
      addBattleUnitOf(
        lobby.players.values.first().id,
        Vector(200f, 200f),
        25f,
        WgSize(100, 75),
        WgColor.Red
      )
      addBattleUnitOf(
        lobby.players.values.first().id,
        Vector(300f, 300f),
        25f,
        WgSize(100, 75),
        WgColor.Blue
      )
    }

fun serverWorld(lobby: Lobby): World =
  configureWorld(1000) {
    injectables {
      add(worldScope)
      add(logger)
      add("lobby.id", lobby.id)
    }
    systems {
      add(ServerReplicationSystem())
      add(CollisionSystem())
      add(PathingSystem())
      add(MovementSystem())
    }
  }

fun World.addPlayerEntities(players: Collection<Player>): Unit =
  players.forEach { p -> entity { it += PlayerCmpnt(p.id, p.user.name, p.color) } }

fun World.addDistributor(): ActionDistributor =
  actionDistributorOf(this) {
    SpawnUnitHandler()
    MoveUnitHandler()
    UnitDestinationHandler()
  }

suspend fun World.launchLoop(lid: LobbyID) =
  worldScope.launch {
    logger.info("Starting World: $lid")
    while (gameState!!.notDone) {
      val startTime = currentTimeMillis()
      useDistributor(lid) { it.distribute() }
      update(updateInterval.toFloat())
      currentTimeMillis()
        .minus(startTime)
        .let { updateInterval.seconds - it.milliseconds }
        .inWholeMilliseconds
        .also { if (it < 0) logger.debug("too long {}", it) }
        .also { delay(max(0, it)) }
    }
    logger.debug("Disposing World: $lid")
    dispose()
  }
