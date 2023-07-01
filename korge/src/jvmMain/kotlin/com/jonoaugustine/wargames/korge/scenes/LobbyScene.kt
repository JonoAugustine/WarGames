package com.jonoaugustine.wargames.korge.scenes

import com.jonoaugustine.wargames.common.Lobby
import com.jonoaugustine.wargames.common.network.missives.CreateMatch
import com.jonoaugustine.wargames.korge.SocketManager
import com.jonoaugustine.wargames.korge.ui.backButton
import com.jonoaugustine.wargames.korge.ui.primaryButton
import com.jonoaugustine.wargames.korge.ui.style
import com.jonoaugustine.wargames.korge.virtualSize
import korlibs.korge.annotations.KorgeExperimental
import korlibs.korge.input.onClick
import korlibs.korge.scene.Scene
import korlibs.korge.ui.tooltip
import korlibs.korge.ui.uiTooltipContainer
import korlibs.korge.view.SContainer
import korlibs.korge.view.align.centerXOnStage
import korlibs.korge.view.positionY
import korlibs.korge.view.text

class LobbyScene(private val lobby: Lobby) : Scene() {

  @OptIn(KorgeExperimental::class)
  override suspend fun SContainer.sceneInit() {
    backButton("Main Menu") {
      onClick { sceneContainer.changeTo<MainScene>() }
    }

    uiTooltipContainer {
      tooltip(it, lobby.id)
      text(lobby.name, style.value.text.large) {
        centerXOnStage()
        positionY(virtualSize.height / 4)
      }
    }

    primaryButton("Start Game") {
      onClick { SocketManager.send(CreateMatch(lobby.id)) }
    }
  }
}
