package com.jonoaugustine.wargames.common

sealed interface Entity {

  val id: String
  val position: Vector
  val size: Size
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
  override val collisionBox: Rectangle
    get() = Rectangle(
      Vector(position.x - collisionMargin, position.y - collisionMargin),
      Size(size.width + collisionMargin, size.height + collisionMargin)
    )
}

/**
 * Data container for Infantry [BattleUnit]
 *
 * @property step current index of the path
 */
data class Infantry(
  override val id: String,
  override val position: Vector,
  override val size: Size,
  override val rotation: Float,
  override val speed: Float,
  override val path: List<Vector> = emptyList(),
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
      match.entities.filterIsInstance<BattleUnit>()
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

