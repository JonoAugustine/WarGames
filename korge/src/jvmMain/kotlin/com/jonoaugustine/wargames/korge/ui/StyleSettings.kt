package com.jonoaugustine.wargames.korge.ui

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.io.async.ObservableProperty
import korlibs.korge.ui.UIButton
import korlibs.math.geom.Size

val style = ObservableProperty(StyleSettings())

data class StyleSettings(
  val text: TextStyleSettings = TextStyleSettings(),
  val buttons: ButtonStyleSettings = ButtonStyleSettings(),
  val colors: ColorStyleSettings = ColorStyleSettings()
)

data class TextStyleSettings(
  val small: Float = 16f,
  val medium: Float = 32f,
  val large: Float = 64f,
)

data class ButtonStyleSettings(
  val small: Size = UIButton.DEFAULT_SIZE,
  val medium: Size = Size(150, 42),
  val large: Size = Size(310, 70),
)

data class ColorStyleSettings(
  val primary: RGBA = Colors.FORESTGREEN,
  val primaryDark: RGBA = Colors.DARKGREEN,
  val secondary: RGBA = Colors.DEEPSKYBLUE,
  val secondaryDark: RGBA = Colors.STEELBLUE,
  val dark: RGBA = Colors["#333333"],
  val white: RGBA = Colors.GHOSTWHITE,
  val negative: RGBA = Colors["#DD242D"]
)

enum class Corners {
  TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}
