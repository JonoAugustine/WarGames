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
import logic.Match
import logic.MatchState
import logic.models.BattleUnit
import logic.models.center
import util.Vector
import util.dp

context(Match)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BattleUnit.sprite() {
  var mouseOver by remember { mutableStateOf(false) }
  val requester = FocusRequester()
  Box(
    Modifier.offset(this.position.x.dp, this.position.y.dp)
      .size(this.size.dp)
      .rotate(rotation)
      .background(this.color)
      .focusRequester(requester)
      .focusable()
      .onKeyEvent {
        if (mouseOver && it.key == Key.R) {
          rotation += 12f
          true
        } else false
      }
      .onPointerEvent(PointerEventType.Enter) { mouseOver = true }
      .onPointerEvent(PointerEventType.Exit) { mouseOver = false }
      .pointerInput(Unit) { moveUnit() }
  ) {
    val dragBoxSize = Size(10f, 10f)
    if (state == MatchState.PLANNING) {
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
        lineTo(this@sprite.size.width, this@sprite.size.height)
        lineTo(this@sprite.size.width, 0f)
        lineTo(0f, this@sprite.size.height)
      })
    }
  }
}

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
      path = path + change.historical.map { it.position }.map {
        Offset(
          it.x + center.x - initialOffset.x,
          it.y + center.y - initialOffset.y
        )
      }.plus(change.position.let {
        Offset(
          it.x + center.x - initialOffset.x,
          it.y + center.y - initialOffset.y
        )
      })
    },
  )
}

context(BattleUnit, Match)
suspend fun PointerInputScope.moveUnit() {
  detectDragGestures(
    onDrag = { change, _ ->
      if (state == MatchState.PLACING) {
        position = change.position.let {
          Vector(it.x + position.x - size.width / 2, it.y + position.y - size.height / 2)
        }
      }
    },
  )
}
