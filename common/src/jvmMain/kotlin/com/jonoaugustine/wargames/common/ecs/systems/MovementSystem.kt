package com.jonoaugustine.wargames.common.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.jonoaugustine.wargames.common.ecs.components.*
import com.jonoaugustine.wargames.common.math.minus
import com.jonoaugustine.wargames.common.math.normalized
import com.jonoaugustine.wargames.common.math.times

class MovementSystem : IteratingSystem(family = family {
  all(PathMovementCmpnt, PathingCmpnt, TransformCmpnt, SpriteCmpnt)
}) {

  override fun onTickEntity(entity: Entity) = entity[PathingCmpnt]
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

      val nextPoint = pathing.path!![movement.waypoint]

      val direction = (nextPoint - centerOf(transform, sprite)).normalized()
      val distance = movement.baseSpeed * deltaTime
      val travel = direction * distance

      transform.moveOrigin(travel)
      val newCenter = transform.center(sprite.size)

      if (
        newCenter.x.toInt() == nextPoint.x.toInt() &&
        newCenter.y.toInt() == nextPoint.y.toInt()
      ) {
        movement.waypoint += 1
      }
    }
}
