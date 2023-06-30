package com.jonoaugustine.wargames.korge.scenes

import com.jonoaugustine.wargames.korge.ui.backButton
import korlibs.korge.input.onClick
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer

class LobbyScene : Scene() {

  override suspend fun SContainer.sceneInit() {
    backButton("Main Menu") {
      onClick { sceneContainer.changeTo<MainScene>() }
    }
  }
}
