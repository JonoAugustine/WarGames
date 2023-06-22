package com.jonoaugustine.wargames.common.math

import com.jonoaugustine.wargames.common.WgSize
import kotlinx.serialization.Serializable
import kotlin.math.sqrt

/**
 * Represents a 2D vector with x and y components and an optional magnitude.
 *
 * @property x The x component of the vector.
 * @property y The y component of the vector.
 * @property magnitude The magnitude (length) of the vector.
 */
@Serializable
data class Vector(
  val x: Float = 0f,
  val y: Float = 0f,
  val magnitude: Float = 0f
) {

  constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())

  override fun toString(): String = "($x, $y, ${magnitude}u)"

  /**
   * Represents a zero vector with all components set to zero.
   */
  companion object {

    val ZERO = Vector()
  }
}

operator fun Vector.minus(size: WgSize): Vector =
  this - Vector(size.width.toFloat(), size.height.toFloat())

/**
 * Subtracts another vector from this vector.
 *
 * @param vector The vector to subtract.
 * @return The result of the vector subtraction.
 */
operator fun Vector.minus(vector: Vector): Vector = this + vector * -1f

operator fun Vector.plus(size: WgSize): Vector =
  this + Vector(size.width.toFloat(), size.height.toFloat())

/**
 * Calculates the dot product between this vector and another vector.
 *
 * @param vector The other vector to calculate the dot product with.
 * @return The dot product of the two vectors.
 */
operator fun Vector.times(vector: Vector): Float = this dot vector

/**
 * Performs scalar multiplication on the vector.
 *
 * @param scale The scalar value to multiply the vector by.
 * @return The result of the scalar multiplication.
 */
operator fun Vector.times(scale: Float): Vector = Vector(this.x * scale, this.y * scale)

/**
 * Adds another vector to this vector.
 *
 * @param vector The vector to add.
 * @return The result of the vector addition.
 */
operator fun Vector.plus(vector: Vector): Vector =
  Vector(this.x + vector.x, this.y + vector.y)

/**
 * Calculates the dot (scalar) product of two vectors.
 *
 * @param vector2 The other vector to calculate the dot product with.
 * @return The dot product of the two vectors.
 */
infix fun Vector.dot(vector2: Vector): Float = this.x * vector2.x + this.y * vector2.y

/**
 * Represents an array of vectors as the edges of a polygon.
 */
val Array<Vector>.edges: Array<Vector>
  get() = Array(size) { index -> edgeVector(get(index), get((index + 1) % size)) }

/**
 * Calculates the orthogonal vector of this vector.
 *
 * @return The orthogonal vector of this vector.
 */
val Vector.orthogonal: Vector get() = Vector(y, -x)

/**
 * Calculates the vector going from point1 to point2.
 *
 * @param point1 The starting point of the vector.
 * @param point2 The ending point of the vector.
 * @return The vector going from point1 to point2.
 */
fun edgeVector(point1: Vector, point2: Vector): Vector =
  Vector(point2.x - point1.x, point2.y - point1.y)

/**
 * Projects an array of vectors onto a given axis.
 *
 * @param axis The axis to project the vectors onto.
 * @return A vector representing the range of projections along the axis.
 */
infix fun Array<Vector>.project(axis: Vector): Vector =
  fold(Vector(Float.MAX_VALUE, Float.MIN_VALUE)) { acc, point ->
    val projection = point dot axis
    Vector(minOf(acc.x, projection), maxOf(acc.y, projection))
  }

/**
 * Checks if this projection overlaps with another projection.
 *
 * @param projection2 The other projection to compare with.
 * @return `true` if the projections overlap, `false` otherwise.
 */
infix fun Vector.overlaps(projection2: Vector): Boolean =
  this.x <= projection2.y && projection2.x <= this.y

/**
 * Checks if this projection does not overlap with another projection.
 *
 * @param projection2 The other projection to compare with.
 * @return `true` if the projections do not overlap, `false` otherwise.
 */
infix fun Vector.noOverlap(projection2: Vector): Boolean = !(this overlaps projection2)

/** Calculates the Euclidean distance between this vector and another vector */
fun Vector.distanceTo(other: Vector): Float =
  sqrt((other.x - x) * (other.x - x) + (other.y - y) * (other.y - y))
