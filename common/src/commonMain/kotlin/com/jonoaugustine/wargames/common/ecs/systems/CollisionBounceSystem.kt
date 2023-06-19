package com.jonoaugustine.wargames.common.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.jonoaugustine.wargames.common.div
import com.jonoaugustine.wargames.common.ecs.components.CollisionCmpnt
import com.jonoaugustine.wargames.common.ecs.components.HitboxKeys
import com.jonoaugustine.wargames.common.ecs.components.SpriteCmpnt
import com.jonoaugustine.wargames.common.ecs.components.TransformCmpnt

class CollisionBounceSystem :
    IteratingSystem(family { all(TransformCmpnt, CollisionCmpnt) }) {

  override fun onTickEntity(entity: Entity) {
    val body = entity[CollisionCmpnt].hitboxes[HitboxKeys.BODY] ?: return
    val (other, collision) = body.collisions.entries.firstOrNull() ?: return

    entity[TransformCmpnt].position = collision.vector - (entity[SpriteCmpnt].size / 2)
    body.collisions -= other
    other[CollisionCmpnt].hitboxes.forEach { (_, hb) -> hb.collisions -= entity }
  }
}
