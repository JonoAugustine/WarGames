package logic.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import logic.Game
import logic.MatchState
import util.Vector

data class CollisionBox(
  var position: Vector,
  var size: Size,
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

sealed interface Entity {

  val id: String
  val position: Vector
  val size: Size
  val collisionBox: CollisionBox
  fun update(delta: Float, game: Game): Unit
  fun collidesWith(other: BattleUnit): Boolean =
    with(Pair(this.collisionBox, other.collisionBox)) {
      first.position.x < second.maxPos.x &&
          first.maxPos.x > second.position.x &&
          first.position.y < second.minPos.y &&
          first.minPos.y > second.position.y
    }
}

val Entity.center
  get() = Vector(
    position.x + size.width / 2,
    position.y + size.height / 2
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
  var rotation by mutableStateOf(0f)
  override val collisionBox
    get() = CollisionBox(
      Vector(position.x - collisionMargin, position.y - collisionMargin),
      Size(size.width + collisionMargin, size.height + collisionMargin)
    )
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
    if (this._path.isEmpty() || game.clientMatch!!.state === MatchState.PLANNING) return
    this.pathIndex += delta * this.speed
    // move along path
    this.nextPathOffset?.let { this.position = it }
    // check collisions
    game.clientMatch!!.entities.filterIsInstance<BattleUnit>()
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





