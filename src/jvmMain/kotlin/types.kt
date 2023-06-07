import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array

data class Vector(var x: Float, var y: Float, var magnitude: Float? = null) {

  private val mkArray: D1Array<Float> get() = mk.ndarray(mk[this.x, this.y])
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
