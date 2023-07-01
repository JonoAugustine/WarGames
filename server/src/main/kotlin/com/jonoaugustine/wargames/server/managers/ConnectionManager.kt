package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.common.UserID
import com.jonoaugustine.wargames.common.network.missives.*
import io.ktor.server.websocket.WebSocketServerSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random
import kotlin.random.nextUInt

typealias ActionResponse = Pair<Event, Set<UserID>>

data class Connection(val user: User, val session: WebSocketServerSession)

val Connection.id get() = user.id
private var connections: Map<UserID, Connection> = mapOf()
private val mutex = Mutex()

suspend fun getConnection(id: String): Connection? = mutex.withLock { connections[id] }

suspend fun Connection.save() = mutex.withLock { connections += (id to this) }

/** Returns an existing [Connection] with the given [id] or a new [Connection] */
suspend fun WebSocketServerSession.getConnectionOrNew(
  id: String,
  name: String
): Connection =
  getConnection(id)
    ?.copy(session = this)
    ?.also { it.save() }
    ?.also { println("CONNECT(RENEW): ${it.id}") }
    ?: Connection(User(Random.nextUInt().toString(), name), this)
      .also { mutex.withLock { connections += (it.id to it) } }
      .also { println("CONNECT(NEW): ${it.user.id}") }

suspend fun WebSocketServerSession.onClose(uid: String) {
  // val connection = mutex.withLock { connections[uid] } ?: return
  // mutex.withLock { connections = connections.filterValues { it.id != uid } }
  // TODO("handle player leaving match")
}

suspend fun Connection.handleAction(action: Action): ActionResponse? =
  when (action) {
    is UserAction  -> handleUserAction(action)
    is LobbyAction -> handleLobbyAction(action)
    is MatchAction -> handleMatchAction(action)
    is WorldAction -> getMatchOf(user)
      .let { it ?: return errorEventOf("match not found") }
      .let { (id to action) queueTo it.id }
      .takeIf { it.isFailure }
      ?.also { println("failed to queue action") }
      .let { null }
  }

suspend fun Connection.handleUserAction(action: UserAction): ActionResponse =
  when (action) {
    is UpdateUsername -> this.copy(user = this.user.copy(name = action.name))
      .also { it.save() }
      .let { UserUpdated(it.user) } to setOf(id)
  }

context(Connection)
fun errorEventOf(
  message: String = "an unknown error occurred",
  vararg ids: UserID = arrayOf(id)
): Pair<ErrorEvent, Set<UserID>> = ErrorEvent(message) to setOf(*ids)
