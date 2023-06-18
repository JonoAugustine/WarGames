package com.jonoaugustine.wargames.common.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.jonoaugustine.wargames.common.ecs.components.Collision
import com.jonoaugustine.wargames.common.ecs.components.CollisionCmpnt
import com.jonoaugustine.wargames.common.ecs.components.TransformCmpnt
import com.jonoaugustine.wargames.common.ecs.components.polygonWith
import com.jonoaugustine.wargames.common.math.collisionWith

class CollisionSystem : IteratingSystem(family { all(TransformCmpnt, CollisionCmpnt) }) {

  override fun onTickEntity(entity: Entity) {
    val others = family.entities.filterNot { it == entity }

    val origin = entity[TransformCmpnt].position
    val rotation = entity[TransformCmpnt].rotation
    val collision = entity[CollisionCmpnt]

    collision.hitboxes.values.forEach { hitboxA ->
      val polygonA = hitboxA.polygonWith(origin, rotation)
      hitboxA.collisions = emptyList()

      others.forEach { entityB ->
        val transB = entityB[TransformCmpnt]
        hitboxA.collisions += entityB[CollisionCmpnt].hitboxes
          .mapValues { it.value.polygonWith(transB.position, transB.rotation) }
          .mapValues { polygonA collisionWith it.value }
          .filterNot { it.value == null }
          .map { Collision(entityB, it.key, it.value!!) }
      }
    }
  }
}
