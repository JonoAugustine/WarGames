package ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HoverBox(
  modifier: Modifier,
  onHoverChange: (Boolean) -> Unit = {},
  content: @Composable (Boolean) -> Unit
) {
  var mouseOver by remember { mutableStateOf(false) }
  Box(
    Modifier
      .then(modifier)
      .onPointerEvent(PointerEventType.Enter) {
        mouseOver = true
        onHoverChange(true)
      }
      .onPointerEvent(PointerEventType.Exit) {
        mouseOver = false
        onHoverChange(false)
      },
    content = { content(mouseOver) },
  )
}
