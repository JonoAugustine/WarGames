package com.jonoaugustine.wargames.common.entities

import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.math.Rectangle
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.math.overlaps

typealias EntityID = String

sealed interface Entity {

  val id: EntityID
  val position: Vector
  val size: WgSize
  val rotation: Float
  val collisionBox: Rectangle

  fun copy()
  fun update(delta: Float, match: Match): Entity
  fun collidesWith(other: Entity): Boolean =
    this.collisionBox.overlaps(other.collisionBox)
}

/** Coordinate [Vector] of the center point of this [Entity] */
val Entity.center
  get() = Vector(position.x + size.width / 2, position.y + size.height / 2)

