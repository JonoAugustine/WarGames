package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.Match.State.PLACING
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import state.AppState
import util.composeColor
import util.dp

context(AppState, DefaultClientWebSocketSession)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BattleUnit.sprite() {
  var mouseOver by remember { mutableStateOf(false) }
  val requester = FocusRequester()
  Box(
    Modifier.offset(this.position.x.dp, this.position.y.dp)
      .size(this.size.dp)
      .rotate(rotation)
      .background(this.color.composeColor)
      .focusRequester(requester)
      .focusable()
      .onKeyEvent {
        if (mouseOver && it.key == Key.R) {
          TODO("send unit update event")
          true
        } else false
      }
      .onPointerEvent(PointerEventType.Enter) { mouseOver = true }
      .onPointerEvent(PointerEventType.Exit) { mouseOver = false }
      .pointerInput(Unit) { moveUnit() }
  ) {
    val dragBoxSize = Size(10f, 10f)
    if (state.match!!.state == Match.State.PLANNING) {
      Box(
        Modifier.align(Alignment.Center)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = if (mouseOver) 0.7f else 0f))
          .size(dragBoxSize.dp)
          .pointerInput(Unit) { recordPath() }
      )
    }
    Canvas(Modifier.fillMaxSize()) {
      drawPath(color = Color.Black, style = Stroke(1f), path = Path().apply {
        moveTo(0f, 0f)
        lineTo(this@sprite.size.width.toFloat(), this@sprite.size.height.toFloat())
        lineTo(this@sprite.size.width.toFloat(), 0f)
        lineTo(0f, this@sprite.size.height.toFloat())
      })
    }
  }
}

context(Entity)
@OptIn(ExperimentalComposeUiApi::class)
suspend fun PointerInputScope.recordPath() {
  var path by mutableStateOf(listOf<Vector>())
  var initialOffset = Offset(0f, 0f)
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
      TODO("send path update")
    }
  )
}

context(AppState, DefaultClientWebSocketSession, PointerInputScope)
private suspend fun BattleUnit.moveUnit() {
  val originalPos = this.position.copy()
  var dragPos by mutableStateOf(this.position)
  detectDragGestures(
    onDrag = { change, _ ->
      if (state.match!!.state == PLACING) {
        dragPos = Vector(
          change.position.x + this.position.x - this.size.width / 2,
          change.position.y + this.position.y - this.size.height / 2
        )
      }
    },
    onDragEnd = {
      TODO("send position update")
    }
  )
}
