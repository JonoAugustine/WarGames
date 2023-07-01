package com.jonoaugustine.wargames.korge.scenes

import com.jonoaugustine.wargames.korge.*
import com.jonoaugustine.wargames.korge.ui.Corners.BOTTOM_RIGHT
import com.jonoaugustine.wargames.korge.ui.cornerButton
import com.jonoaugustine.wargames.korge.ui.primaryButton
import com.jonoaugustine.wargames.korge.ui.sizeFromTextSize
import com.jonoaugustine.wargames.korge.ui.style
import korlibs.korge.annotations.KorgeExperimental
import korlibs.korge.input.onClick
import korlibs.korge.scene.Scene
import korlibs.korge.service.storage.storage
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UITextInput
import korlibs.korge.ui.uiTextInput
import korlibs.korge.view.SContainer
import korlibs.korge.view.align.alignTopToBottomOf
import korlibs.korge.view.align.centerOnStage
import korlibs.korge.view.align.centerXOnStage
import korlibs.korge.view.positionY
import korlibs.korge.view.text
import kotlin.random.Random.Default.nextInt
import kotlin.time.Duration.Companion.seconds

@OptIn(KorgeExperimental::class)
class LoginScene : Scene() {

  private lateinit var username: UITextInput
  private lateinit var login: UIButton

  private suspend fun login() {
    if (SocketManager.connectAs(username.text)) {
      storage[SaveData.storeKey] = (injector.getOrNull<SaveData>()
        ?.copy(username = username.text, password = username.text)
        ?: SaveData(0u, username = username.text, password = username.text))
        .toJson()
      sceneContainer.changeTo<MainScene>()
    } else {
      login.bgColorOver = style.value.colors.negative
      kotlinx.coroutines.delay(2.seconds)
      login.bgColorOver = style.value.colors.primary
    }
  }

  @OptIn(KorgeExperimental::class)
  override suspend fun SContainer.sceneInit() {
    val centerText = text("Login", 64f) {
      centerOnStage()
      positionY(virtualSize.height / 3)
    }

    val name = injector.getOrNull<SaveData>()?.username ?: "NewPlayer${nextInt()}"

    username = uiTextInput(
      name,
      sizeFromTextSize(" ".repeat(30), style.value.text.medium)
    ) {
      alignTopToBottomOf(centerText, 10)
      centerXOnStage()
      textSize = style.value.text.medium
    }

    primaryButton("Login", 52f) {
      alignTopToBottomOf(username, 10)
      centerXOnStage()
      onClick { login() }
    }

    cornerButton("Exit Game", BOTTOM_RIGHT) {
      bgColorOut = style.value.colors.negative
      bgColorOver = style.value.colors.negative
      onClick { gameWindow.close(0) }
    }
  }
}
