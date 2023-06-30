package com.jonoaugustine.wargames.korge

import io.ktor.client.plugins.websocket.ClientWebSocketSession
import korlibs.memory.Os

data class AppState(
  val username: String = Os.CURRENT.name,
  val socket: ClientWebSocketSession? = null
)

var state: AppState = AppState()
  private set

fun updateState(update: AppState.() -> Unit) {
  state = AppState().apply(update)
}
