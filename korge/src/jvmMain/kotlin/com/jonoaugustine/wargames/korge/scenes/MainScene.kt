package com.jonoaugustine.wargames.korge.scenes

import com.jonoaugustine.wargames.korge.SocketManager
import com.jonoaugustine.wargames.korge.virtualSize
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.scene.Scene
import korlibs.korge.ui.uiButton
import korlibs.korge.view.SContainer
import korlibs.korge.view.align.alignTopToBottomOf
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.align.centerXOnStage
import korlibs.korge.view.positionY
import korlibs.korge.view.text
import korlibs.math.geom.Size

object MainScene : Scene() {

  override suspend fun SContainer.sceneInit() {
    val centerText = text("Welcome", 64f) {
      centerOnStage()
      positionY(virtualSize.height / 3)
    }

    uiButton("Create Lobby", Size(310, 70)) {
      alignTopToBottomOf(centerText, 10)
      centerXOnStage()
      textSize = 52f
      textColor = Colors.GHOSTWHITE
      bgColorOut = Colors.DARKGREEN
      bgColorOver = Colors.FORESTGREEN
      background.shadowRadius = 0f
      onClick {
        sceneContainer.changeTo { LobbyScene }
      }
    }

    uiButton("Reconnect") {
      onClick {
        SocketManager.connectAs("user123")
      }
    }
  }
}
