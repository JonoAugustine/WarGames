package ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.jonoaugustine.wargames.common.Grass
import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.Match.State.PLACING
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.ecs.components.SpriteCmpnt
import com.jonoaugustine.wargames.common.ecs.components.TransformCmpnt
import com.jonoaugustine.wargames.common.ecs.entities.CombatUnit
import com.jonoaugustine.wargames.common.entities.BattleUnit
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.network.missives.SetMatchState
import com.jonoaugustine.wargames.common.network.missives.SpawnUnit
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import state.AppState
import state.Page.MAIN_MENU
import state.send
import ui.components.Grid
import ui.sprites.units.infantrySpriteOf
import util.composeColor
import java.util.*

context(AppState, DefaultClientWebSocketSession)
@OptIn(DelicateCoroutinesApi::class)
@Composable
fun MatchScreen() {
  if (state.match == null) return goTo(MAIN_MENU)
  val nextState = Match.State.values().asList()
    .getOrElse(state.match!!.state.ordinal + 1) { PLACING }
  //
  Box(Modifier.fillMaxSize().background(WgColor.Grass.composeColor))
  Grid()
  if (state.match!!.state == PLACING) PlacementLayer()
  Button(
    enabled = state.match!!.state != Match.State.RUNNING,
    onClick = {
      GlobalScope.launch(Dispatchers.IO) { send(SetMatchState(nextState)) }
    }) {
    Text("Start ${nextState.name.lowercase(Locale.US)}", color = Color.White)
  }
  var sprites by remember { mutableStateOf(listOf<@Composable () -> Unit>()) }
  LaunchedEffect(world) {
    inWorld {
      sprites = family { all(SpriteCmpnt, TransformCmpnt) }
        .entities
        .map<@Composable () -> Unit> { e -> @Composable { infantrySpriteOf(e) } }
    }
  }

  sprites.forEach { it() }
  PathLayer()
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
          GlobalScope.launch(Dispatchers.IO) {
            send(
              SpawnUnit(
                state.user.id,
                Vector(offset.x - size.width / 2, offset.y - size.height / 2),
                CombatUnit
              )
            )
          }
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

