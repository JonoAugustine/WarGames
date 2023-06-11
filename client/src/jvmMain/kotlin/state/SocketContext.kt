package state

import Eventbus
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jonoaugustine.wargames.common.JsonConfig
import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.common.network.missives.CreateMatch
import com.jonoaugustine.wargames.common.network.missives.Event
import com.jonoaugustine.wargames.common.network.missives.UpdateLobbyName
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.Frame.Close
import io.ktor.websocket.Frame.Text
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import state.AttemptState.ATTEMPTING
import state.AttemptState.DONE
import state.AttemptState.FAILED
import state.AttemptState.WAITING
import ui.screens.LoadingScreen
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.random.Random
import kotlin.random.Random.Default
import kotlin.time.Duration.Companion.seconds

// TODO move credentials class to a file manager or something
@Serializable
private data class Credentials(val id: String, val name: String)
private enum class AttemptState { ATTEMPTING, WAITING, DONE, FAILED }

private const val maxAttempts = 3
private const val retryDelay = 10
private val credentials =
  Path(System.getProperty("user.dir"), ".credentials.json")
    .readText()
    .let { JsonConfig.decodeFromString<Credentials>(it) }
    .let { it.copy(id = it.id + Random.nextBits(4), name = it.name + Random.nextBits(4)) }

/**
 * Hoisted [websocket](DefaultClientWebSocketSession) state wrapper
 *
 * TODO fix auto reconnecting bugs
 */
context(AppState)
@Composable
fun SocketContext(content: @Composable DefaultClientWebSocketSession.() -> Unit) {
  var socket: DefaultClientWebSocketSession? by remember { mutableStateOf(null) }
  var attempts by remember { mutableStateOf(0) }
  var attemptTimer by remember { mutableStateOf(retryDelay) }
  var attemptState by remember { mutableStateOf(ATTEMPTING) }

  LaunchedEffect(attempts) {
    if (socket != null) return@LaunchedEffect
    if (attempts > maxAttempts) {
      attemptState = FAILED
      return@LaunchedEffect
    }

    attemptState = ATTEMPTING
    val connectionResult = runCatching {
      client.webSocketSession(HttpMethod.Get, "localhost", 8080, "/") {
        header("wg.id", credentials.id)
        header("wg.name", credentials.name)
        basicAuth(credentials.id, credentials.id) // TODO send saved name and password
      }
    }
    if (connectionResult.isFailure) {
      connectionResult.exceptionOrNull()
        ?.printStackTrace() // TODO remove stacktrace print
      attemptState = WAITING
      attemptTimer = retryDelay
      while (attemptTimer > 0) {
        delay(1.seconds)
        attemptTimer -= 1
      }
      attempts += 1
      return@LaunchedEffect
    }
    attemptState = DONE
    socket = connectionResult.getOrThrow()
    val handlerResult = socket!!.runCatching {
      incoming.consumeEach { frame ->
        when (frame) {
          is Text  -> frame.readEvent()
            .also { println("SOCKET: $it") }
            ?.let { Eventbus.announce(it) }
            ?: println("failed to process frame $frame")

          is Close -> println("SOCKET: closed")
          else     -> println("SOCKET: received unhandled frame type ${frame.frameType}")
        }
      }
    }

    if (handlerResult.isFailure) println("socket disconnected")
    else println("socket closed")

    attemptState = FAILED
    socket = null
    attempts = 1
  }

  when (attemptState) {
    FAILED     -> LoadingScreen("Failed to connect to server after ${attempts + 1} attempts")
    WAITING    -> LoadingScreen("Failed to connect to server. Attempting again in ${attemptTimer}s")
    ATTEMPTING -> LoadingScreen("Waiting for socket connection (attempt ${attempts + 1})")
    DONE       -> content(socket!!)
  }
  if (attemptState == FAILED) Column(
    Modifier.fillMaxSize(0.98F),
    verticalArrangement = Arrangement.Bottom,
    horizontalAlignment = Alignment.End
  ) {
    Button(
      colors = buttonColors(Color.Red),
      onClick = { attempts = 0 },
      content = { Text("Reconnect") }
    )
  }
}

private fun Frame.Text.readEvent(): Event? =
  readText().runCatching { JsonConfig.decodeFromString<Event>(this) }
    .also { it.exceptionOrNull()?.printStackTrace() }
    .getOrNull()

suspend fun DefaultClientWebSocketSession.send(action: Action) =
  send(JsonConfig.encodeToString<Action>(action))


