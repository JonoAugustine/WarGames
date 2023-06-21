package com.jonoaugustine.wargames.common.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.ecs.components.CollisionCmpnt
import com.jonoaugustine.wargames.common.ecs.components.PathingCmpnt
import com.jonoaugustine.wargames.common.ecs.components.SpriteCmpnt
import com.jonoaugustine.wargames.common.ecs.components.TransformCmpnt
import com.jonoaugustine.wargames.common.ecs.gameState
import com.jonoaugustine.wargames.common.math.*
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.min
import java.util.*
import kotlin.math.abs

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
      entity[SpriteCmpnt].size.min / 2,
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
  obstacles: Set<Vector>,
  xBound: ClosedFloatingPointRange<Float>,
  yBound: ClosedFloatingPointRange<Float>,
): List<Vector> {
  // Create the start and goal nodes
  val startNode = Node(start)
  val goalNode = Node(goal)
  val marginGoal = rectangleFrom(goal, WgSize(margin, margin))

  /** Unevaluated nodes */
  val openQueue = PriorityQueue<Node>(compareBy { it.fScore })

  /** Unevaluated nodes */
  val openSet = mutableSetOf<Node>()
  val visited = mutableSetOf<Node>()

  // Add the start node to the open set
  openQueue.add(startNode)

  while (openQueue.isNotEmpty()) {
    // Find the node with the lowest fScore in the open set
    val current = openQueue.poll() ?: break
    openSet.remove(current)

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

        if (nbr !in openQueue || tentativeGScore < nbr.gScore) {
          // Update the neighbor node with the new gScore and hScore
          nbr.gScore = tentativeGScore
          nbr.hScore = nbr.position.distanceTo(goalNode.position)

          // Add the neighbor node to the open set
          if (nbr !in openSet) {
            openQueue.add(nbr)
            openSet.add(nbr)
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
  margin: Int,
  obstacles: Set<Vector>,
  xRange: ClosedFloatingPointRange<Float>,
  yRange: ClosedFloatingPointRange<Float>,
): List<Node> =
  listOf(
    -abs(margin - 1f) to 0f,
    abs(margin - 1f) to 0f,
    0f to -abs(margin - 1f),
    0f to abs(margin - 1f)
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
