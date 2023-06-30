package com.jonoaugustine.wargames.korge

import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.common.network.missives.DisconnectEvent
import com.jonoaugustine.wargames.korge.scenes.LobbyScene
import com.jonoaugustine.wargames.korge.scenes.LoginScene
import com.jonoaugustine.wargames.korge.scenes.MainScene
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.math.geom.Size
import kotlinx.serialization.Serializable

val virtualSize = Size(1920, 1080)

@Serializable
data class SaveData(val user: User, val password: String)

suspend fun main() = Korge(
  windowSize = Size(1280, 720),
  virtualSize = virtualSize,
  bgcolor = Colors["#333333"]
) {
  //views.storage["save"] =
  //  JsonConfig.encodeToString(SaveData(User("1", "user"), "password"))

  val sceneContainer = sceneContainer()
  injector
    .mapSingleton { LoginScene() }
    .mapSingleton { MainScene() }
    .mapSingleton { LobbyScene() }

  SocketManager.sceneContainer = sceneContainer
  sceneContainer.navigationEntries

  SocketManager.bus<DisconnectEvent> {
    sceneContainer.changeTo<LoginScene>()
    SocketManager.connectAs(state.username)
  }

  sceneContainer.changeTo<LoginScene>()
}

