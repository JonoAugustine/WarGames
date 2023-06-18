package com.jonoaugustine.wargames.server.ecs.actions

import com.github.quillraven.fleks.World
import com.jonoaugustine.wargames.common.ID
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.ecs.GameStateContainer
import com.jonoaugustine.wargames.common.ecs.components.CollisionCmpnt
import com.jonoaugustine.wargames.common.ecs.components.PlayerCmpnt
import com.jonoaugustine.wargames.common.ecs.entities.CombatUnit
import com.jonoaugustine.wargames.common.ecs.entities.addBattleUnitOf
import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.common.network.missives.MoveUnit
import com.jonoaugustine.wargames.common.network.missives.SpawnUnit

class ActionDistributor internal constructor(
  private val world: World,
  private val handlers: List<context(World)(Action, ID) -> Unit>
) {

  private val queue = mutableListOf<Pair<ID, Action>>()

  fun <T : Action> queue(ownedAction: Pair<ID, T>): Unit {
    queue += ownedAction
  }

  fun distribute(): Unit {
    queue.forEach { (owner, action) ->
      handlers.forEach {
        it(world, action, owner)
      }
    }
    queue.clear()
  }
}

class ActionDistributorConfiguration internal constructor(val world: World) {

  val handlers = mutableListOf<context(World)(Action, ID) -> Unit>()

  inline fun <reified T : Action> add(noinline handler: context(World)(T, ID) -> Unit) {
    handlers.add { action: Action, owner: ID ->
      if (action is T) handler(world, action, owner)
    }
  }
}

fun actionDistributorOf(
  world: World, cfg: ActionDistributorConfiguration.() -> Unit
): ActionDistributor = ActionDistributor(
  world, ActionDistributorConfiguration(world).apply(cfg).handlers.toList()
)

fun ActionDistributorConfiguration.SpawnUnitHandler() = add<SpawnUnit> { act, uid ->
  val gs = world.inject<GameStateContainer>()
  // TODO gamestate restrict spawning if (gs.state !== PLACING) return@add
  val player = world.family { all(PlayerCmpnt) }
    .entities.firstOrNull { it[PlayerCmpnt].id == uid }
    ?: return@add

  when (act.unitType) {
    is CombatUnit -> world.addBattleUnitOf(
      uid,
      act.position,
      10f,
      WgSize(50, 25),
      player[PlayerCmpnt].color
    )
  }
}

fun ActionDistributorConfiguration.MoveUnitHandler() = add<MoveUnit> { act, uid ->
  val player = world.family { all(PlayerCmpnt) }
    .entities.firstOrNull { it[PlayerCmpnt].id == uid }
    ?: return@add
  val unit = world.asEntityBag()[act.entityID]
  if (unit[CollisionCmpnt].colliding) {
    TODO("handle collision movement")
  }
}
