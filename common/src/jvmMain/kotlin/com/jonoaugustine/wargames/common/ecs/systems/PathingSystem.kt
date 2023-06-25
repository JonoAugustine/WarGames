package com.jonoaugustine.wargames.common.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import com.jonoaugustine.wargames.common.ecs.components.*
import com.jonoaugustine.wargames.common.ecs.components.HitboxKeys.BODY
import com.jonoaugustine.wargames.common.ecs.gameState
import com.jonoaugustine.wargames.common.math.*
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*

class PathingSystem : IntervalSystem(Fixed(0.2f)) {

  private val scope = inject<CoroutineScope>()
  private val pathingJobs = Collections.synchronizedMap(mutableMapOf<Entity, Job>())

  private fun setPath(entity: Entity, obstacles: List<Vector>) {
    val pathing = entity[PathingCmpnt]
    val sprite = entity[SpriteCmpnt]

    val margin = sprite.size.max / 2
    //pathing.path?.size
    //?.let { if (it > sprite.size.max) 5 else 2 }
    //?: 10

    val aStarPath = findShortestPath(
      centerOf(entity[TransformCmpnt], sprite),
      pathing.destination,
      margin,
      obstacles,
      0f..world.gameState!!.mapSize.width.toFloat(),
      0f..world.gameState!!.mapSize.height.toFloat()
    )
    pathing.path = aStarPath
    entity[PathingCmpnt].recalc = false
  }

  override fun onTick() {
    // generate obstacles
    val obstacles = world.family { all(CollisionCmpnt, SpriteCmpnt, TransformCmpnt) }
      .entities
      .associate { e ->
        val (pos, rotation) = e[TransformCmpnt]
        val hitboxes = e[CollisionCmpnt].hitboxes.filterKeys { it == BODY }.values
        e to generateObstacles(
          hitboxes.map { rectangleFrom(pos + it.offset, it.size).toRotated(rotation) }
        )
      }

    // calc paths
    val pending = world.family { all(PathingCmpnt) }.entities
      .filter { it[PathingCmpnt].recalc }

    synchronized(pathingJobs) {
      pending.filter { it in pathingJobs }
        .mapNotNull { pathingJobs.remove(it) }
        .forEach { it.cancel("path recalculated") }

      pending.associateTo(pathingJobs) {
        it to scope.launch { setPath(it, (obstacles - it).values.flatten()) }
      }
    }
  }
}

