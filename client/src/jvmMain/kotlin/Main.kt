import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import logic.Game
import ui.screens.GameScreen
import ui.screens.MainMenu

val windowSize = Size(1920f / 2, 1080f / 2)

fun main() = application {
  val game = remember { Game() }

  Window(
    title = "War Games",
    state = WindowState(width = windowSize.width.dp, height = windowSize.height.dp),
    onCloseRequest = ::exitApplication,
  ) {
    LaunchedEffect(Unit) {
      var lastTime = 0L
      while (true) {
        withFrameNanos { time ->
          val delta = ((time - lastTime) / 1E8).toFloat()
          lastTime = time
          game.update(delta)
        }
      }
    }
    MaterialTheme {
      with(game) {
        if (match == null) MainMenu()
        else with(match!!) { GameScreen() }
      }
    }
  }
}
