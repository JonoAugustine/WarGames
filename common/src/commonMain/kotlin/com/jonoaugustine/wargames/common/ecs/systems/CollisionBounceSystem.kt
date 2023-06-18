package com.jonoaugustine.wargames.common.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.jonoaugustine.wargames.common.ecs.components.CollisionCmpnt
import com.jonoaugustine.wargames.common.ecs.components.HitboxKeys
import com.jonoaugustine.wargames.common.ecs.components.TransformCmpnt

class CollisionBounceSystem :
    IteratingSystem(family { all(TransformCmpnt, CollisionCmpnt) }) {

  override fun onTickEntity(entity: Entity) {
    val origin = entity[TransformCmpnt].position
    val rotation = entity[TransformCmpnt].rotation
    val size = entity[TransformCmpnt].rotation

    entity[CollisionCmpnt]
      .hitboxes[HitboxKeys.BODY]
      ?.collisions
      ?.firstOrNull()
      ?.let { entity[TransformCmpnt].position = it.vector }
  }
}
