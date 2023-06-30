package com.jonoaugustine.wargames.korge.scenes

import com.jonoaugustine.wargames.korge.SocketManager
import com.jonoaugustine.wargames.korge.virtualSize
import korlibs.image.color.Colors
import korlibs.korge.annotations.KorgeExperimental
import korlibs.korge.input.onClick
import korlibs.korge.scene.Scene
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UITextInput
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiTextInput
import korlibs.korge.view.SContainer
import korlibs.korge.view.align.alignTopToBottomOf
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.align.centerXOnStage
import korlibs.korge.view.positionY
import korlibs.korge.view.text
import korlibs.math.geom.Size
import kotlin.time.Duration.Companion.seconds

object LoginScene : Scene() {

  @OptIn(KorgeExperimental::class)
  private lateinit var username: UITextInput
  private lateinit var login: UIButton

  @OptIn(KorgeExperimental::class)
  override suspend fun SContainer.sceneInit() {
    val centerText = text("Login", 64f) {
      centerOnStage()
      positionY(virtualSize.height / 3)
    }

    username = uiTextInput("User1234", Size(370, 60)) {
      alignTopToBottomOf(centerText, 10)
      centerXOnStage()
      textSize = 52f
      colorMul = Colors.ORANGE
    }

    uiButton("Login", Size(310, 70)) {
      alignTopToBottomOf(username, 10)
      centerXOnStage()
      textSize = 52f
      textColor = Colors.GHOSTWHITE
      bgColorOut = Colors.DARKGREEN
      bgColorOver = Colors.FORESTGREEN
      background.shadowRadius = 0f
      onClick {
        if (SocketManager.connectAs(username.text)) {
          sceneContainer.changeTo { MainScene }
        } else {
          login.bgColorOver = Colors.RED
          kotlinx.coroutines.delay(2.seconds)
          login.bgColorOver = Colors.FORESTGREEN
        }
      }
    }
  }
}
