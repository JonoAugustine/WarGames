package com.jonoaugustine.wargames.korge.ui

import com.jonoaugustine.wargames.korge.ui.Corners.BOTTOM_LEFT
import com.jonoaugustine.wargames.korge.ui.Corners.BOTTOM_RIGHT
import com.jonoaugustine.wargames.korge.ui.Corners.TOP_LEFT
import com.jonoaugustine.wargames.korge.ui.Corners.TOP_RIGHT
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.view.Container
import korlibs.korge.view.position
import korlibs.math.annotations.ViewDslMarker
import korlibs.math.geom.Point

/** corner button margin */
const val margin = 20

inline fun Container.button(
  label: String,
  textSize: Float = style.value.text.medium,
  block: @ViewDslMarker UIButton.() -> Unit = {}
): UIButton = uiButton(label, sizeFromTextSize(label, textSize)) {
  this.textSize = textSize
  background.shadowRadius = 0f
  block()
}

inline fun Container.primaryButton(
  label: String,
  textSize: Float = style.value.text.medium,
  block: @ViewDslMarker UIButton.() -> Unit = {}
) = button(label, textSize) {
  textColor = style.value.colors.white
  bgColorOut = style.value.colors.primaryDark
  bgColorOver = style.value.colors.primary
  block()
}

inline fun Container.secondaryButton(
  label: String,
  textSize: Float = style.value.text.medium,
  block: @ViewDslMarker UIButton.() -> Unit = {}
) = button(label, textSize) {
  textColor = style.value.colors.white
  bgColorOut = style.value.colors.secondaryDark
  bgColorOver = style.value.colors.secondary
  block()
}

inline fun Container.cornerButton(
  label: String,
  corner: Corners = TOP_LEFT,
  textSize: Float = style.value.text.medium,
  block: @ViewDslMarker UIButton.() -> Unit = {}
): UIButton = button(label, textSize) {
  position(
    when (corner) {
      TOP_LEFT     -> Point(margin, margin)
      TOP_RIGHT    -> Point(this@cornerButton.width - size.width - margin, margin)
      BOTTOM_LEFT  -> Point(margin, this@cornerButton.height - size.height - margin)
      BOTTOM_RIGHT -> Point(
        this@cornerButton.width - size.width - 10,
        this@cornerButton.height - size.height - 10
      )
    }
  )
  textColor = style.value.colors.white
  bgColorOut = style.value.colors.secondaryDark
  bgColorOver = style.value.colors.secondary
  block()
}

inline fun Container.backButton(
  label: String,
  textSize: Float = style.value.text.medium,
  block: @ViewDslMarker (UIButton.() -> Unit)
): UIButton = cornerButton(label, TOP_LEFT, textSize, block)
