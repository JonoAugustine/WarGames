package com.jonoaugustine.wargames.korge.scenes

import com.jonoaugustine.wargames.common.network.missives.CreateLobby
import com.jonoaugustine.wargames.korge.SaveData
import com.jonoaugustine.wargames.korge.SocketManager
import com.jonoaugustine.wargames.korge.ui.Corners.BOTTOM_RIGHT
import com.jonoaugustine.wargames.korge.ui.Corners.TOP_RIGHT
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

    val name = injector.get<SaveData>().username
    cornerButton(name, TOP_RIGHT)

    val centerText = text("Welcome", 64f) {
      centerOnStage()
      positionY(virtualSize.height / 3)
    }

    primaryButton("Create Lobby", 52f) {
      alignTopToBottomOf(centerText, 10)
      centerXOnStage()
      onClick { SocketManager.send(CreateLobby) }
    }

    cornerButton("Logout", BOTTOM_RIGHT) {
      onClick {
        // TODO Logout
        sceneContainer.changeTo<LoginScene>()
      }
    }
  }
}
