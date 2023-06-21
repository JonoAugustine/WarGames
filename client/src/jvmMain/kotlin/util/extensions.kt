package util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.math.Vector

val WgSize.dp get() = DpSize(width.dp, height.dp)
val WgSize.composeSize
  get() = androidx.compose.ui.geometry.Size(
    width.toFloat(),
    height.toFloat()
  )
val androidx.compose.ui.geometry.Size.dp: DpSize
  get() = DpSize(
    this.width.dp,
    this.height.dp
  )
val WgColor.composeColor
  get() = androidx.compose.ui.graphics.Color(
    red.toInt(),
    green.toInt(),
    blue.toInt(),
    alpha.toInt()
  )

fun Offset.toVector(): Vector = Vector(x, y)
