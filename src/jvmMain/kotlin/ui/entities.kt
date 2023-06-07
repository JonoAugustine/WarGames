package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import logic.models.BattleUnit

context(BattleUnit)
@OptIn(ExperimentalComposeUiApi::class)
suspend fun PointerInputScope.recordPath() = detectDragGestures(
  onDragStart = { offset ->
    path = mutableStateListOf(
      offset.copy(
        offset.x + position.x,
        offset.y + position.y
      )
    )
  },
  onDrag = { change, _ ->
    path = path + change.historical
      .filterIndexed { index, _ -> index % 10 == 0 }
      .map {
        it.position.copy(
          it.position.x + position.x,
          it.position.y + position.y
        )
      }
      .toTypedArray()
      .plus(change.position.run {
        copy(x + position.x, y + position.y)
    })
  },
)

@Composable
fun BattleUnit.sprite() {
  Box(
    Modifier
      .offset(this.position.x.dp, this.position.y.dp)
      .size(DpSize(this.size.width.dp, this.size.height.dp))
      .clip(RoundedCornerShape(0))
      .background(this.color)
      .pointerInput(Unit) { recordPath() }
  ) {
    Canvas(Modifier.fillMaxSize()) {
      drawPath(
        color = Color.Black,
        style = Stroke(1f),
        path = Path().apply {
          moveTo(0f, 0f)
          lineTo(this@sprite.size.width, this@sprite.size.height)
          lineTo(this@sprite.size.width, 0f)
          lineTo(0f, this@sprite.size.height)
        }
      )
    }
  }
}
