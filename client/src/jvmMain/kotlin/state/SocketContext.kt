package state

import Eventbus
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.jonoaugustine.wargames.common.JsonConfig
import com.jonoaugustine.wargames.common.network.Action
import com.jonoaugustine.wargames.common.network.Event
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import ui.screens.LoadingScreen

private val client = HttpClient {
  install(WebSockets) {
    contentConverter = KotlinxWebsocketSerializationConverter(JsonConfig)
    pingInterval = 1000L * 15
  }
}

/**
 * Hoisted [websocket](DefaultClientWebSocketSession) state wrapper
 */
context(AppState)
@Composable
fun SocketContext(content: @Composable DefaultClientWebSocketSession.() -> Unit) {
  var socket: DefaultClientWebSocketSession? by remember { mutableStateOf(null) }

  LaunchedEffect(socket?.isActive) {
    client.webSocket(
      HttpMethod.Get,
      "localhost",
      8080,
      request = { /* TODO send saved name */ }
    ) {
      socket = this
      incoming.consumeEach { frame ->
        when (frame) {
          is Frame.Text -> frame.readEvent()
            .also { println("incoming event $it") }
            ?.let { Eventbus.announce(it) }
            ?: println("failed to process frame $frame")

          else          -> println("received unhandled frame type ${frame.frameType}")
        }
      }

    }
  }

  if (socket === null) LoadingScreen("Waiting for socket connections")
  else content(socket!!)
}

fun Frame.Text.readEvent(): Event? = readText()
  .runCatching { JsonConfig.decodeFromString<Event>(this) }
  .also { it.exceptionOrNull()?.printStackTrace() }
  .getOrNull()

suspend fun DefaultClientWebSocketSession.send(action: Action) =
  send(JsonConfig.encodeToString<Action>(action))


