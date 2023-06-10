package ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jonoaugustine.wargames.common.network.missives.CreateMatch
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.launch
import state.AppState
import state.Page.MAIN_MENU
import state.send
import ui.Menu

context(AppState, DefaultClientWebSocketSession)
@Composable
fun LobbyScreen() = Menu {
  val scope = rememberCoroutineScope()
  Button(
    modifier = Modifier.offset(10.dp, 10.dp),
    colors = ButtonDefaults.buttonColors(
      backgroundColor = Color.White,
      contentColor = Color.Black
    ),
    onClick = { goTo(MAIN_MENU) },
    content = { Text("Main Menu") }
  )
  Column(
    Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Button(
      colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green),
      onClick = { scope.launch { send(CreateMatch(state.lobby!!.id)) } },
      content = { Text("Start Match") }
    )
  }
}
