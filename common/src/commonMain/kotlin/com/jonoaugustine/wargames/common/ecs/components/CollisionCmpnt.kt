package com.jonoaugustine.wargames.common.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.ecs.Replicated
import com.jonoaugustine.wargames.common.math.*
import kotlinx.serialization.Serializable

object HitboxKeys {

  const val FRONT = "FRONT"
  const val BODY = "BODY"
  const val REAR = "REAR"
}

@Serializable
data class Collision(
  val entity: Entity,
  val hitboxKey: String,
  val vector: CollisionVector
)

/**
 * @property offset Origin offset from the parent origin position
 * @property collisions List of colliding entities and the related [CollisionVector]
 */
@Serializable
data class Hitbox(
  val size: WgSize,
  val offset: Vector = Vector.ZERO,
  var collisions: Map<Entity, Collision> = emptyMap()
)

fun Hitbox.polygonWith(origin: Vector, rotation: Float): Polygon =
  rectangleFrom(origin + offset, size).toRotated(rotation)

@Serializable
data class CollisionCmpnt(
  var hitboxes: Map<String, Hitbox> = emptyMap()
) : Component<CollisionCmpnt>, Replicated {

  /** Whether any hitbox has a collision */
  val colliding: Boolean get() = hitboxes.values.any { it.collisions.isNotEmpty() }
  val notColliding: Boolean get() = !colliding

  fun clear() = hitboxes.forEach { it.value.collisions = emptyMap() }

  override fun type() = CollisionCmpnt

  companion object : ComponentType<CollisionCmpnt>()
}
