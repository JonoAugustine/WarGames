package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.Lobby
import com.jonoaugustine.wargames.common.UserID
import com.jonoaugustine.wargames.common.network.JsonConfig
import com.jonoaugustine.wargames.common.network.missives.ErrorEvent
import com.jonoaugustine.wargames.common.network.missives.Event
import com.jonoaugustine.wargames.server.managers.Connection
import com.jonoaugustine.wargames.server.managers.id
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString

typealias ActionResponse = Pair<Event, Set<UserID>>

suspend fun WebSocketServerSession.send(event: Event) =
  send(JsonConfig.encodeToString<Event>(event))

fun errorEvent(
  message: String = "an unknown error occurred",
  vararg ids: UserID
): ActionResponse = ErrorEvent(message) to setOf(*ids)

fun Connection.errorEvent(
  message: String = "an unknown error occurred",
  vararg ids: UserID = arrayOf(id)
): ActionResponse = errorEvent(message, *ids)

fun Lobby.responseOf(event: Event): ActionResponse = event to players.keys
