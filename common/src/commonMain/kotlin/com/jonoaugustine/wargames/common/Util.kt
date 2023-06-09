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
