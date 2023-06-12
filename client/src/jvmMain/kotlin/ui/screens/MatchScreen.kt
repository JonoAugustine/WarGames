package ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jonoaugustine.wargames.common.BattleUnit
import com.jonoaugustine.wargames.common.Infantry
import com.jonoaugustine.wargames.common.Match.State.PLACING
import com.jonoaugustine.wargames.common.Vector
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.network.missives.PlaceEntity
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import state.AppState
import state.Page.MAIN_MENU
import state.send
import ui.components.Grid
import ui.spriteOf
import util.composeColor

context(AppState, DefaultClientWebSocketSession)
@Composable
fun MatchScreen() {
  if (state.match == null) return goTo(MAIN_MENU)
  Box(Modifier.fillMaxSize().background(state.match!!.background.composeColor))
  if (state.match!!.state == PLACING) PlacementLayer()
  Box {
    Text(
      state.match!!.state.name,
      color = Color.White,
      modifier = Modifier.padding(10.dp)
    )
  }
  state.match!!.entities.values
    .filterIsInstance<BattleUnit>()
    .forEach { spriteOf(it) }
  PathLayer()
  Grid()

  LaunchedEffect(Unit) {
    val size = WgSize(50, 25)
    Infantry(
      id = "",
      position = Vector(200f,200f),
      size = size,
      speed = 2f,
      color = state.match!!.players[state.user.id]!!.color
    )
      .let { PlaceEntity(state.match!!.id, it) }
      .let { GlobalScope.launch(Dispatchers.IO) { send(it) } }
  }
}

context(AppState, DefaultClientWebSocketSession)
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun PlacementLayer() {
  Box(
    Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectTapGestures { offset ->
          val size = WgSize(50, 25)
          Infantry(
            id = "",
            position = Vector(offset.x - size.width / 2, offset.y - size.height / 2),
            size = size,
            speed = 2f,
            color = state.match!!.players[state.user.id]!!.color
          )
            .let { PlaceEntity(state.match!!.id, it) }
            .let { GlobalScope.launch(Dispatchers.IO) { send(it) } }
        }
      }
  )
}

context(AppState, DefaultClientWebSocketSession)
@Composable
fun PathLayer() {
  Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
    state.match!!.entities.values
      .filterIsInstance<BattleUnit>()
      .forEach { bu ->
        if (bu.path.size <= 1) return@forEach
        drawPath(
          color = Color.Blue,
          style = Stroke(3f),
          path = Path().apply {
            bu.path.first().run { moveTo(x, y) }
            bu.path.subList(1, bu.path.size)
              .filterIndexed { index, _ -> index % 2 == 0 }
              .forEach { lineTo(it.x, it.y) }
            bu.path.last().let { last ->
              //draw X
              moveTo(last.x - 10, last.y - 10)
              lineTo(last.x + 10, last.y + 10)
              moveTo(last.x + 10, last.y - 10)
              lineTo(last.x - 10, last.y + 10)
              // draw outline
              val olxRoot = last.x - bu.size.width / 2
              val olyRoot = last.y - bu.size.height / 2
              moveTo(olxRoot, olyRoot)
              lineTo(olxRoot + bu.size.width, olyRoot)
              lineTo(olxRoot + bu.size.width, olyRoot + bu.size.height)
              lineTo(olxRoot, olyRoot + bu.size.height)
              lineTo(olxRoot, olyRoot)
            }
          }
        )
      }
  })
}

