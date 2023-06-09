package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import com.jonoaugustine.wargames.common.network.UpdateUsername
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.launch
import state.AppState
import state.Page.MATCH_CREATOR
import state.send
import java.util.*

context(AppState, DefaultClientWebSocketSession)
@Composable
fun MainMenu() {
  val scope = rememberCoroutineScope()
  var text by remember { mutableStateOf(UUID.randomUUID().toString().substring(0..5)) }

  Box(Modifier.fillMaxSize().background(Color.Black))
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text("Welcome ${data.user.name}", color = Color.White)
    Button(
      colors = buttonColors(backgroundColor = Color.Green),
      onClick = { goTo(MATCH_CREATOR) },
      content = { Text("Create Match") }
    )
    OutlinedTextField(
      value = text,
      onValueChange = { text = it.trim() },
      colors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = Color.White,
        focusedBorderColor = Color.Green,
        unfocusedBorderColor = Color.White,
        focusedLabelColor = Color.Green,
        unfocusedLabelColor = Color.White,
      ),
      label = { Text("Username") },
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = {
        scope.launch { send(UpdateUsername(text)) }
      })
    )
    Button(
      colors = buttonColors(backgroundColor = Color.Blue),
      onClick = { scope.launch { send(UpdateUsername(text)) } },
      content = { Text("Update Name") }
    )
  }
}

