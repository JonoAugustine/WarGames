package com.jonoaugustine.wargames.korge.scenes

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.jonoaugustine.wargames.common.network.missives.WorldUpdated
import com.jonoaugustine.wargames.korge.SocketManager
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.addFixedUpdater
import korlibs.korge.view.solidRect
import korlibs.time.timesPerSecond

class WorldScene(initWorld: World) : Scene() {

  private var world = initWorld

  override suspend fun SContainer.sceneInit() {

    SocketManager.bus<WorldUpdated> { (snapshot) ->
      injector.mapInstance(snapshot)
      @Suppress("UNCHECKED_CAST")
      world.loadSnapshot(snapshot as Map<Entity, List<Component<*>>>)
      sceneContainer.changeTo<WorldScene>()
    }

    solidRect(10, 10)

    addFixedUpdater(60.timesPerSecond) {
      world.update(1 / 60f)
    }
  }
}
