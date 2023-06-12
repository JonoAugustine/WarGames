package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import ui.components.BackButton
import ui.components.Menu
import util.composeColor

context(AppState, DefaultClientWebSocketSession)
@Composable
fun LobbyScreen() = Menu {
  val scope = rememberCoroutineScope()
  BackButton(MAIN_MENU, "Main Menu")
  Column(
    Modifier.fillMaxSize(0.95f),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      state.lobby!!.name,
      color = Color.White,
    )
  }
  Spacer(Modifier.height(30.dp))
  Row(
    Modifier.fillMaxWidth(0.9f),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    SettingsColumn()
    PlayerList()
  }
  if (state.user.id == state.lobby!!.hostID) Column(
    Modifier.fillMaxSize(0.98f),
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
    Modifier.fillMaxWidth(0.4f),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
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
    Modifier.fillMaxWidth(0.4f),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text("Players", color = Color.White)
    Spacer(Modifier.height(15.dp))
    state.lobby!!.players.values.forEach {
      Box(
        Modifier
          .border(1.dp, Color.White, RoundedCornerShape(5.dp))
          .fillMaxWidth(0.80f)
      ) {
        Row(
          Modifier.fillMaxWidth(0.90f).padding(0.dp, 4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Spacer(Modifier.width(10.dp))
          Box(Modifier.size(10.dp).background(it.color.composeColor))
          Spacer(Modifier.width(10.dp))
          Text(it.user.name, color = Color.White)
        }
      }
      Spacer(Modifier.height(5.dp))
    }
  }
}
