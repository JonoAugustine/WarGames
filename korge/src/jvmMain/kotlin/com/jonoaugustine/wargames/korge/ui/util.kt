package com.jonoaugustine.wargames.korge.ui

import korlibs.math.geom.Size

fun sizeFromTextSize(text: String, textSize: Float) =
  Size(textSize * text.length * 0.6f, textSize * 1.3f)
