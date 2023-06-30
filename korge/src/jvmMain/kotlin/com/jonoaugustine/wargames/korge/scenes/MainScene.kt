package com.jonoaugustine.wargames.korge.scenes

import com.jonoaugustine.wargames.korge.ui.Corners.BOTTOM_RIGHT
import com.jonoaugustine.wargames.korge.ui.cornerButton
import com.jonoaugustine.wargames.korge.ui.primaryButton
import com.jonoaugustine.wargames.korge.virtualSize
import korlibs.korge.input.onClick
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.align.alignTopToBottomOf
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.align.centerXOnStage
import korlibs.korge.view.positionY
import korlibs.korge.view.text

class MainScene : Scene() {

  override suspend fun SContainer.sceneInit() {
    val centerText = text("Welcome", 64f) {
      centerOnStage()
      positionY(virtualSize.height / 3)
    }

    primaryButton("Create Lobby", 52f) {
      alignTopToBottomOf(centerText, 10)
      centerXOnStage()
      onClick {
        sceneContainer.changeTo<LobbyScene>()
      }
    }

    cornerButton("Logout", BOTTOM_RIGHT) {
      onClick {
        // TODO Logout
        sceneContainer.changeTo<LoginScene>()
      }
    }
  }
}
