import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import logic.Game
import logic.models.BattleUnit
import ui.screens.GameScreen
import util.Vector
import java.util.*
import kotlin.random.Random

val windowSize = Size(1920f / 2, 1080f / 2)

fun main() = application {
  val game = remember {
    Game(
      entities = mutableStateListOf(
        BattleUnit(
          UUID.randomUUID().toString(),
          Vector(windowSize.width / 4, windowSize.height / 4),
          Size(if (Random.nextBoolean()) 50f else 100f, 50f)
        ),
        BattleUnit(
          UUID.randomUUID().toString(),
          Vector(windowSize.width / 4, windowSize.height / 2),
          Size(if (Random.nextBoolean()) 50f else 100f, 50f)
        ),
        BattleUnit(
          UUID.randomUUID().toString(),
          Vector(windowSize.width / 8, windowSize.height / 8),
          Size(if (Random.nextBoolean()) 50f else 100f, 50f)
        ),
      )
    )
  }

  Window(
    title = "War Games",
    state = WindowState(width = windowSize.width.dp, height = windowSize.height.dp),
    onCloseRequest = ::exitApplication
  ) {
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
      GameScreen(game)
    }
  }
}
