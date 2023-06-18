package com.jonoaugustine.wargames.common.entities

import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.Match.State.RUNNING
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.math.Rectangle
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.math.overlaps
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface PathBoundEntity : Entity {

  val path: List<Vector>
  val step: Float
  val speed: Float

  fun copy(
    path: List<Vector> = this.path,
    step: Float = this.step,
    speed: Float = this.speed
  ): PathBoundEntity

  fun move(delta: Float): Any =
    takeIf { path.isNotEmpty() }
      .let { it ?: return this }
      .takeIf { step.toInt() < path.size }
      .let { it ?: return copy(path = emptyList()) }
      // calc travel distance
      .let { step + delta * speed }
      .let { travel -> travel to (travel.toInt() == step.toInt()) }
      // return early with no actionable travel
      .let { (travel, isOld) -> if (isOld) return copy(step = travel) else travel }
      // get the next position
      .let { travel -> travel to (path.getOrNull(travel.toInt()) ?: path.last()) }
      .let { (t, pos) -> t to Vector(pos.x - size.width / 2, pos.y - size.height / 2) }
      .let { (travel, newPos) -> copy() }
}

sealed interface BattleUnit : PathBoundEntity {

  companion object {

    const val collisionMargin = 2
  }

  val color: WgColor get() = WgColor()
  override val collisionBox: Rectangle
    get() = Rectangle(
      Vector(position.x - collisionMargin, position.y - collisionMargin),
      WgSize(size.width + collisionMargin, size.height + collisionMargin)
    )
}

/**
 * Data container for Infantry [BattleUnit]
 *
 * @property step current index of the path
 */
@Serializable
@SerialName("entity.infantry")
data class Infantry(
  override val id: String,
  override val position: Vector,
  override val size: WgSize,
  override val speed: Float,
  override val rotation: Float = 0f,
  override val path: List<Vector> = emptyList(),
  override val color: WgColor = WgColor(200u),
  override val step: Float = 0f,
) : BattleUnit {

  override fun copy(path: List<Vector>, step: Float, speed: Float): Infantry =
    this.copy(id = id, path = path, step = step, speed = speed)

  override fun copy() {
  }

  override fun update(delta: Float, match: Match): Infantry =
    this.takeIf { match.state == RUNNING }
      .takeIf { path.isNotEmpty() }
      .let { it ?: return this }
      .takeIf { step.toInt() < path.size }
      .let { it ?: return this.copy(path = emptyList()) }
      // calc travel distance
      .let { step + delta * speed }
      .let { travel -> travel to (travel.toInt() == step.toInt()) }
      // return early with no actionable travel
      .let { (travel, isOld) -> if (isOld) return copy(step = travel) else travel }
      // get the next position
      .let { travel -> travel to (path.getOrNull(travel.toInt()) ?: path.last()) }
      .let { (t, pos) -> t to Vector(pos.x - size.width / 2, pos.y - size.height / 2) }
      // check for collisions
      .let { (travel, newPos) ->
        Triple(travel, newPos, Rectangle(newPos, size)
          .let { rec ->
            match.entities.values
              .filterNot { it.id == id }
              .any { rec.overlaps(it.collisionBox) }
          }
        )
      }
      .let { (travel, newPos, collides) ->
        this.copy(
          position = newPos,
          step = travel,
          path = if (collides) emptyList() else this.path
        )
      }
}

/**
 * Data container for Archer [BattleUnit]
 *
 * @property step current index of the path
 */
@Serializable
@SerialName("entity.archer")
data class Archer(
  override val id: String,
  override val position: Vector,
  override val size: WgSize,
  override val speed: Float,
  override val rotation: Float = 0f,
  override val path: List<Vector> = emptyList(),
  override val color: WgColor = WgColor(200u),
  val range: ClosedRange<Float>,
  override val step: Float = 0f,
) : BattleUnit {

  override fun copy(path: List<Vector>, step: Float, speed: Float): Archer {
    TODO("Not yet implemented")
  }

  override fun copy() {
    TODO("Not yet implemented")
  }

  override fun update(delta: Float, match: Match): Archer =
    this.takeIf { path.isNotEmpty() }
      ?.takeIf { step.toInt() < path.size }
      ?.takeIf { match.state == RUNNING }
      .let { it ?: return this }
      // calc travel distance
      .let { step + delta * speed }
      .let { travel -> travel to (travel.toInt() == step.toInt()) }
      // return early with no actionable travel
      .let { (travel, isOld) -> if (isOld) return copy(step = travel) else travel }
      // get the next position
      .let { travel -> travel to (path.getOrNull(travel.toInt()) ?: path.last()) }
      .let { (t, pos) -> t to Vector(pos.x - size.width / 2, pos.y - size.height / 2) }
      // check for collisions
      .let { (travel, newPos) ->
        Triple(travel, newPos, Rectangle(newPos, size)
          .let { rec ->
            match.entities.values
              .filterNot { it.id == id }
              .any { rec.overlaps(it.collisionBox) }
          }
        )
      }
      .let { (travel, newPos, collides) ->
        this.copy(
          position = newPos,
          step = travel,
          path = if (collides) emptyList() else this.path
        )
      }
}
