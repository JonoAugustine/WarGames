package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import logic.models.BattleUnit
import logic.models.center
import util.dp

/**
 * TODO Make sure path starts in center
 *
 */
context(BattleUnit)
@OptIn(ExperimentalComposeUiApi::class)
suspend fun PointerInputScope.recordPath() {
  var initialOffset = Offset(0f, 0f)
  detectDragGestures(
    onDragStart = { offset ->
      initialOffset = offset
      path = mutableStateListOf(Offset(center.x, center.y))
    },
    onDrag = { change, _ ->
      path = path + change.historical
        .map { it.position }
        .map {
          Offset(
            it.x + center.x - initialOffset.x,
            it.y + center.y - initialOffset.y
          )
        }
        .plus(change.position.let {
          Offset(
            it.x + center.x - initialOffset.x,
            it.y + center.y - initialOffset.y
          )
        })
    },
  )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BattleUnit.sprite() {
  var showDragBox by remember { mutableStateOf(false) }
  Box(
    Modifier
      .offset(this.position.x.dp, this.position.y.dp)
      .size(this.size.dp)
      .background(this.color)
      .onPointerEvent(PointerEventType.Enter) { showDragBox = true }
      .onPointerEvent(PointerEventType.Exit) { showDragBox = false }
  ) {
    val dragBoxSize = Size(10f, 10f)
    Box(
      Modifier
        .align(Alignment.Center)
        .clip(CircleShape)
        .background(Color.Black.copy(alpha = if (showDragBox) 0.7f else 0f))
        .size(dragBoxSize.dp)
        .pointerInput(Unit) { recordPath() }
    )
    Canvas(Modifier.fillMaxSize()) {
      drawPath(
        color = Color.Black,
        style = Stroke(1f),
        path = Path().apply {
          moveTo(0f, 0f)
          lineTo(this@sprite.size.width, this@sprite.size.height)
          lineTo(this@sprite.size.width, 0f)
          lineTo(0f, this@sprite.size.height)
        }
      )
    }
  }
}
