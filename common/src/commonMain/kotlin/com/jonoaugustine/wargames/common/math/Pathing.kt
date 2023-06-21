package com.jonoaugustine.wargames.common.math

data class Segment(val start: Vector, val end: Vector)

/**
 * Generates a list of obstacle vectors using the scanline algorithm on a collection of polygons.
 *
 * @param polygons The collection of polygons.
 * @return A list of obstacle vectors representing the edges of the polygons.
 */
fun generateObstacles(polygons: Collection<Polygon>): List<Vector> {
  val obstacles = mutableListOf<Vector>()

  for (polygon in polygons) {
    val edges = getPolygonEdges(polygon)
    val maxY = getMaxY(polygon)

    val intersections = mutableMapOf<Int, MutableList<Float>>()

    for (y in 0 until maxY) {
      for (edge in edges) {
        val p1 = edge.start
        val p2 = edge.end

        if ((p1.y <= y && p2.y > y) || (p2.y <= y && p1.y > y)) {
          val xIntersection = calculateXIntersection(p1, p2, y)
          intersections.getOrPut(y) { mutableListOf() }.add(xIntersection)
        }
      }
    }

    for ((y, intersectionList) in intersections) {
      intersectionList.sort()
      obstacles.addAll(getObstacleVectors(intersectionList, y))
    }
  }

  return obstacles
}

/**
 * Retrieves the edges of a polygon by connecting consecutive vertices.
 *
 * @param polygon The polygon represented as an array of vertices.
 * @return A list of line segments representing the edges of the polygon.
 */
fun getPolygonEdges(polygon: Polygon): List<Segment> {
  val edges = mutableListOf<Segment>()

  for (i in polygon.indices) {
    val j = (i + 1) % polygon.size
    edges.add(Segment(polygon[i], polygon[j]))
  }

  return edges
}

/**
 * Calculates the maximum Y-coordinate among all vertices in the polygon.
 *
 * @param polygon The polygon represented as an array of vertices.
 * @return The maximum Y-coordinate.
 */
fun getMaxY(polygon: Polygon): Int {
  var maxY = Int.MIN_VALUE

  for (vector in polygon) {
    maxY = maxOf(maxY, vector.y.toInt())
  }

  return maxY
}

/**
 * Calculates the intersection point of a line segment with a given Y-coordinate.
 *
 * @param p1 The start point of the line segment.
 * @param p2 The end point of the line segment.
 * @param y The Y-coordinate of the intersection point.
 * @return The X-coordinate of the intersection point.
 */
fun calculateXIntersection(p1: Vector, p2: Vector, y: Int): Float {
  val t = (y - p1.y) / (p2.y - p1.y)
  return p1.x + t * (p2.x - p1.x)
}

/**
 * Generates a list of obstacle vectors from the sorted X-intersection points and the current Y-coordinate.
 *
 * @param intersections The sorted list of X-intersection points.
 * @param y The current Y-coordinate.
 * @return A list of obstacle vectors representing the line segments between the X-intersection points.
 */
fun getObstacleVectors(intersections: List<Float>, y: Int): List<Vector> {
  val obstacleVectors = mutableListOf<Vector>()

  for (i in 0 until intersections.size step 2) {
    val xStart = intersections[i]
    val xEnd = intersections[i + 1]
    obstacleVectors.add(Vector(xStart, y.toFloat()))
    obstacleVectors.add(Vector(xEnd, y.toFloat()))
  }

  return obstacleVectors
}
