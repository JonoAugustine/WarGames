package com.jonoaugustine.wargames.common.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.World.Companion.family
import com.jonoaugustine.wargames.common.ecs.components.*
import com.jonoaugustine.wargames.common.ecs.components.HitboxKeys.BODY
import com.jonoaugustine.wargames.common.ecs.components.HitboxKeys.FRONT
import com.jonoaugustine.wargames.common.ecs.components.HitboxKeys.VISION
import com.jonoaugustine.wargames.common.math.Polygon
import com.jonoaugustine.wargames.common.math.collisionWith

class CollisionSystem : IteratingSystem(family { all(TransformCmpnt, CollisionCmpnt) }) {

  override fun onTickEntity(entity: Entity) {
    val others = family.entities.filterNot { it == entity }

    val origin = entity[TransformCmpnt].position
    val rotation = entity[TransformCmpnt].rotation
    val collision = entity[CollisionCmpnt]

    collision.hitboxes.values.forEach { hitboxA ->
      val polygonA = hitboxA.asPolygon(origin, rotation)
      hitboxA.collisions = emptyMap()

      others.forEach { entityB ->
        val transB = entityB[TransformCmpnt]
        hitboxA.collisions += entityB[CollisionCmpnt].hitboxes
          .filterNot { it.key == VISION }
          .mapValues { it.value.asPolygon(transB.position, transB.rotation) }
          .mapValues { polygonA collisionWith it.value }
          .filterNot { it.value == null }
          .map { Collision(entityB, it.key, it.value!!) }
          .associateBy { entityB }
      }
    }

    if (collision[BODY].colliding || collision[FRONT].colliding) {
      entity.configure { it -= PathingCmpnt }
    }
  }
}

fun World.checkCollisions(
  polygonA: Polygon,
  vararg ignore: Int = intArrayOf()
): Map<Entity, Collision> =
  family { all(CollisionCmpnt, TransformCmpnt) }
    .entities
    .filterNot { ignore.contains(it.id) }
    .map { entity ->
      val transB = entity[TransformCmpnt]
      entity[CollisionCmpnt].hitboxes
        .mapValues { it.value.asPolygon(transB.position, transB.rotation) }
        .mapValues { polygonA collisionWith it.value }
        .filterNot { it.value == null }
        .map { Collision(entity, it.key, it.value!!) }
    }
    .flatten()
    .associateBy { it.entity }

