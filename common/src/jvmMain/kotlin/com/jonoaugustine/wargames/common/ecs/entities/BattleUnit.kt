package com.jonoaugustine.wargames.common.ecs.entities

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.World
import com.jonoaugustine.wargames.common.ID
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.ecs.components.*
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.times

fun World.addUnitOf(
  owner: ID,
  position: Vector,
  size: WgSize,
  color: WgColor,
  config: EntityCreateContext.(Entity) -> Unit = {}
) = entity {
  it += OwnerCmpnt(owner)
  it += TransformCmpnt(position)
  it += SpriteCmpnt(size, color)
  config(it)
}

fun World.addBattleUnitOf(
  owner: String,
  position: Vector,
  speed: Float,
  size: WgSize,
  color: WgColor,
  config: EntityCreateContext.(Entity) -> Unit = {}
) = addUnitOf(owner, position, size, color) {
  it += PathMovementCmpnt(speed)
  it += CollisionCmpnt(
    mapOf(
      HitboxKeys.FRONT to Hitbox(size.copy(height = size.height / 10)),
      HitboxKeys.BODY to Hitbox(size),
      HitboxKeys.VISION to Hitbox(size * 2),
    )
  )
  config(it)
}

fun World.infantryOf(
  owner: String,
  position: Vector,
  speed: Float,
  size: WgSize,
  color: WgColor,
  config: EntityCreateContext.(Entity) -> Unit = {}
) = addBattleUnitOf(
  owner,
  position,
  speed,
  size,
  color
) {
  it += NameCmpnt("infantry")
  config(it)
}
