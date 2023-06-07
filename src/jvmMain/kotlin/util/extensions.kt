package util

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

val Size.dp: DpSize get() = DpSize(this.width.dp, this.height.dp)
