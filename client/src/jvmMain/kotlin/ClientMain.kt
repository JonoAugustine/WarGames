import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.jonoaugustine.wargames.common.ecs.gameState
import state.AppState
import state.AppState.inWorld
import state.Page.LOBBY
import state.Page.LOBBY_BROWSER
import state.Page.MAIN_MENU
import state.Page.MATCH_PLAY
import state.SocketContext
import ui.screens.LobbyBrowser
import ui.screens.LobbyScreen
import ui.screens.MainMenu
import ui.screens.WorldScreen
import util.dp

val windowSize = Size(1000f, 600f)

fun main() = application {
  val appState = remember { AppState }
  var windowState by remember {
    mutableStateOf(
      WindowState(width = windowSize.width.dp, height = windowSize.height.dp)
    )
  }

  LaunchedEffect(appState.world) {
    inWorld {
      gameState?.mapSize?.dp
        ?.takeUnless { it == windowState.size }
        ?.let { windowState = WindowState(width = it.width, height = it.height) }
    }
  }

  Window(
    title = "War Games",
    state = windowState,
    resizable = false,
    onCloseRequest = ::exitApplication,
  ) {
    MaterialTheme {
      with(appState) {
        SocketContext {
          when (page) {
            MAIN_MENU     -> MainMenu()
            LOBBY_BROWSER -> LobbyBrowser()
            LOBBY         -> LobbyScreen()
            MATCH_PLAY    -> WorldScreen()
          }
        }
      }
    }
  }
}
