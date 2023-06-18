package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable

@Serializable
data class WgColor(
  val red: UByte = 0u,
  val green: UByte = 0u,
  val blue: UByte = 0u,
  val alpha: UByte = 255u,
)

val WgColor.Companion.DarkGray get() = WgColor(51u, 51u, 51u)
val WgColor.Companion.Grass get() = WgColor(72u, 111u, 56u)
val WgColor.Companion.Blue get() = WgColor(25u, 0u, 221u)
val WgColor.Companion.Red get() = WgColor(221u, 0u, 25u)
