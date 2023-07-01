package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.common.UserID
import com.jonoaugustine.wargames.common.network.missives.*
import com.jonoaugustine.wargames.common.userOf
import com.jonoaugustine.wargames.server.ActionResponse
import com.jonoaugustine.wargames.server.errorEvent
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.util.logging.error
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Connection Manager")
private var connections: Map<UserID, Connection> = mapOf()
private val mutex = Mutex()

data class Connection(val user: User, val session: WebSocketServerSession)

val Connection.id get() = user.id
val Connection.name get() = user.name

suspend fun Connection.save() = mutex.withLock { connections += (id to this) }

fun getConnection(id: UserID): Connection? = connections[id]

/** Update the [Connection.session] */
suspend fun Connection.refresh(session: WebSocketServerSession): Connection =
  copy(session = session)
    .apply { save() }
    .also { logger.debug("CONNECT(REFRESH): ${it.id}") }

suspend fun WebSocketServerSession.newConnection(username: String): Connection =
  Connection(userOf(username), this)
    .apply { save() }
    .also { logger.debug("CONNECT(NEW): ${it.id}") }

suspend fun Connection.handleAction(action: Action): ActionResponse? = when (action) {
  is UserAction  -> handleUserAction(action)
  is LobbyAction -> handleLobbyAction(action)
  is WorldAction -> getLobbyOf(user)
    .let { it ?: return errorEvent("lobby not found") }
    .let { (id to action) queueTo it.id }
    .takeIf { it.isFailure }
    ?.also { logger.error("failed to queue action") }
    ?.exceptionOrNull()
    ?.also { logger.error(it) }
    .let { null }
}

suspend fun Connection.handleUserAction(action: UserAction): ActionResponse =
  when (action) {
    is UpdateUsername -> this.copy(user = this.user.copy(name = action.name))
      .also { it.save() }
      .let { UserUpdated(it.user) } to setOf(id)
  }

suspend fun WebSocketServerSession.onClose(uid: UserID) {
  // val connection = mutex.withLock { connections[uid] } ?: return
  // mutex.withLock { connections = connections.filterValues { it.id != uid } }
  // TODO("handle player leaving match")
}
