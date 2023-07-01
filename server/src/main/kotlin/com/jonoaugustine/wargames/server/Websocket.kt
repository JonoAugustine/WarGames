package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.network.JsonConfig
import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.common.network.missives.ErrorEvent
import com.jonoaugustine.wargames.common.network.missives.UserConnected
import com.jonoaugustine.wargames.server.managers.*
import io.ktor.server.application.Application
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.header
import io.ktor.server.routing.Routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.util.logging.error
import io.ktor.websocket.CloseReason
import io.ktor.websocket.CloseReason.Codes.CANNOT_ACCEPT
import io.ktor.websocket.CloseReason.Codes.INTERNAL_ERROR
import io.ktor.websocket.Frame.Close
import io.ktor.websocket.Frame.Text
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.isActive
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Websocket")

context (Application)
fun Routing.WebsocketConfiguration() = authenticate("basic") {
  webSocket {
    // onConnect
    val (uid) = call.principal<UserIdPrincipal>()
      ?: return@webSocket send(ErrorEvent("authentication"))
    val headerUsername = call.request.header("wg.name")
    val con = getConnection(uid)
      ?.refresh(this)
      ?: headerUsername?.let { newConnection(it) }
      ?: return@webSocket close(CloseReason(CANNOT_ACCEPT, "missing header wg.name"))

    logger.info("new connection ${con.id}")

    send(UserConnected(con.user))

    // onMessage
    frameHandler(con)
      .exceptionOrNull()
      ?.takeUnless { it is ClosedReceiveChannelException }
      ?.let { logger.error(it) }

    onClose(uid)
    logger.info(
      buildString {
        appendLine("connection closed: $uid")
        closeReason.await()
          ?.also { appendLine("close reason: ${it.knownReason}") }
          ?.also { appendLine("close message: ${it.message}") }
      }
    )
  }
}

private suspend fun DefaultWebSocketServerSession.frameHandler(con: Connection) =
  runCatching {
    incoming.consumeEach { frame ->
      val connection = getConnection(con.user.id)
        ?: return@runCatching close(CloseReason(INTERNAL_ERROR, "missing connection"))

      when (frame) {
        is Text -> frame.readText()
          .runCatching { JsonConfig.decodeFromString<Action>(this) }
          .getOrElse {
            logger.error("Failed to decode text frame")
            it.printStackTrace()
            send(ErrorEvent("internal error"))
            null
          }
          ?.also { logger.debug("RECEIVED: {}", it) }
          ?.let { connection.handleAction(it) }
          ?.also { logger.debug("SEND: {}", it.first::class.simpleName) }
          ?.let { (e, targets) -> e to targets.mapNotNull { getConnection(it) } }
          ?.let { (e, targets) -> e to targets.filter { it.session.isActive } }
          ?.let { (event, targets) -> targets.forEach { it.session.send(event) } }

        is Close -> onClose(connection.id)
        else -> logger.error("unregistered frame {}", frame)
      }
    }
  }
