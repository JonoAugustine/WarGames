package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jonoaugustine.wargames.common.network.missives.CreateMatch
import com.jonoaugustine.wargames.common.network.missives.UpdateLobbyName
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import state.AppState
import state.Page.MAIN_MENU
import state.send
import ui.BackButton
import ui.Menu
import windowSize

context(AppState, DefaultClientWebSocketSession)
@Composable
fun LobbyScreen() = Menu {
  val scope = rememberCoroutineScope()
  BackButton(MAIN_MENU, "Main Menu")
  Text(
    state.lobby!!.name,
    color = Color.White,
    modifier = Modifier.offset(windowSize.width.dp / 2, 10.dp)
  )
  Row(
    Modifier.fillMaxSize(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceEvenly
  ) {
    SettingsColumn()
    PlayerList()
  }
  if (state.user.id == state.lobby!!.hostID) Column(
    Modifier.fillMaxSize(0.98F),
    verticalArrangement = Arrangement.Bottom,
    horizontalAlignment = Alignment.End
  ) {
    Button(
      colors = buttonColors(backgroundColor = Color.Green),
      onClick = { scope.launch(Dispatchers.IO) { send(CreateMatch(state.lobby!!.id)) } },
      content = { Text("Start Match") }
    )
  }
}

context(AppState, DefaultClientWebSocketSession)
@Composable
fun SettingsColumn() {
  val scope = rememberCoroutineScope()
  var nameText by remember { mutableStateOf(state.lobby!!.name) }
  // Settings
  Column(
    Modifier.fillMaxHeight().fillMaxWidth(0.4f),
    verticalArrangement = Arrangement.Center,
    //horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text("Settings", color = Color.White)
    Spacer(Modifier.height(20.dp))
    OutlinedTextField(
      value = nameText,
      onValueChange = { nameText = it.trim() },
      modifier = Modifier,
      colors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = Color.White,
        focusedBorderColor = Color.Green,
        unfocusedBorderColor = Color.White,
        focusedLabelColor = Color.Green,
        unfocusedLabelColor = Color.White,
      ),
      label = { Text("Lobby Name") },
    )
    Button(
      colors = buttonColors(backgroundColor = Color.White),
      onClick = {
        scope.launch(Dispatchers.IO) {
          send(UpdateLobbyName(state.lobby!!.id, nameText))
        }
      },
      content = { Text("Save") },
      modifier = Modifier.padding(horizontal = 5.dp).height(35.dp)
    )
  }
}

context(AppState, DefaultClientWebSocketSession)
@Composable
fun PlayerList() {
  Column(
    Modifier.fillMaxHeight().fillMaxWidth(0.4f),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    state.lobby!!.players.values.forEach {
      Box(Modifier.background(Color.White).padding(7.dp)) {
        Text(          it.user.name,        )
      }
      Spacer(Modifier.height(10.dp))
    }
  }
}
