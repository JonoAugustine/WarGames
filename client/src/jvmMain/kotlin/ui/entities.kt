package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.Match.State.PLACING
import com.jonoaugustine.wargames.common.network.missives.MoveEntity
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
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun spriteOf(unit: BattleUnit) {
  var dragPos by mutableStateOf(unit.position)
  var movePreview: Vector by mutableStateOf(Vector())

  HoverBox(
    Modifier.offset(unit.position.x.dp, unit.position.y.dp)
      .size(unit.size.dp)
      .rotate(unit.rotation)
      .background(unit.color.composeColor)
      .border(1.dp, Color.Black)
      .pointerInput(unit) {
        detectDragGestures(
          onDragEnd = {
            GlobalScope.launch(Dispatchers.IO) { send(MoveEntity(unit.id, dragPos)) }
            movePreview = Vector()
          }
        ) { change, _ ->
          if (state.match!!.state != PLACING) return@detectDragGestures
          movePreview = Vector(
            change.position.x - unit.size.width / 2,
            change.position.y - unit.size.height / 2
          )
          dragPos = Vector(
            (change.position.x + unit.position.x) - (unit.size.width / 2),
            (change.position.y + unit.position.y) - (unit.size.height / 2),
          )
        }
      }
  ) { hovering ->
    UnitEnsign(unit)
    if (movePreview.x + movePreview.y != 0f) UnitEnsign(unit, movePreview)
    if (state.match!!.state == Match.State.PLANNING) {
      val pathSelectorSize = Size(10f, 10f)
      Box(Modifier.clip(CircleShape)
        .background(Color.Black.copy(alpha = if (hovering) 0.7f else 0f))
        .size(pathSelectorSize.dp)
        .pointerInput(Unit) { with(unit) { recordPath() } }
        .offset(
          (unit.center.x - pathSelectorSize.width / 2).dp,
          (unit.center.y - pathSelectorSize.height / 2).dp
        ))
    }
  }
}

@Composable
fun UnitEnsign(unit: BattleUnit, offset: Vector = Vector()) {
  Canvas(Modifier.fillMaxSize().offset(offset.x.dp, offset.y.dp)) {
    drawPath(color = Color.Black, style = Stroke(1f), path = Path().apply {
      moveTo(0f, 0f)
      lineTo(unit.size.width.toFloat(), unit.size.height.toFloat())
      moveTo(0f, unit.size.height.toFloat())
      lineTo(unit.size.width.toFloat(), 0f)
    })
  }
}

context (Entity)
@OptIn(ExperimentalComposeUiApi::class)
suspend fun PointerInputScope.recordPath() {
  var path by mutableStateOf(listOf<Vector>())
  var initialOffset = Offset(0f, 0f)
  detectDragGestures(onDragStart = { offset ->
    initialOffset = offset
    path = mutableStateListOf(Vector(center.x, center.y))
  }, onDrag = { change, _ ->
    path = path + change.historical.map { it.position }.map {
      Vector(
        it.x + center.x - initialOffset.x, it.y + center.y - initialOffset.y
      )
    }.plus(change.position.let {
      Vector(
        it.x + center.x - initialOffset.x, it.y + center.y - initialOffset.y
      )
    })
  }, onDragEnd = {
    TODO("send path update")
  })
}

