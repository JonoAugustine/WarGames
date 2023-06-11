package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.common.UserID
import com.jonoaugustine.wargames.common.network.missives.*
import io.ktor.server.websocket.WebSocketServerSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

data class Connection(val user: User, val session: WebSocketServerSession)

val Connection.id get() = user.id
private var connections: Map<UserID, Connection> = mapOf()
private val mutex = Mutex()

suspend fun getConnection(id: String): Connection? = mutex.withLock { connections[id] }

suspend fun Connection.save() = mutex.withLock { connections -= id }

/** Returns an existing [Connection] with the given [id] or a new [Connection] */
suspend fun WebSocketServerSession.getConnectionOrNew(
  id: String,
  name: String
): Connection =
  getConnection(id)
    ?.also { println("reestablished connection ${it.id}") }
    ?: Connection(User(UUID.randomUUID().toString(), name), this)
      .also { mutex.withLock { connections += (it.id to it) } }
      .also { println("new connection: ${it.user.id}") }

suspend fun WebSocketServerSession.onClose(uid: String) {
  // val connection = mutex.withLock { connections[uid] } ?: return
  // mutex.withLock { connections = connections.filterValues { it.id != uid } }
  // TODO("handle player leaving match")
}
typealias ActionResponse = Pair<Event, Set<UserID>>

suspend fun Connection.handleAction(action: Action): ActionResponse? =
  when (action) {
    is UserAction  -> handleUserAction(action)
    is LobbyAction -> handleLobbyAction(action)
    is MatchAction -> handleMatchAction(action)
  }

suspend fun Connection.handleUserAction(action: UserAction): ActionResponse =
  when (action) {
    is UpdateUsername -> this.copy(user = this.user.copy(name = action.name))
      .also { it.save() }
      .let { UserUpdated(it.user) } to setOf(id)
  }

