package com.jonoaugustine.wargames.korge

import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.korge.scenes.LoginScene
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.math.geom.Size
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile

val virtualSize = Size(1920, 1080)

@Serializable
data class SaveData(val user: User, val password: String) {

  companion object {

    val PATH = Path("/saves/user.save")
      .apply { parent.createDirectories() }
      .apply { createFile() }
  }
}

suspend fun main() = Korge(
  windowSize = Size(1280, 720),
  virtualSize = virtualSize,
  bgcolor = Colors["#333333"],
) {
  val sceneContainer = sceneContainer()

  sceneContainer.changeTo { LoginScene }
}



