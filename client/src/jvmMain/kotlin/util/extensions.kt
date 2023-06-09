package util

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.jonoaugustine.wargames.common.Color
import com.jonoaugustine.wargames.common.Size

val Size.dp get() = DpSize(width.dp, height.dp)
val Size.composeSize
  get() = androidx.compose.ui.geometry.Size(
    width.toFloat(),
    height.toFloat()
  )
val androidx.compose.ui.geometry.Size.dp: DpSize
  get() = DpSize(
    this.width.dp,
    this.height.dp
  )
val Color.composeColor
  get() = androidx.compose.ui.graphics.Color(
    red.toInt(),
    green.toInt(),
    blue.toInt(),
    alpha.toInt()
  )
