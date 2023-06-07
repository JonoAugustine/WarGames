package logic.models

import Game
import Vector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

sealed interface Entity {

  val id: String
  val position: Vector
  fun update(delta: Float, gamestate: Game): Unit
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
) : Entity {

  override var position by mutableStateOf(position)
  var speed by mutableStateOf(speed)
  var size by mutableStateOf(size)
  private var pathIndex by mutableStateOf(0f)
  private val nextPathOffset: Offset?
    get() = if (this.pathIndex < this._path.size) {
      this._path[this.pathIndex.toInt()]
    } else {
      this.pathIndex = 0f
      this._path = emptyList()
      null
    }
  private var _path by mutableStateOf<List<Offset>>(emptyList())
  var path
    get() = this._path
    set(value) {
      this.pathIndex = 0f
      this._path = value
    }

  override fun update(delta: Float, gamestate: Game) {
    if (this._path.isEmpty() || !gamestate.running && this.speed > 0) return
    this.pathIndex = this.pathIndex + delta * this.speed
    this.nextPathOffset
      ?.let { Vector(it.x - this.size.width / 2, it.y - this.size.height / 2) }
      ?.let { this.position = it }
  }
}
