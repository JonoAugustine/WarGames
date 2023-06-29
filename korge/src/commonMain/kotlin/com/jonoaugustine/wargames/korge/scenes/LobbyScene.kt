package com.jonoaugustine.wargames.korge.scenes

import io.ktor.client.plugins.websocket.ClientWebSocketSession
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.scene.Scene
import korlibs.korge.ui.uiButton
import korlibs.korge.view.SContainer
import korlibs.korge.view.position

class LobbyScene(private val session: ClientWebSocketSession) : Scene() {

  override suspend fun SContainer.sceneInit() {
    uiButton("Main Menu") {
      textColor = Colors["#333333"]
      bgColorOut = Colors.GHOSTWHITE
      bgColorOver = Colors.WHITESMOKE
      position(10, 10)
      onClick { sceneContainer.changeTo { MainScene(session) } }
    }
  }
}
