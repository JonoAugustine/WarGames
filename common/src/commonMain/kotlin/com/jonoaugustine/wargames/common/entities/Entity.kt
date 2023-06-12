package com.jonoaugustine.wargames.common.entities

import com.jonoaugustine.wargames.common.*

typealias EntityID = String

sealed interface Entity {

  val id: EntityID
  val position: Vector
  val size: WgSize
  val rotation: Float
  val collisionBox: Rectangle
  fun update(delta: Float, match: Match): Entity
  fun collidesWith(other: Entity): Boolean =
    this.collisionBox.overlaps(other.collisionBox)
}

/** Coordinate [Vector] of the center point of this [Entity] */
val Entity.center
  get() = Vector(position.x + size.width / 2, position.y + size.height / 2)

