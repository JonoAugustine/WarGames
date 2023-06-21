package com.jonoaugustine.wargames.server.ecs.actions

import com.github.quillraven.fleks.World
import com.jonoaugustine.wargames.common.ID
import com.jonoaugustine.wargames.common.ecs.components.*
import com.jonoaugustine.wargames.common.ecs.systems.checkCollisions
import com.jonoaugustine.wargames.common.math.rectangleFrom
import com.jonoaugustine.wargames.common.math.toRotated
import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.common.network.missives.MoveUnit
import com.jonoaugustine.wargames.common.network.missives.SetUnitDestination

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

fun ActionDistributorConfiguration.MoveUnitHandler() = add<MoveUnit> { act, uid ->
  val player = world.family { all(PlayerCmpnt) }
    .entities.firstOrNull { it[PlayerCmpnt].id == uid }
    ?: return@add
  val unit = act.entityID
    .takeIf { it < world.numEntities }
    ?.let { world.asEntityBag()[it] }
    ?: return@add

  val collisionPredictions =
    rectangleFrom(act.position, unit[SpriteCmpnt].size)
      .toRotated(act.rotation ?: unit[TransformCmpnt].rotation)
      .let { checkCollisions(it, unit.id) }

  if (collisionPredictions.isNotEmpty()) return@add

  unit[TransformCmpnt].position = act.position
  act.rotation?.let { unit[TransformCmpnt].rotation = it }
}

fun ActionDistributorConfiguration.UnitDestinationHandler() =
  add<SetUnitDestination> { (entityID, dest), uid ->
    val player = world.family { all(PlayerCmpnt) }
      .entities.firstOrNull { it[PlayerCmpnt].id == uid }
      ?: return@add
    val unit = entityID
      .takeIf { it < world.numEntities }
      ?.let { world.asEntityBag()[it] }
      ?: return@add

    if (unit[OwnerCmpnt].ownerID != uid) return@add

    unit.configure { it += PathingCmpnt(dest) }
  }
