package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.JsonConfig
import com.jonoaugustine.wargames.common.network.Action
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.CloseReason.Codes
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
    module = Application::configuration
  )
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
  install(WebSockets) {
    contentConverter = KotlinxWebsocketSerializationConverter(JsonConfig)
    pingPeriod = Duration.ofSeconds(15)
    timeout = Duration.ofSeconds(15)
    maxFrameSize = Long.MAX_VALUE
    masking = PORT !== null
  }
  WebsocketConfiguration()
}

fun Application.WebsocketConfiguration() = routing {
  webSocket {
    val userID: String = this.call.request.queryParameters["name"]
      ?.let { connectionFrom(it) }
      ?.user?.id
      ?: return@webSocket this.close(CloseReason(CANNOT_ACCEPT, "missing name"))

    try {
      incoming.consumeEach { frame ->
        val connection = getConnection(userID)
          ?: return@webSocket close(CloseReason(INTERNAL_ERROR, "missing connection"))
        when (frame) {
          is Frame.Text  -> frame.readText()
            .runCatching { JsonConfig.decodeFromString<Action>(this) }
            .getOrNull()
            ?.let { TODO("handle incoming action") }
            ?: TODO("send error event back to client")

          is Frame.Close -> TODO("handle closing frame")
          else           -> println("unregistered frame")
        }
      }
    } catch (e: ClosedReceiveChannelException) {
      println("session closed")
      TODO("remove connection instance")
    } catch (e: Throwable) {
      TODO("remove connection instance")
      TODO("handle error")
    }
  }
}
