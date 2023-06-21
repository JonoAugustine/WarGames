/***
 * Credit: https://github.com/JoelEager/Kotlin-Collision-Detector
 ***/
package com.jonoaugustine.wargames.common.math

import com.jonoaugustine.wargames.common.WgSize
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

typealias CollisionVector = Vector
typealias Polygon = Array<Vector>

infix fun Polygon.collisionWith(poly2: Polygon): CollisionVector? =
  hasCollided(this, poly2)

/**
 * Returns Vector of to the nearest non-collision position of polygon A
 *
 * @param poly1, poly2 The two polygons described as arrays of points as Vectors
 *  Note: The points list must go in sequence around the polygon
 * @param maxDist The maximum distance between any two points of any two polygons
 *  that can be touching. If this null then the optimization check that uses it
 *  will be skipped. By providing a maxDist value, you can control the sensitivity
 *  of the collision detection. Smaller values will result in stricter collision
 *  detection, considering only collisions that occur within a close range,
 *  while larger values will allow for looser collision detection, considering
 *  collisions that occur within a wider range.
 */
fun hasCollided(
  poly1: Polygon,
  poly2: Polygon,
  maxDist: Double? = null
): CollisionVector? =
  // No maxDist so run SAT on the polys
  if (maxDist == null || canCollide(poly1, poly2, maxDist)) runSAT(poly1, poly2)
  else null

private fun canCollide(
  poly1: Polygon,
  poly2: Polygon,
  maxDist: Double
): Boolean = (poly1[1].x - poly2[0].x).pow(2) +
    (poly1[1].y - poly2[0].y).pow(2) <= maxDist.pow(2)

fun runSAT(a: Polygon, b: Polygon): CollisionVector? {
  val edges = (a.edges + b.edges)
  val axes = Array(edges.size) { index -> edges[index].orthogonal }
  var collisionVector = Vector.ZERO

  // Variables to store the nearest position where polygons do not collide
  var nearestPosition: Vector? = null
  var nearestDistance = Float.MAX_VALUE

  // Iterate over the axes
  for (axis in axes) {
    // Project the polygons onto the current axis
    val minMaxA = a project axis
    val minMaxB = b project axis

    // Check if there is no overlap on the axis, indicating no collision
    if (minMaxA noOverlap minMaxB) return null // No collision, return null

    // Calculate the minimum distance of separation on the axis
    val distMin = minMaxB.y - minMaxA.x

    // Update the collision vector if the separation is larger
    if (distMin.absoluteValue > collisionVector.magnitude.absoluteValue) {
      collisionVector = Vector(axis.x, axis.y, distMin)

      // Calculate the nearest position where polygons do not collide
      val centerA = a.center() // Calculate the center point of polygon A
      val distanceToCenter = centerA dot axis // Distance from centerA to the axis
      val distanceToTopLeft =
        a[0] dot axis // Distance from top-left vertex of A to the axis

      // Determine the nearest position as the center or top-left vertex, based on distances
      val nearest = if (distanceToCenter < distanceToTopLeft) centerA else a[0]

      // Update nearest position and distance if the current distance is smaller
      val distance = distMin.absoluteValue
      if (distance < nearestDistance) {
        nearestPosition = nearest
        nearestDistance = distance
      }
    }
  }

  // Return the nearest position if found, or fallback to the collision vector
  return nearestPosition ?: collisionVector
}

// Calculates the center point of a polygon
fun Polygon.center(): Vector {
  val sumX = this.sumOf { it.x.toDouble() }
  val sumY = this.sumOf { it.y.toDouble() }
  val centerX = (sumX / size).toFloat()
  val centerY = (sumY / size).toFloat()
  return Vector(centerX, centerY)
}

fun rectangleFrom(position: Vector, size: WgSize): Polygon =
  arrayOf(
    position,
    position.copy(x = position.x + size.width),
    Vector(position.x + size.width, position.y + size.height),
    position.copy(y = position.y + size.height)
  )

/**
 * https://math.stackexchange.com/questions/1917449/rotate-polygon-around-center-and-get-the-coordinates
 */
fun Polygon.toRotated(degrees: Float): Polygon {
  val theta = (degrees * (Math.PI / 180)).toFloat()
  val vectorMatrix: FloatMatrix =
    arrayOf(map { it.x }.toTypedArray(), map { it.y }.toTypedArray())
  val centroid: FloatMatrix = reduce { acc, vector -> acc + vector }
    .let { it * (1f / size) }
    .let { arrayOf(arrayOf(it.x), arrayOf(it.y)) }
  val C: FloatMatrix = Array(2) { r -> Array(size) { centroid[r][0] } }
  val R: FloatMatrix = arrayOf(
    arrayOf(cos(theta), -sin(theta)),
    arrayOf(sin(theta), cos(theta))
  )
  /*
  [
    [x, x, x, x]
    [y, y, y, y]
  ]
   */
  val result = R dot (vectorMatrix matrixSubtract C) matrixAdd C
  return result.first()
    .mapIndexed { col, x -> Vector(x, result.last()[col]) }
    .toTypedArray()
}

fun Polygon.contains(vector: Vector): Boolean {
  var isInPolygon = false
  val polygonSize = this.size

  var j = polygonSize - 1
  for (i in 0 until polygonSize) {
    if (
      (this[i].y < vector.y && this[j].y >= vector.y ||
          this[j].y < vector.y && this[i].y >= vector.y) &&
      (this[i].x <= vector.x || this[j].x <= vector.x)
    ) {
      if (this[i].x + (vector.y - this[i].y) / (this[j].y - this[i].y) * (this[j].x - this[i].x) < vector.x) {
        isInPolygon = !isInPolygon
      }
    }
    j = i
  }

  return isInPolygon
}
