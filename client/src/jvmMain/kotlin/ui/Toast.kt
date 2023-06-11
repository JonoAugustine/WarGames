package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import state.AppState
import windowSize

context(AppState)
@Composable
fun Toast() {
  var queue by remember { mutableStateOf(emptyList<String>()) }

  Box(
    Modifier
      .width((windowSize.width / 3).dp)
      .height(30.dp),
  ) {
    Text(queue.first())
  }
}
