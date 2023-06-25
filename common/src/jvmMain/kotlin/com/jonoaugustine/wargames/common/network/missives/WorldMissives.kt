package com.jonoaugustine.wargames.common.network.missives

import com.jonoaugustine.wargames.common.UserID
import com.jonoaugustine.wargames.common.ecs.Snapshot
import com.jonoaugustine.wargames.common.ecs.entities.WorldUnit
import com.jonoaugustine.wargames.common.math.Vector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface WorldAction : Action
sealed interface WorldEvent : Event

@Serializable
@SerialName("world.update")
data class WorldUpdated(val snapshot: Snapshot) : WorldEvent

@Serializable
@SerialName("world.unit.spawn")
data class SpawnUnit(
  val ownerID: UserID,
  val position: Vector,
  val unitType: WorldUnit
) : WorldAction

@Serializable
@SerialName("world.unit.move")
data class MoveUnit(
  val entityID: Int,
  val position: Vector,
  val rotation: Float? = null
) : WorldAction

@Serializable
@SerialName("world.unit.path")
data class SetUnitPath(val entityID: Int, val path: List<Vector>) : WorldAction

@Serializable
@SerialName("world.unit.destination")
data class SetUnitDestination(val entityID: Int, val destination: Vector) : WorldAction
