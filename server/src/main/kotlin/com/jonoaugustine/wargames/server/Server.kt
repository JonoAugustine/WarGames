package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.JsonConfig
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
import io.ktor.websocket.Frame
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

@Suppress("UNREACHABLE_CODE")
fun Application.WebsocketConfiguration() = routing {
  webSocket {
    val playerID = TODO("Generate new Connection with player ID")
    val result = runCatching {
      incoming.consumeEach { frame ->
        when (frame) {
          is Frame.Text  -> TODO("handle json frame")
          is Frame.Close -> TODO("handle closing frame")
          else           -> println("unregistered frame")
        }
      }
    }
    try {
    } catch (e: ClosedReceiveChannelException) {
      println("session closed")
      TODO("remove connection instance")
    } catch (e: Throwable) {
      TODO("remove connection instance")
      TODO("handle error")
    }
  }
}
