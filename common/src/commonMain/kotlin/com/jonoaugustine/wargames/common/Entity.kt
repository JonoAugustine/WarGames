package com.jonoaugustine.wargames.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias EntityID = String

sealed interface Entity {

  val id: EntityID
  val position: Vector
  val size: WgSize
  val rotation: Float
  val collisionBox: Rectangle
  fun update(delta: Float, match: Match): Entity
  fun collidesWith(other: Entity): Boolean =
    this.collisionBox.overlaps(other.collisionBox)
}

val Entity.center
  get() = Vector(position.x + size.width / 2, position.y + size.height / 2)

sealed interface BattleUnit : Entity {

  companion object {

    const val collisionMargin = 2
  }

  val speed: Float
  val path: List<Vector>
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
  private val step: Float = 0f,
) : BattleUnit {

  override fun update(delta: Float, match: Match): Infantry {
    if (
      path.isEmpty() ||
      step.toInt() >= path.size ||
      match.state === Match.State.PLANNING
    ) return this
    val nextStep = step + delta * this.speed
    if (step.toInt() == nextStep.toInt()) return this.copy(step = nextStep)
    val nextPos = with(path[nextStep.toInt()]) {
      Vector(x - size.width / 2, y - size.height / 2)
    }
    val collides = with(Rectangle(nextPos, this.size)) {
      match.entities.values.filterIsInstance<BattleUnit>()
        .filterNot { it === this@Infantry }
        .any { this@with.overlaps(it.collisionBox) }
    }
    // move to next position
    return this.copy(
      position = nextPos,
      step = nextStep,
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
  private val step: Float = 0f,
) : BattleUnit {

  override fun update(delta: Float, match: Match): Archer {
    if (
      path.isEmpty() ||
      step.toInt() >= path.size ||
      match.state === Match.State.PLANNING
    ) return this
    val nextStep = step + delta * this.speed
    if (step.toInt() == nextStep.toInt()) return this.copy(step = nextStep)
    val nextPos = with(path[nextStep.toInt()]) {
      Vector(x - size.width / 2, y - size.height / 2)
    }
    val collides = with(Rectangle(nextPos, this.size)) {
      match.entities.values.filterIsInstance<BattleUnit>()
        .filterNot { it === this@Archer }
        .any { this@with.overlaps(it.collisionBox) }
    }
    // move to next position
    return this.copy(
      position = nextPos,
      step = nextStep,
      path = if (collides) emptyList() else this.path
    )
  }
}
