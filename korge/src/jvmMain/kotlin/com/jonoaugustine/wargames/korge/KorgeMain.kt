package com.jonoaugustine.wargames.korge

import com.jonoaugustine.wargames.common.network.JsonConfig
import com.jonoaugustine.wargames.common.network.missives.DisconnectEvent
import com.jonoaugustine.wargames.common.network.missives.LobbyCreated
import com.jonoaugustine.wargames.common.network.missives.UserConnected
import com.jonoaugustine.wargames.korge.scenes.LobbyScene
import com.jonoaugustine.wargames.korge.scenes.LoginScene
import com.jonoaugustine.wargames.korge.scenes.MainScene
import com.jonoaugustine.wargames.korge.scenes.WorldScene
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.SceneContainer
import korlibs.korge.scene.sceneContainer
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Stage
import korlibs.math.geom.Size
import org.slf4j.LoggerFactory

val virtualSize = Size(1920, 1080)

suspend fun main() = Korge(
  windowSize = Size(1280, 720),
  virtualSize = virtualSize,
  bgcolor = Colors["#333333"]
) {
  val logger = LoggerFactory.getLogger("WarGames")
  injector.mapInstance(logger)

  // Load Save Data
  (storage.getOrNull(SaveData.storeKey)
    ?.runCatching { JsonConfig.decodeFromString<SaveData>(this) }
    ?.takeIf { it.isSuccess }
    ?.getOrThrow()
    ?.also { logger.info("loaded save data $it") }
    ?: SaveData(0u, "LocalUser1", "")
      .also { logger.info("no save data loaded") })
    .let { injector.mapInstance(it) }

  // Map Scenes
  val sceneContainer = sceneContainer()
  injector
    .mapSingleton { LoginScene() }
    .mapSingleton { MainScene() }
    .mapPrototype { LobbyScene(get()) }
    .mapPrototype { WorldScene(get()) }

  eventHandlers(sceneContainer)

  // Opening Scene
  sceneContainer.changeTo<LoginScene>()
}

private fun Stage.eventHandlers(sceneContainer: SceneContainer) {
  SocketManager.bus<DisconnectEvent> {
    sceneContainer.changeTo<LoginScene>()
    SocketManager.connectAs(state.username)
  }

  SocketManager.bus<UserConnected> { (user) ->
    storage[SaveData.storeKey] = (injector.getOrNull<SaveData>()
      ?.copy(id = user.id.toUInt(), username = user.name, password = user.name)
      ?: SaveData(user.id.toUInt(), user.name, user.name))
      .toJson()
  }

  SocketManager.bus<LobbyCreated> {
    injector.mapInstance(it.lobby)
    sceneContainer.changeTo<LobbyScene>()
  }
}

