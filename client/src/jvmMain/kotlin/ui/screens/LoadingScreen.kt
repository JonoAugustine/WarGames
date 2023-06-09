package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jonoaugustine.wargames.common.Color
import com.jonoaugustine.wargames.common.DarkGray
import util.composeColor

@Composable
fun LoadingScreen(text: String) {
  Column(Modifier.fillMaxSize().background(Color.DarkGray.composeColor)) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
      Text(text = text, modifier = Modifier)
    }
  }
}
