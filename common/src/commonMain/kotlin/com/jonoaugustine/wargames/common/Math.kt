package com.jonoaugustine.wargames.common

data class Vector(val x: Float, val y: Float) {

  operator fun times(scale: Float): Vector {
    return Vector(this.x * scale, this.y * scale)
  }

  operator fun plus(vector: Vector): Vector {
    return Vector(this.x + vector.x, this.y + vector.y)
  }

  operator fun minus(vector: Vector): Vector {
    return this + vector * -1f
  }
}

data class Size(val width: Int, val height: Int)

data class Rectangle(val origin: Vector, val size: Size)

val Rectangle.minPos
  get() = Vector(this.origin.x, this.origin.y + this.size.height)
val Rectangle.maxPos
  get() = Vector(this.origin.x + this.size.width, this.origin.y)

fun Rectangle.overlaps(other: Rectangle): Boolean =
  this.origin.x < other.maxPos.x &&
      this.maxPos.x > other.origin.x &&
      this.origin.y < other.minPos.y &&
      this.minPos.y > other.origin.y
