package com.jonoaugustine.wargames.korge

import com.jonoaugustine.wargames.common.network.JsonConfig
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import korlibs.memory.Os
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class SaveData(val id: UInt, val username: String, val password: String)

val SaveData.Companion.storeKey get() = "savedata"
fun SaveData.toJson(): String = JsonConfig.encodeToString(this)

@Serializable
data class AppState(
  val username: String = Os.CURRENT.name,
  val socket: ClientWebSocketSession? = null
)

var state: AppState = AppState()
  private set

fun updateState(update: AppState.() -> Unit) {
  state = AppState().apply(update)
}

