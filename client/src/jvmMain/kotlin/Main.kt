import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import state.AppState
import state.Page.MAIN_MENU
import state.Page.MATCH_CREATOR
import state.SocketContext
import ui.screens.MainMenu
import ui.screens.MatchCreator

val windowSize = Size(1920f / 2, 1080f / 2)

fun main() = application {
  val appState = remember { AppState }

  Window(
    title = "War Games",
    state = WindowState(width = windowSize.width.dp, height = windowSize.height.dp),
    onCloseRequest = ::exitApplication,
  ) {
    //LaunchedEffect(Unit) {
    //  var lastTime = 0L
    //  while (true) {
    //    withFrameNanos { time ->
    //      val delta = ((time - lastTime) / 1E8).toFloat()
    //      lastTime = time
    //      game.update(delta)
    //    }
    //  }
    //}
    MaterialTheme {
      with(appState) {
        SocketContext {
          when (page) {
            MAIN_MENU     -> MainMenu()
            MATCH_CREATOR -> MatchCreator()
            //MATCH_PLAY    ->  with(socket) { MatchScreen(match) }
            else          -> TODO()
          }
        }
      }
    }
  }
}
