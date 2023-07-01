package com.jonoaugustine.wargames.korge

import com.jonoaugustine.wargames.common.EventBus
import com.jonoaugustine.wargames.common.network.JsonConfig
import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.common.network.missives.DisconnectEvent
import com.jonoaugustine.wargames.common.network.missives.Event
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.ClientWebSocketSession
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import org.slf4j.LoggerFactory.getLogger
import kotlin.time.Duration.Companion.seconds

object SocketManager {

  private val logger = getLogger(SocketManager::class.simpleName)
  private const val maxConnectionAttempts = 1
  private var reconnections: Int = 0
  private var session: ClientWebSocketSession? = null
  private val client = HttpClient {
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

  val bus = EventBus()

  init {
    @OptIn(DelicateCoroutinesApi::class)
    with(GlobalScope) {
      bus<Event> { logger.debug(it.toString()) }
    }
  }

  suspend fun send(action: Action) = session?.send(action)

  suspend fun connectAs(username: String): Boolean {
    logger.info("connecting to server $reconnections")
    reconnections++

    with(connect(username)) {
      session = if (isSuccess) getOrThrow()
      else {
        logger.error("failed to connect to server")
        exceptionOrNull()?.printStackTrace() // TODO remove stacktrace print
        return false
      }
    }

    launch(Dispatchers.IO) {
      logger.debug("starting socket session handler")
      if (handler(session!!).isFailure) logger.error("socket crashed")
      else logger.info("socket closed")
      if (reconnections++ >= maxConnectionAttempts) {
        bus.announce(DisconnectEvent)
        reconnections = 0
        return@launch
      }
      delay(3.seconds)
      connectAs(username)
    }

    return session != null
  }

  private suspend fun connect(username: String) = runCatching {
    client.webSocketSession(HttpMethod.Get, "localhost", 8080, "/") {
      header("wg.id", username) // TODO remove these when auth is made real
      header("wg.name", username)
      basicAuth(username, username) // TODO send saved name and password
    }
  }

  private suspend fun handler(session: ClientWebSocketSession) = session.runCatching {
    incoming.consumeEach { frame ->
      when (frame) {
        is Text  -> frame.readEvent()
          ?.let { bus.announce(it) }
          ?: println("failed to process frame $frame")

        is Close -> println("SOCKET: closed")
        else     -> println("SOCKET: received unhandled frame type ${frame.frameType}")
      }
    }
  }

  private fun Text.readEvent(): Event? =
    readText().runCatching { JsonConfig.decodeFromString<Event>(this) }
      .also { it.exceptionOrNull()?.printStackTrace() }
      .getOrNull()

  private suspend fun ClientWebSocketSession.send(action: Action) =
    println("SENDING: $action")
      .also { send(JsonConfig.encodeToString<Action>(action)) }
}

