package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable

@Serializable
data class Color(
  val red: UByte = 0u,
  val green: UByte = 0u,
  val blue: UByte = 0u,
  val alpha: UByte = 255u,
)

val Color.Companion.DarkGray get() = Color(51u, 51u, 51u)
val Color.Companion.Grass get() = Color(72u, 111u, 56u)
val Color.Companion.Blue get() = Color(25u, 0u, 221u)
val Color.Companion.Red get() = Color(221u, 0u, 25u)
