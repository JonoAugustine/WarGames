package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable

@Serializable
data class WgSize(val width: Int, val height: Int)

operator fun WgSize.div(i: Int): WgSize = WgSize(width / i, height / i)

