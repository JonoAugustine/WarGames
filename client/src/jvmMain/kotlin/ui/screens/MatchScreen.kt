package ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher.Companion.mouse
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerButton.Companion.Primary
import androidx.compose.ui.input.pointer.pointerInput
import com.github.quillraven.fleks.World
import com.jonoaugustine.wargames.common.Grass
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.ecs.GameState.PLACING
import com.jonoaugustine.wargames.common.ecs.components.PathingCmpnt
import com.jonoaugustine.wargames.common.ecs.components.SpriteCmpnt
import com.jonoaugustine.wargames.common.ecs.components.TransformCmpnt
import com.jonoaugustine.wargames.common.ecs.entities.CombatUnit
import com.jonoaugustine.wargames.common.ecs.gameState
import com.jonoaugustine.wargames.common.math.Vector
import com.jonoaugustine.wargames.common.network.missives.SetUnitDestination
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
import util.toVector

context(AppState, DefaultClientWebSocketSession)
@Composable
fun MatchScreen() {
  if (state.lobby == null) return goTo(MAIN_MENU)
  var sprites by remember { mutableStateOf(listOf<@Composable () -> Unit>()) }

  LaunchedEffect(world) {
    inWorld {
      sprites = family { all(SpriteCmpnt, TransformCmpnt) }
        .entities
        .map<@Composable () -> Unit> { e -> @Composable { infantrySpriteOf(e) } }
    }
  }

  Box(Modifier.fillMaxSize().background(WgColor.Grass.composeColor))
  Grid()

  world.DrawPathLayer()
  world.PathingInputLayer()

  if (world.gameState?.state == PLACING) PlacementLayer()
  sprites.forEach { it() }
}

context(AppState, DefaultClientWebSocketSession)
@OptIn(DelicateCoroutinesApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlacementLayer() {
  Box(
    Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectTapGestures(mouse(Primary)) { offset ->
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
fun World.DrawPathLayer() {
  Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
    world.family { all(PathingCmpnt) }
      .entities
      .mapNotNull { it.getOrNull(PathingCmpnt) }
      .filterNot { it.path == null }
      .forEach { pathing ->
        if (pathing.path!!.size <= 1) return@forEach
        drawPath(
          color = Color.Blue,
          style = Stroke(3f),
          path = Path().apply {
            pathing.path!!.first().run { moveTo(x, y) }
            pathing.path!!.subList(1, pathing.path!!.size)
              .filterIndexed { index, _ -> index % 2 == 0 }
              .forEach { lineTo(it.x, it.y) }
            pathing.destination.let { (x, y) ->
              //draw X
              moveTo(x - 10, y - 10)
              lineTo(x + 10, y + 10)
              moveTo(x + 10, y - 10)
              lineTo(x - 10, y + 10)
            }
          }
        )
      }
  })
}

context(AppState, DefaultClientWebSocketSession)
@OptIn(ExperimentalFoundationApi::class, DelicateCoroutinesApi::class)
@Composable
fun World.PathingInputLayer() = Box(
  Modifier
    .fillMaxSize()
    .pointerInput(selectedEntity) {
      selectedEntity?.let {
        detectTapGestures(mouse(PointerButton.Secondary)) { offset ->
          GlobalScope.launch(Dispatchers.IO) {
            send(SetUnitDestination(it.id, offset.toVector()))
          }
        }
      }
    })
