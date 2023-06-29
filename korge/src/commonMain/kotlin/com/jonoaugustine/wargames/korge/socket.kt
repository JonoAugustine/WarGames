package com.jonoaugustine.wargames.korge

import com.jonoaugustine.wargames.common.EventBus
import com.jonoaugustine.wargames.common.network.JsonConfig
import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.common.network.missives.Event
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame.Close
import io.ktor.websocket.Frame.Text
import io.ktor.websocket.readText
import io.ktor.websocket.send
import korlibs.io.async.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.encodeToString

private val client = HttpClient {
  //install()
  install(WebSockets) {
    contentConverter = KotlinxWebsocketSerializationConverter(JsonConfig)
    pingInterval = 1000L * 15
  }
  install(ContentNegotiation) { json(JsonConfig) }
  defaultRequest {
    host = "localhost"
    port = 8080
  }
}

val SocketEventBus = EventBus()

suspend fun connectSocket(username: String): ClientWebSocketSession? {
  val socket: ClientWebSocketSession
  val connection = runCatching {
    client.webSocketSession(HttpMethod.Get, "localhost", 8080, "/") {
      header("wg.id", username) // TODO remove these when auth is made real
      header("wg.name", username)
      basicAuth(username, username) // TODO send saved name and password
    }
  }

  socket = if (connection.isSuccess) connection.getOrThrow()
  else {
    connection.exceptionOrNull()?.printStackTrace() // TODO remove stacktrace print
    return null
  }

  launch(Dispatchers.IO) {
    val handlerResult = socket.runCatching {
      incoming.consumeEach { frame ->
        when (frame) {
          is Text  -> frame.readEvent()
            ?.let { SocketEventBus.announce(it) }
            ?: println("failed to process frame $frame")

          is Close -> println("SOCKET: closed")
          else     -> println("SOCKET: received unhandled frame type ${frame.frameType}")
        }
      }
    }

    if (handlerResult.isFailure) println("socket disconnected")
    else println("socket closed")
  }

  return socket
}

private fun Text.readEvent(): Event? =
  readText().runCatching { JsonConfig.decodeFromString<Event>(this) }
    .also { it.exceptionOrNull()?.printStackTrace() }
    .getOrNull()

suspend fun DefaultClientWebSocketSession.send(action: Action) =
  println("SENDING: $action")
    .also { send(JsonConfig.encodeToString<Action>(action)) }


