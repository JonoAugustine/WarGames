package ui.sprites.units

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.jonoaugustine.wargames.common.Match.State.PLACING
import com.jonoaugustine.wargames.common.Match.State.PLANNING
import com.jonoaugustine.wargames.common.ecs.components.CollisionCmpnt
import com.jonoaugustine.wargames.common.ecs.components.SpriteCmpnt
import com.jonoaugustine.wargames.common.ecs.components.TransformCmpnt
import com.jonoaugustine.wargames.common.ecs.components.centeredOn
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.network.missives.MoveUnit
import com.jonoaugustine.wargames.common.network.missives.SetUnitPath
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import state.AppState
import state.send
import ui.components.HoverBox
import util.composeColor
import util.dp

context(AppState, DefaultClientWebSocketSession)
@Composable
fun World.infantrySpriteOf(entity: Entity) {
  var movePreview: Vector by mutableStateOf(Vector.ZERO)
  val transform = entity[TransformCmpnt]
  val sprite = entity[SpriteCmpnt]

  HoverBox(
    Modifier.offset(transform.position.x.dp, transform.position.y.dp)
      .size(sprite.size.dp)
      .rotate(transform.rotation)
      .background(sprite.color.composeColor)
      .border(1.dp, if (entity[CollisionCmpnt].colliding) Color.Yellow else Color.Black)
      .pointerInput(entity, state.match!!.state) {
        if (state.match!!.state == PLACING)
          handleUnitDragging(entity.id, transform, sprite) { movePreview = it }
      }
  ) { hovering ->
    Text(entity.id.toString())
    UnitEnsign(sprite)
    // TODO unit doesn't show preview on first drag
    if (movePreview.x + movePreview.y != 0f) UnitEnsign(sprite, movePreview)

    // Show collisions
    //entity[CollisionCmpnt].hitboxes.forEach { (key, box) ->
    //  Box(
    //    Modifier.offset(box.offset.x.dp, box.offset.y.dp)
    //      .size(box.size.dp)
    //      .border(1.dp, Color.Yellow),
    //    content = { Text(key) }
    //  )
    //}

    // Path selector icon
    if (state.match!!.state == PLANNING) {
      val pathSelectorSize = Size(10f, 10f)
      Box(Modifier.clip(CircleShape)
        .background(Color.Black.copy(alpha = if (hovering) 0.7f else 0f))
        .size(pathSelectorSize.dp)
        .pointerInput(entity) { recordPath(entity.id, transform, sprite) }
        // TODO path start point offset not working
        .offset(100.dp, 100.dp)
      )
    }
  }
}

@Composable
private fun UnitEnsign(sprite: SpriteCmpnt, offset: Vector = Vector()) {
  Canvas(Modifier.fillMaxSize().offset(offset.x.dp, offset.y.dp)) {
    drawPath(color = Color.Black, style = Stroke(1f), path = Path().apply {
      moveTo(0f, 0f)
      lineTo(sprite.size.width.toFloat(), sprite.size.height.toFloat())
      moveTo(0f, sprite.size.height.toFloat())
      lineTo(sprite.size.width.toFloat(), 0f)
    })
  }
}

context(AppState, DefaultClientWebSocketSession, PointerInputScope)
@OptIn(DelicateCoroutinesApi::class)
private suspend fun handleUnitDragging(
  eid: Int,
  transform: TransformCmpnt,
  sprite: SpriteCmpnt,
  setPreviewPosition: (Vector) -> Unit,
) {
  var dragPos by mutableStateOf(transform.position)
  detectDragGestures(
    onDragEnd = {
      GlobalScope.launch(Dispatchers.IO) { send(MoveUnit(eid, dragPos)) }
      setPreviewPosition(Vector.ZERO)
    }
  ) { change, _ ->
    setPreviewPosition(
      Vector(
        change.position.x - sprite.size.width / 2,
        change.position.y - sprite.size.height / 2
      )
    )
    dragPos = Vector(
      (change.position.x + transform.position.x) - (sprite.size.width / 2),
      (change.position.y + transform.position.y) - (sprite.size.height / 2),
    )
  }
}

context(DefaultClientWebSocketSession, PointerInputScope)
@OptIn(ExperimentalComposeUiApi::class, DelicateCoroutinesApi::class)
private suspend fun recordPath(
  eid: Int,
  transform: TransformCmpnt,
  sprite: SpriteCmpnt,
) {
  var path by mutableStateOf(listOf<Vector>())
  var initialOffset = Offset(0f, 0f)
  val center = transform centeredOn sprite
  detectDragGestures(
    onDragStart = { offset ->
      initialOffset = offset
      path = mutableStateListOf(Vector(center.x, center.y))
    },
    onDrag = { change, _ ->
      path = path + change.historical.map { it.position }.map {
        Vector(
          it.x + center.x - initialOffset.x,
          it.y + center.y - initialOffset.y
        )
      }.plus(change.position.let {
        Vector(
          it.x + center.x - initialOffset.x,
          it.y + center.y - initialOffset.y
        )
      })
    },
    onDragEnd = {
      GlobalScope.launch(Dispatchers.IO) { send(SetUnitPath(eid, path)) }
    })
}

