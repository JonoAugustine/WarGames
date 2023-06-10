import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import state.AppState
import state.Page.LOBBY
import state.Page.MAIN_MENU
import state.Page.MATCH_PLAY
import state.SocketContext
import ui.screens.LobbyScreen
import ui.screens.MainMenu
import ui.screens.MatchScreen

val windowSize = Size(1920f / 2, 1080f / 2)

fun main() = application {
  val appState = remember { AppState }

  Window(
    title = "War Games",
    state = WindowState(width = windowSize.width.dp, height = windowSize.height.dp),
    resizable = false,
    onCloseRequest = ::exitApplication,
  ) {
//    LaunchedEffect(Unit) {
    //      while (true) {
    //        println("WATCHER: ${appState.state}")
    //        delay(15.seconds)
    //      }
    //    }
    MaterialTheme {
      with(appState) {
        SocketContext {
          when (page) {
            MAIN_MENU  -> MainMenu()
            LOBBY      -> LobbyScreen()
            MATCH_PLAY -> MatchScreen()
          }
        }
      }
    }
  }
}
