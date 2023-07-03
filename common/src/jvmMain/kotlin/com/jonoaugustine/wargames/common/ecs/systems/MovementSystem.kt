package com.jonoaugustine.wargames.common.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.jonoaugustine.wargames.common.ecs.components.*
import com.jonoaugustine.wargames.common.math.*

class MovementSystem : IteratingSystem(family = family {
  all(PathMovementCmpnt, PathingCmpnt, TransformCmpnt, SpriteCmpnt)
}) {

  override fun onTickEntity(entity: Entity): Unit {
    entity[PathingCmpnt]
      .takeUnless { it.path == null }
      .let { it ?: return }
      .let { pathing ->
        val transform = entity[TransformCmpnt]
        val movement = entity[PathMovementCmpnt]
        val sprite = entity[SpriteCmpnt]

        // if no path, skip
        when {
          pathing.path!!.isEmpty()                 -> {
            movement.waypoint = 0
            return
          }

          movement.waypoint >= pathing.path!!.size -> {
            pathing.path = null
            movement.waypoint = 0
            return
          }
        }

        val center = transform.center(sprite.size)
        pathing.path!![movement.waypoint]
          .takeUnless { it approx center }
          ?.let { next ->
            val direction = (next - center).normalized()
            val distance = movement.baseSpeed * deltaTime
            val travel = direction * distance

            transform.rotation = cos(center, centerOfFront(transform, sprite), next)
            transform.moveOrigin(travel)
          }
          ?: run { movement.waypoint += 1 }
      }
  }
}
