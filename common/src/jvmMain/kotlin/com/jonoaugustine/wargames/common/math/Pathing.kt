package com.jonoaugustine.wargames.common.math

import com.jonoaugustine.wargames.common.WgSize
import java.util.*
import kotlin.math.abs

data class Segment(val start: Vector, val end: Vector)

/**
 * Represents a node in the pathfinding grid
 * @property position The position of the node in the grid
 * @property parent The parent node from which this node is reached
 * @property gScore The cost from the start node to this node
 * @property hScore The heuristic (estimated) cost from this node to the goal node
 */
data class Node(
  val position: Vector,
  var parent: Node? = null,
  var gScore: Float = 0f,
  var hScore: Float = 0f
) {

  override fun equals(other: Any?): Boolean = other is Node && other.position == position
  override fun hashCode(): Int = (1000 * (position.x + position.y)).toInt()
}

/** The total cost of this node (gScore + hScore) */
val Node.fScore: Float get() = gScore + hScore

/**
 * Finds the shortest path using the A* algorithm
 * @param start The start position of the path
 * @param goal The goal position of the path
 * @param obstacles The list of obstacle positions
 * @return The list of positions representing the shortest path, or an empty list if no path is found
 */
fun findShortestPath(
  start: Vector,
  goal: Vector,
  margin: Int,
  obstacles: Collection<Vector>,
  xBound: ClosedFloatingPointRange<Float>,
  yBound: ClosedFloatingPointRange<Float>,
): List<Vector> {
  // Create the start and goal nodes
  val startNode = Node(start)
  val goalNode = Node(goal)
  val marginGoal = rectangleFrom(goal, WgSize(margin, margin))

  /** Unevaluated nodes */
  val frontierQueue = PriorityQueue<Node>(compareBy { it.fScore })

  /** Unevaluated nodes */
  val frontierSet = mutableSetOf<Node>()
  val visited = mutableSetOf<Node>()

  // Add the start node to the open set
  frontierQueue.add(startNode)

  while (frontierQueue.isNotEmpty()) {
    // Find the node with the lowest fScore in the open set
    val current = frontierQueue.poll() ?: break
    frontierSet.remove(current)

    // Move the current node from the open set to the closed set
    visited.add(current)

    // If the current node is the goal node
    // reconstruct the path and return it
    if (
      current.position == goalNode.position ||
      marginGoal.contains(current.position)
    ) return reconstructPath(current)

    // Generate the neighbor nodes
    generateNeighbors(current, margin, obstacles, xBound, yBound)
      // Skip already evaluated nodes
      .filterNot { it in visited }
      .forEach { nbr ->
        val tentativeGScore =
          current.gScore + current.position.distanceTo(nbr.position)

        if (nbr !in frontierQueue || tentativeGScore < nbr.gScore) {
          // Update the neighbor node with the new gScore and hScore
          nbr.gScore = tentativeGScore
          nbr.hScore = nbr.position.distanceTo(goalNode.position)

          // Add the neighbor node to the open set
          if (nbr !in frontierSet) {
            frontierQueue.add(nbr)
            frontierSet.add(nbr)
          }
        }
      }
  }

  // No path found, return an empty list
  return emptyList()
}

fun heuristic(node: Node) {
}

/**
 * Generates the neighboring nodes for a given node
 * @param node The current node
 * @param obstacles The list of obstacle positions
 * @return The list of neighbor nodes
 */
fun generateNeighbors(
  node: Node,
  margin: Int,
  obstacles: Collection<Vector>,
  xRange: ClosedFloatingPointRange<Float>,
  yRange: ClosedFloatingPointRange<Float>,
): List<Node> =
  listOf(
    // top row
    -abs(margin - 1f) to -abs(margin - 1f),
    0f to -abs(margin - 1f),
    abs(margin - 1f) to -abs(margin - 1f),
    // center row
    -abs(margin - 1f) to 0f,
    abs(margin - 1f) to 0f,
    // bottom row
    -abs(margin - 1f) to abs(margin - 1f),
    0f to abs(margin - 1f),
    abs(margin - 1f) to abs(margin - 1f),
  )
    .map { Vector(node.position.x + it.first, node.position.y + it.second) }
    // Check if the neighbor position is valid and not blocked by an obstacle
    .filter { isValidPosition(it, xRange, yRange) && !obstacles.contains(it) }
    .map { Node(it, node) }

/**
 * Checks if a given position is valid within the grid bounds
 * @param position The position to check
 * @return True if the position is valid, false otherwise
 */
fun isValidPosition(
  position: Vector,
  xRange: ClosedFloatingPointRange<Float>,
  yRange: ClosedFloatingPointRange<Float>,
): Boolean = position.x in xRange && position.y in yRange

/**
 * Reconstructs the path from the goal node to the start node
 * @param goalNode The goal node
 * @return The list of positions representing the reconstructed path
 */
fun reconstructPath(goalNode: Node): List<Vector> {
  val path = mutableListOf<Vector>()
  var current: Node? = goalNode

  while (current != null) {
    path.add(0, current.position)
    current = current.parent
  }

  return path
}

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
