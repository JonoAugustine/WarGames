package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.LobbyPreview
import com.jonoaugustine.wargames.common.network.JsonConfig
import com.jonoaugustine.wargames.server.managers.allLobbies
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.basic
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import org.slf4j.event.Level
import java.time.Duration

val PORT: Int? = System.getenv("PORT")?.toInt()

// TODO move to action queue
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
      validate { (uid, name) ->
        if (uid == name) UserIdPrincipal(uid)
        else null
      }
    }
  }
  install(WebSockets) {
    contentConverter = KotlinxWebsocketSerializationConverter(JsonConfig)
    pingPeriod = Duration.ofSeconds(15)
    timeout = Duration.ofSeconds(90)
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
