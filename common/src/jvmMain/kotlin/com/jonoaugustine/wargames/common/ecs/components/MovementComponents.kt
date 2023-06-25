package com.jonoaugustine.wargames.common.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.jonoaugustine.wargames.common.ecs.Replicated
import com.jonoaugustine.wargames.common.math.Vector
import kotlinx.serialization.Serializable

sealed interface MovementComponent<T> : Component<T>, Replicated

/**
 * @property recalc whether the path needs to be recalculated
 */
@Serializable
data class PathingCmpnt(
  var destination: Vector,
  var path: List<Vector>? = null,
  var recalc: Boolean = true,
) : MovementComponent<PathingCmpnt>, Replicated {

  override fun type() = PathingCmpnt

  companion object : ComponentType<PathingCmpnt>()
}

@Serializable
data class PathMovementCmpnt(val baseSpeed: Float) :
    MovementComponent<PathMovementCmpnt>, Replicated {

  var waypoint: Int = 0

  fun distanceOf(delta: Float): Float = delta * baseSpeed

  override fun type() = PathMovementCmpnt

  companion object : ComponentType<PathMovementCmpnt>()
}
