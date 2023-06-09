package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.JsonConfig
import com.jonoaugustine.wargames.common.network.Event
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString

suspend fun WebSocketServerSession.send(event: Event) =
  send(JsonConfig.encodeToString<Event>(event))
