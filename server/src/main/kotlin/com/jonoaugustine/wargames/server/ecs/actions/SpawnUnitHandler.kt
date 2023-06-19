package com.jonoaugustine.wargames.server.ecs.actions

import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.ecs.GameStateContainer
import com.jonoaugustine.wargames.common.ecs.components.PlayerCmpnt
import com.jonoaugustine.wargames.common.ecs.entities.CombatUnit
import com.jonoaugustine.wargames.common.ecs.entities.addBattleUnitOf
import com.jonoaugustine.wargames.common.ecs.systems.checkCollisions
import com.jonoaugustine.wargames.common.math.rectangleFrom
import com.jonoaugustine.wargames.common.network.missives.SpawnUnit

fun ActionDistributorConfiguration.SpawnUnitHandler() = add<SpawnUnit> { act, uid ->
  val gs = world.inject<GameStateContainer>()
  // TODO gamestate restrict spawning if (gs.state !== PLACING) return@add
  val player = world.family { all(PlayerCmpnt) }
    .entities.firstOrNull { it[PlayerCmpnt].id == uid }
    ?: return@add

  when (act.unitType) {
    is CombatUnit -> {
      val size = WgSize(50, 25)
      val collisions = checkCollisions(rectangleFrom(act.position, size))
      if (collisions.isEmpty())
        world.addBattleUnitOf(
          uid,
          act.position,
          10f,
          size,
          player[PlayerCmpnt].color
        )
    }
  }
}
