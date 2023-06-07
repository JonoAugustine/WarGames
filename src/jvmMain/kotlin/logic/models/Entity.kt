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
  fun update(delta: Float, game: Game): Unit
}

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

  override var position by mutableStateOf(position)
  var speed by mutableStateOf(speed)
  var size by mutableStateOf(size)
  var color by mutableStateOf(color)
  private var pathIndex by mutableStateOf(0f)
  private var _path by mutableStateOf<List<Offset>>(emptyList())
  private val nextPathOffset: Offset?
    get() = if (this.pathIndex < this._path.size) {
      this._path[this.pathIndex.toInt()]
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
    this.pathIndex = this.pathIndex + delta * this.speed
    // check collisions
    game.entities.filterIsInstance<BattleUnit>()
      .filterNot { it === this }
      .filter { this.collidesWith(it) }
      .takeIf { it.isNotEmpty() }
      ?.let {
        this._path = emptyList()
      }
    // move along path
    this.nextPathOffset
      ?.let { Vector(it.x - this.size.width / 2, it.y - this.size.height / 2) }
      ?.let { this.position = it }
  }
}

private val BattleUnit.minPos
  get() = Vector(
    this.position.x,
    this.position.y + this.size.height
  )
private val BattleUnit.maxPos
  get() = Vector(
    this.position.x + this.size.width,
    this.position.y
  )

fun BattleUnit.collidesWith(other: BattleUnit): Boolean {
  val d1x = other.minPos.x - this.maxPos.x
  val d1y = other.minPos.y - this.maxPos.y
  val d2x = this.minPos.x - other.maxPos.x
  val d2y = this.minPos.y - other.maxPos.y

  return this.position.x < other.maxPos.x &&
      this.maxPos.x > other.position.x &&
      this.position.y < other.minPos.y &&
      this.minPos.y > other.position.y

  //  if (d1x > 0.0f || d1y > 0.0f)
  //    return false;
  //
  //  if (d2x > 0.0f || d2y > 0.0f)
  //    return false;
  //
  //  return true;
  //return !((d1x > 0.0f || d1y > 0.0f) || (d2x > 0.0f || d2y > 0.0f))
}
