import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import java.util.*
import kotlin.random.Random

data class Game(var points: MutableMap<String, List<Offset>> = mutableStateMapOf())

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BattleUnit(game: Game) {
  val eid = UUID.randomUUID().toString()
  val location = Position((Random.nextFloat() * 1000).dp, (Random.nextFloat() * 1000).dp)
  val size = DpSize(Dp(if (Random.nextBoolean()) 50f else 100f), Dp(50f))
  Box(
    Modifier
      .offset(location.x, location.y)
      .size(size)
      .clip(RoundedCornerShape(0))
      .background(Color(150, 0, 0))
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { offset ->
            game.points[eid] = listOf(
              offset.copy(
                offset.x + location.x.value,
                offset.y + location.y.value
              )
            )
          },
          onDrag = { change, _ ->
            game.points[eid] = game.points[eid]!! + change.historical
              .map {
                it.position.copy(
                  it.position.x + location.x.value,
                  it.position.y + location.y.value
                )
              }
              .toTypedArray()
              .plus(change.position.run {
                copy(x + location.x.value, y + location.y.value)
              })
          }
        )
      }
  ) {
    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
      drawPath(
        color = Color.Black,
        style = Stroke(1f),
        path = Path().apply {
          moveTo(0f, 0f)
          lineTo(size.width.value, size.height.value)
          lineTo(size.width.value, 0f)
          lineTo(0f, size.height.value)
        }
      )
    })
  }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
  val game = remember { Game() }
  LaunchedEffect(Unit) {
    while (true) {
      withFrameMillis {

      }
    }
  }
  MaterialTheme {
    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
      game.points.forEach { (eid, points) ->
        if (points.size > 1) {
          drawPath(
            color = Color.Blue,
            style = Stroke(3f),
            path = Path().apply {
              points.first().run { moveTo(x, y) }
              points.subList(1, points.size)
                .filterIndexed { index, _ -> index % 2 == 0 }
                .forEach { lineTo(it.x, it.y) }
              points.last().let { last ->
                moveTo(last.x - 10, last.y - 10)
                lineTo(last.x + 10, last.y + 10)
                moveTo(last.x + 10, last.y - 10)
                lineTo(last.x - 10, last.y + 10)
              }
            }
          )
        }
      }
    })
    for (i in 0..3) {
      BattleUnit(game)
    }
  }
}

fun main() = application {
  Window(
    title = "War Games",
    state = WindowState(width = 1920.dp, height = 1080.dp),
    onCloseRequest = ::exitApplication
  ) {
    App()
  }
}
