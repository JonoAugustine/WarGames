package com.jonoaugustine.wargames.common.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.jonoaugustine.wargames.common.ecs.components.CollisionCmpnt
import com.jonoaugustine.wargames.common.ecs.components.PathingCmpnt
import com.jonoaugustine.wargames.common.ecs.components.SpriteCmpnt
import com.jonoaugustine.wargames.common.ecs.components.TransformCmpnt
import com.jonoaugustine.wargames.common.ecs.gameState
import com.jonoaugustine.wargames.common.math.*
import com.jonoaugustine.wargames.common.math.Vector
import java.util.*

class PathingSystem : IteratingSystem(family { all(PathingCmpnt) }) {

  // TODO do this with coroutines for each entity
  override fun onTickEntity(entity: Entity) {
    val worldSize = world.gameState!!.mapSize
    val obstacles = world.family { all(CollisionCmpnt, SpriteCmpnt, TransformCmpnt) }
      .entities
      .filterNot { it == entity }
      .associate { it[TransformCmpnt] to it[CollisionCmpnt].hitboxes.values }
      .map { (tfc, hbs) ->
        hbs.map { rectangleFrom(tfc.position, it.size).toRotated(tfc.rotation) }
      }
      .flatten()
      .let { generateObstacles(it) }
      .toSet()

    val aStarPath = findShortestPath(
      entity[TransformCmpnt].position,
      entity[PathingCmpnt].destination,
      obstacles,
      0f..worldSize.width.toFloat(),
      0f..worldSize.height.toFloat()
    )
    entity[PathingCmpnt].path = aStarPath
  }
}

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
)

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
  obstacles: Set<Vector>,
  xBound: ClosedFloatingPointRange<Float> = 0f..500f,
  yBound: ClosedFloatingPointRange<Float> = 0f..500f,
): List<Vector> {
  // Create the start and goal nodes
  val startNode = Node(start)
  val goalNode = Node(goal)

  // Create the open and closed sets
  val openSet = PriorityQueue<Node>(compareBy { it.fScore })
  val closedSet = mutableSetOf<Node>()

  // Add the start node to the open set
  openSet.add(startNode)

  while (openSet.isNotEmpty()) {
    // Find the node with the lowest fScore in the open set
    val current = openSet.poll() ?: break

    // Move the current node from the open set to the closed set
    closedSet.add(current)

    // If the current node is the goal node, reconstruct the path and return it
    if (current.position == goalNode.position)
      return reconstructPath(current)

    // Generate the neighbor nodes
    val neighbors = generateNeighbors(current, obstacles, xBound, yBound)
      // Skip already evaluated nodes
      .filterNot { it in closedSet }

    for (neighbor in neighbors) {
      val tentativeGScore =
        current.gScore + current.position.distanceTo(neighbor.position)

      if (neighbor !in openSet || tentativeGScore < neighbor.gScore) {
        // Update the neighbor node with the new gScore and hScore
        neighbor.parent = current
        neighbor.gScore = tentativeGScore
        neighbor.hScore = neighbor.position.distanceTo(goalNode.position)

        if (neighbor !in openSet) {
          // Add the neighbor node to the open set
          openSet.add(neighbor)
        }
      }
    }
  }

  // No path found, return an empty list
  return emptyList()
}

/**
 * Generates the neighboring nodes for a given node
 * @param node The current node
 * @param obstacles The list of obstacle positions
 * @return The list of neighbor nodes
 */
fun generateNeighbors(
  node: Node,
  obstacles: Set<Vector>,
  xRange: ClosedFloatingPointRange<Float>,
  yRange: ClosedFloatingPointRange<Float>,
): List<Node> =
  listOf(-1f to 0f, 1f to 0f, 0f to -1f, 0f to 1f)
    .map { Vector(it.first, it.second) }
    .map { node.position + it }
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
