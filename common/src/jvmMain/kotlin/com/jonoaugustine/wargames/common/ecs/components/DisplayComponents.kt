package com.jonoaugustine.wargames.common.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.ecs.Replicated
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.math.distanceTo
import com.jonoaugustine.wargames.common.math.plus
import kotlinx.serialization.Serializable
import kotlin.math.cos

@Serializable
data class TransformCmpnt(var position: Vector, var rotation: Float = 0f) :
    Component<TransformCmpnt>, Replicated {

  fun moveOrigin(vector: Vector) {
    this.position = this.position + vector
  }

  fun center(size: WgSize): Vector =
    Vector(position.x + size.width / 2, position.y + size.height / 2)

  override fun type() = TransformCmpnt

  companion object : ComponentType<TransformCmpnt>()
}

val TransformCmpnt.x get() = position.x
val TransformCmpnt.y get() = position.y

@Serializable
data class SpriteCmpnt(val size: WgSize, val color: WgColor) :
    Component<SpriteCmpnt>, Replicated {

  override fun type() = SpriteCmpnt

  companion object : ComponentType<SpriteCmpnt>()
}

infix fun Vector.centerOn(size: WgSize): Vector =
  Vector(x - size.width / 2, y - size.height / 2)

fun centerOf(transform: TransformCmpnt, sprite: SpriteCmpnt): Vector =
  Vector(
    transform.position.x + sprite.size.width / 2,
    transform.position.y + sprite.size.height / 2
  )

/**
 * Returns X
 * ```
 * o----x----o
 * |         |
 * 0---------o
 * ```
 */
fun centerOfFront(transform: TransformCmpnt, sprite: SpriteCmpnt): Vector =
  centerOf(transform, sprite)
    .let { it + cos(45f) * it.distanceTo(transform.position) }
