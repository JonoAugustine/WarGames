package com.jonoaugustine.wargames.common.math

import com.jonoaugustine.wargames.common.WgSize
import kotlinx.serialization.Serializable

@Serializable
data class Rectangle(val origin: Vector, val size: WgSize)

val Rectangle.minPos
  get() = Vector(this.origin.x, this.origin.y + this.size.height)
val Rectangle.maxPos
  get() = Vector(this.origin.x + this.size.width, this.origin.y)

fun Rectangle.overlaps(other: Rectangle): Boolean =
  this.origin.x < other.maxPos.x &&
      this.maxPos.x > other.origin.x &&
      this.origin.y < other.minPos.y &&
      this.minPos.y > other.origin.y
