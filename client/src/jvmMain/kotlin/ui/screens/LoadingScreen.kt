package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jonoaugustine.wargames.common.Color
import com.jonoaugustine.wargames.common.DarkGray
import kotlinx.coroutines.delay
import util.composeColor
import kotlin.time.Duration.Companion.seconds

@Composable
fun LoadingScreen(inText: String? = null) {
  var defaultText by remember { mutableStateOf("Loading.") }

  if (inText == null) {
    LaunchedEffect(Unit) {
      val range = 1..3
      var i = 1
      while (true) {
        defaultText = defaultText.replace(Regex("""(\s*\.)+"""), " .".repeat(i))
        i = if (range.contains(i + 1)) i + 1 else range.first
        delay(0.6.seconds)
      }
    }
  }

  Box(
    modifier = Modifier.fillMaxSize().background(Color.DarkGray.composeColor),
    contentAlignment = Alignment.BottomEnd
  ) {
    Text(
      text = inText ?: defaultText,
      textAlign = TextAlign.Left,
      color = Color(200u, 200u, 200u).composeColor,
      modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
    )
  }
}
