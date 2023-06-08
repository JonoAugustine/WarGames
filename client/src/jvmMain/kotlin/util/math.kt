package util

data class Vector(var x: Float, var y: Float, var magnitude: Float? = null) {

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
