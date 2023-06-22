package com.jonoaugustine.wargames.common.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.jonoaugustine.wargames.common.UserID
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.ecs.Replicated
import com.jonoaugustine.wargames.common.math.Vector
import kotlinx.serialization.Serializable

@Serializable
data class PlayerCmpnt(
  var id: UserID,
  var name: String,
  var color: WgColor,
) : Component<PlayerCmpnt>, Replicated {

  override fun type() = PlayerCmpnt

  companion object : ComponentType<PlayerCmpnt>()
}

@Serializable
data class OwnerCmpnt(var ownerID: String) : Component<OwnerCmpnt>, Replicated {

  override fun type() = OwnerCmpnt

  companion object : ComponentType<OwnerCmpnt>()
}

@JvmInline
@Serializable
value class NameCmpnt(val name: String) : Component<NameCmpnt>, Replicated {

  override fun type() = NameCmpnt

  companion object : ComponentType<NameCmpnt>()
}

@Serializable
data class MovementPathCmpnt(
  val speed: Float,
  val path: List<Vector> = emptyList()
) : Component<MovementPathCmpnt>, Replicated {

  override fun type() = MovementPathCmpnt

  companion object : ComponentType<MovementPathCmpnt>()
}

@Serializable
data class MapCmpnt(var background: WgColor) : Component<MapCmpnt>, Replicated {

  override fun type() = MapCmpnt

  companion object : ComponentType<MapCmpnt>()
}

/**
 * @property recalc whether the path needs to be recalculated
 */
@Serializable
data class PathingCmpnt(
  var destination: Vector,
  var path: List<Vector>? = null,
  var recalc: Boolean = true,
) : Component<PathingCmpnt>, Replicated {

  override fun type() = PathingCmpnt

  companion object : ComponentType<PathingCmpnt>()
}
