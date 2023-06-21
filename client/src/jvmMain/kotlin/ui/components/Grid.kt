package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import windowSize

@Composable
fun Grid() {
  Canvas(Modifier.fillMaxSize()) {
    drawPath(color = Color.Black, style = Stroke(0.5f), path = Path().apply {
      // latitude
      for (y in 0..2000 step 100) {
        moveTo(0f, y.toFloat())
        lineTo(windowSize.width, y.toFloat())
      }
      // longitude
      for (x in 0..2000 step 100) {
        moveTo(x.toFloat(), 0f)
        lineTo(x.toFloat(), windowSize.height)
      }
    })
  }
  for (y in 0..windowSize.height.toInt() step 100) {
    for (x in 0..windowSize.width.toInt() step 100) {
      Text("$x, $y", modifier = Modifier.offset(x.dp, y.dp))
    }
  }
}
