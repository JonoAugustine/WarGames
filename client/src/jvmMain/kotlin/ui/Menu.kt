package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.DarkGray
import util.composeColor

@Composable
fun Menu(content: @Composable () -> Unit) {
  Box(Modifier.fillMaxSize().background(WgColor.DarkGray.composeColor))
  content()
}
