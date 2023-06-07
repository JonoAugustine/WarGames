package logic.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import logic.Game
import logic.GameState.PLANNING
import util.Vector

sealed interface Entity {

  val id: String
  val position: Vector
  val size: Size
  fun update(delta: Float, game: Game): Unit
}

data class CollisionBox(
  var position: Vector,
  var size: Size,
)

/**
 * TODO collision
 *
 * @constructor
 * TODO
 *
 * @param position position offset (top-left corner)
 * @param speed
 */
class BattleUnit(
  override val id: String,
  position: Vector,
  size: Size,
  speed: Float = 2f,
  color: Color = Color(150, 0, 0),
) : Entity {

  companion object {

    val collisionMargin = 2f
  }

  override var position by mutableStateOf(position)
  override var size by mutableStateOf(size)
  var speed by mutableStateOf(speed)
  var color by mutableStateOf(color)
  private var pathIndex by mutableStateOf(0f)
  private var _path by mutableStateOf<List<Offset>>(emptyList())
  private val nextPathOffset: Vector?
    get() = if (this.pathIndex < this._path.size) {
      this._path[this.pathIndex.toInt()]
        .let { Vector(it.x - this.size.width / 2, it.y - this.size.height / 2) }
    } else {
      this.pathIndex = 0f
      this._path = emptyList()
      null
    }
  var path
    get() = this._path
    set(value) {
      this.pathIndex = 0f
      this._path = value
    }

  override fun update(delta: Float, game: Game) {
    if (this._path.isEmpty() || game.state === PLANNING) return
    this.pathIndex += delta * this.speed
    // move along path
    this.nextPathOffset?.let { this.position = it }
    // check collisions
    game.entities.filterIsInstance<BattleUnit>()
      .filterNot { it === this }
      .filter { this.collidesWith(it) }
      .takeIf { it.isNotEmpty() }
      ?.let {
        this.pathIndex -= delta * this.speed
        this.position = this.nextPathOffset!!
        this._path = emptyList()
      }
  }
}

val BattleUnit.center
  get() = Vector(
    position.x + size.width / 2,
    position.y + size.height / 2
  )
private val BattleUnit.collisionBox
  get() = CollisionBox(
    Vector(
      this.position.x - BattleUnit.collisionMargin,
      this.position.y - BattleUnit.collisionMargin
    ),
    Size(
      this.size.width + BattleUnit.collisionMargin,
      this.size.height + BattleUnit.collisionMargin
    )
  )
private val CollisionBox.minPos
  get() = Vector(
    this.position.x,
    this.position.y + this.size.height
  )
private val CollisionBox.maxPos
  get() = Vector(
    this.position.x + this.size.width,
    this.position.y
  )

fun BattleUnit.collidesWith(other: BattleUnit): Boolean =
  with(Pair(this.collisionBox, other.collisionBox)) {
    first.position.x < second.maxPos.x &&
        first.maxPos.x > second.position.x &&
        first.position.y < second.minPos.y &&
        first.minPos.y > second.position.y
  }

