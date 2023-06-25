package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jonoaugustine.wargames.common.network.missives.JoinLobby
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import state.AppState
import state.Page.MAIN_MENU
import state.send
import ui.components.Menu
import windowSize

context(AppState, DefaultClientWebSocketSession)
@Composable
fun LobbyBrowser() = Menu {
  val scope = rememberCoroutineScope()
  var lobbies by remember { mutableStateOf(emptySet<com.jonoaugustine.wargames.common.LobbyPreview>()) }
  var refresh by remember { mutableStateOf(false) }
  var failed by remember { mutableStateOf(false) }

  LaunchedEffect(refresh) {
    try {
      lobbies = client.get("api/lobbies").body<Set<com.jonoaugustine.wargames.common.LobbyPreview>>()
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }

  Button(
    modifier = Modifier.offset(10.dp, 10.dp),
    colors = ButtonDefaults.buttonColors(
      backgroundColor = Color.White,
      contentColor = Color.Black
    ),
    onClick = { goTo(MAIN_MENU) },
    content = { Text("Main Menu") }
  )
  if (failed) Text("Failed to fetch lobbies")
  else
    Column(
      Modifier.fillMaxWidth().padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      lobbies.forEach {
        Button(
          colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
          onClick = { scope.launch(Dispatchers.IO) { send(JoinLobby(it.id)) } },
        ) {
          Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Text(it.name, modifier = Modifier.padding(horizontal = 5.dp))
            Text("size: ${it.players}", modifier = Modifier.padding(horizontal = 5.dp))
          }
        }
      }
    }
  Button(
    modifier = Modifier.offset(
      (windowSize.width - windowSize.width / 8).dp,
      (windowSize.height - 100).dp
    ),
    colors = ButtonDefaults.buttonColors(
      backgroundColor = Color.White,
      contentColor = Color.Black
    ),
    onClick = { refresh = !refresh },
    content = { Text("Refresh") }
  )
}
