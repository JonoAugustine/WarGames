package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import logic.Game
import logic.Match
import windowSize

context(Game)
@Composable
fun MainMenu() {
  Box(Modifier.fillMaxSize().background(Color.Black))
  Column {
    Button(
      colors = buttonColors(backgroundColor = Color.Green),
      onClick = {
        match = Match()
      }) { Text("Start Game") }
  }
}
