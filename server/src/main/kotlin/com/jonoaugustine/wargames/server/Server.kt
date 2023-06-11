package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.JsonConfig
import com.jonoaugustine.wargames.common.LobbyPreview
import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.common.network.missives.ErrorEvent
import com.jonoaugustine.wargames.common.network.missives.UserConnected
import com.jonoaugustine.wargames.server.managers.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.CloseReason.Codes.CANNOT_ACCEPT
import io.ktor.websocket.CloseReason.Codes.INTERNAL_ERROR
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.event.Level
import java.time.Duration

val PORT: Int? = System.getenv("PORT")?.toInt()

fun main() {
  embeddedServer(
    CIO,
    port = PORT ?: 8080,
    watchPaths = listOf("classes"),
    module = Application::configuration,
  ).start(true)
}

fun Application.configuration() {
  install(CallLogging) { level = Level.DEBUG }
  install(ContentNegotiation) { json(JsonConfig) }
  install(CORS) {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)
    allowHeader(HttpHeaders.Authorization)
    anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
  }
  authentication {
    basic("basic") {
      realm = "websocket"
      validate { (name, pass) ->
        if (name == pass) UserIdPrincipal(name)
        else null
      }
    }
  }
  install(WebSockets) {
    contentConverter = KotlinxWebsocketSerializationConverter(JsonConfig)
    pingPeriod = Duration.ofSeconds(15)
    timeout = Duration.ofSeconds(15)
    maxFrameSize = Long.MAX_VALUE
    masking = PORT !== null
  }
  routing {
    ApiRoutes()
    WebsocketConfiguration()
  }
}

context(Application)
fun Routing.ApiRoutes() {
  get("api/lobbies") {
    call.respond(allLobbies().map { LobbyPreview(it.id, it.name, it.players.size) })
  }
}

context (Application)
fun Routing.WebsocketConfiguration() = authenticate("basic") {
  webSocket {
    //val idPrincipal = call.principal<UserIdPrincipal>()
    val headerName = call.request.header("wg.name")
      ?: return@webSocket close(CloseReason(CANNOT_ACCEPT, "missing header wg.name"))
    val headerUid = call.request.header("wg.id")
      ?: return@webSocket close(CloseReason(CANNOT_ACCEPT, "missing header wg.id"))
    val con = getConnectionOrNew(headerUid, headerName)
    send(UserConnected(con.user))

    try {
      incoming.consumeEach { frame ->
        val connection = getConnection(con.user.id)
          ?: return@webSocket close(CloseReason(INTERNAL_ERROR, "missing connection"))
        when (frame) {
          is Frame.Text -> frame.readText()
            .runCatching { JsonConfig.decodeFromString<Action>(this) }
            .getOrElse {
              it.printStackTrace()
              send(ErrorEvent("internal error"))
              null
            }
            ?.also { println("RECEIVED: $it") }
            ?.let { connection.handleAction(it) }
            ?.let { send(it) }
            ?.also { println("SEND: $it") }

          is Frame.Close -> onClose(connection.id)
          else -> println("unregistered frame")
        }
      }
    } catch (e: ClosedReceiveChannelException) {
      println("session closed gracefully")
      e.printStackTrace()
    } catch (e: Throwable) {
      println("session closed with error")
      e.printStackTrace()
    } finally {
      onClose(headerUid)
      println(
        buildString {
          appendLine("connection closed: $headerUid")
          closeReason.await()
            ?.also { appendLine("close reason: ${it.knownReason}") }
            ?.also { appendLine("close message: ${it.message}") }
        }
      )
    }
  }
}

