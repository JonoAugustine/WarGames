package ui.screens

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
import com.jonoaugustine.wargames.common.network.missives.CreateLobby
import com.jonoaugustine.wargames.common.network.missives.UpdateUsername
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import state.AppState
import state.Page.LOBBY_BROWSER
import state.send
import ui.components.Menu

context(AppState, DefaultClientWebSocketSession)
@Composable
fun MainMenu() = Menu {
  val scope = rememberCoroutineScope()
  var text by remember { mutableStateOf("") }
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text("Welcome ${state.user.name}", color = Color.White)
    Button(
      colors = buttonColors(backgroundColor = Color.Green),
      onClick = { scope.launch { send(CreateLobby) } },
      content = { Text("Create Lobby") }
    )
    Button(
      colors = buttonColors(backgroundColor = Color.White),
      onClick = { goTo(LOBBY_BROWSER) },
      content = { Text("Browse Lobby") }
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = text,
        onValueChange = { text = it.trim() },
        modifier = Modifier,
        colors = TextFieldDefaults.outlinedTextFieldColors(
          textColor = Color.White,
          focusedBorderColor = Color.Green,
          unfocusedBorderColor = Color.White,
          focusedLabelColor = Color.Green,
          unfocusedLabelColor = Color.White,
        ),
        label = { Text("Change Username") },
      )
      Button(
        colors = buttonColors(backgroundColor = Color.White),
        onClick = { scope.launch(Dispatchers.IO) { send(UpdateUsername(text)) } },
        content = { Text("Save") },
        modifier = Modifier.padding(horizontal = 5.dp).height(35.dp)
      )
    }
  }
}
