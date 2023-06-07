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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import logic.models.BattleUnit
import logic.models.Entity
import java.util.*
import kotlin.random.Random

data class Game(  val entities: List<Entity> = mutableStateListOf()) {
  var running: Boolean by mutableStateOf(false)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BattleUnitSprite(bu: BattleUnit, game: Game) {
  Box(
    Modifier
      .offset(bu.position.x.dp, bu.position.y.dp)
      .size(DpSize(bu.size.width.dp, bu.size.height.dp))
      .clip(RoundedCornerShape(0))
      .background(Color(150, 0, 0))
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { offset ->
            game.running = false
            bu.path = mutableStateListOf(
              offset.copy(
                offset.x + bu.position.x,
                offset.y + bu.position.y
              )
            )
          },
          onDrag = { change, _ ->
            bu.path = bu.path + change.historical
              .filterIndexed { index, _ -> index % 10 == 0 }
              .map {
                it.position.copy(
                  it.position.x + bu.position.x,
                  it.position.y + bu.position.y
                )
              }
              .toTypedArray()
              .plus(change.position.run {
                copy(x + bu.position.x, y + bu.position.y)
              })
          },
          onDragEnd = {
            game.running = true
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
          lineTo(bu.size.width, bu.size.height)
          lineTo(bu.size.width, 0f)
          lineTo(0f, bu.size.height)
        }
      )
    })
  }
}

@Composable
@Preview
fun App() {
  val game = remember {
    Game(
      entities = mutableStateListOf(
        BattleUnit(
          UUID.randomUUID().toString(),
          Vector((Random.nextFloat() * 1000), (Random.nextFloat() * 1000)),
          Size(if (Random.nextBoolean()) 50f else 100f, 50f)
        ),
        BattleUnit(
          UUID.randomUUID().toString(),
          Vector((Random.nextFloat() * 1000), (Random.nextFloat() * 1000)),
          Size(if (Random.nextBoolean()) 50f else 100f, 50f)
        ),
        BattleUnit(
          UUID.randomUUID().toString(),
          Vector((Random.nextFloat() * 1000), (Random.nextFloat() * 1000)),
          Size(if (Random.nextBoolean()) 50f else 100f, 50f)
        ),
      )
    )
  }
  LaunchedEffect(Unit) {
    var lastTime = 0L
    while (true) {
      withFrameNanos { time ->
        val delta = ((time - lastTime) / 1E8).toFloat()
        game.entities.forEach { it.update(delta, game) }
        lastTime = time
      }
    }
  }
  MaterialTheme {
    Box(
      modifier = Modifier.fillMaxSize().background(Color(153, 255, 102))
    )
    Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
      game.entities.filterIsInstance<BattleUnit>()
        .forEach { bu ->
          if (bu.path.size > 1) {
            drawPath(
              color = Color.Blue,
              style = Stroke(3f),
              path = Path().apply {
                bu.path.first().run { moveTo(x, y) }
                bu.path.subList(1, bu.path.size)
                  .filterIndexed { index, _ -> index % 2 == 0 }
                  .forEach { lineTo(it.x, it.y) }
                bu.path.last().let { last ->
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
    game.entities.filterIsInstance<BattleUnit>()
      .forEach { BattleUnitSprite(it, game) }
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
