package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

@Serializable
data class WgSize(val width: Int, val height: Int)

operator fun WgSize.times(f: Int): WgSize = WgSize(width * f, height * f)

operator fun WgSize.div(i: Int): WgSize = WgSize(width / i, height / i)

/** Returns the minimum of the [width][WgSize.width] or [height][WgSize.height] */
val WgSize.min get() = min(width, height)
val WgSize.max get() = max(width, height)
