package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.network.missives.*
import com.jonoaugustine.wargames.server.send
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.websocket.send
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

suspend fun Connection.handleAction(action: Action): Event? = when (action) {
  is UserAction  -> handleUserAction(action)
  is LobbyAction -> handleLobbyAction(action)
  is MatchAction -> handleMatchAction(action)
}

suspend fun Connection.handleUserAction(action: UserAction): Event = when (action) {
  is UpdateUsername -> this.copy(user = this.user.copy(name = action.name))
    .also { it.save() }
    .let { UserUpdated(it.user) }
}

suspend fun Connection.handleLobbyAction(action: LobbyAction): Event? = when (action) {
  CreateLobby        -> getLobbyOf(user)
    ?.let { LobbyJoined(it.players[id]!!, it) }
    ?: newLobby(Player(user, WgColor.Red))
      .also { it.save() }
      .let { LobbyCreated(it) }

  is JoinLobby       -> getLobby(action.lobbyID)
    ?.takeUnless { it.players.containsKey(id) }
    ?.let { it to Player(user, WgColor.Blue) }
    ?.let { (lobby, player) -> lobby.addPlayer(player)?.to(player) }
    ?.also { (lobby, player) -> lobby.save() to player }
    ?.let { (lobby, player) -> LobbyJoined(player, lobby) }
    ?.also { event ->
      event.lobby.players.keys
        .filterNot { it == id }
        .mapNotNull { getConnection(it) }
        .forEach { it.session.send(event) }
    }
    ?: ErrorEvent("missing access")

  is UpdateLobbyName -> getLobbyOf(user)
    ?.takeIf { it.hostID == user.id }
    ?.copy(name = action.name)
    ?.also { it.save() }
    ?.let { LobbyUpdated(it) }
    ?: ErrorEvent("missing access")

  is CloseLobby      -> TODO()
}

suspend fun Connection.handleMatchAction(action: MatchAction): Event? = when (action) {
  is CreateMatch -> getLobbyOf(user)
    ?.let { newMatch(it) }
    ?.also { it.save() }
    ?.let { MatchCreated(it) }
    ?: ErrorEvent("lobby does not exist")

  else           -> getMatchOf(user)
    ?.let { handleMatchActionWithMatch(action, it) }
    ?: ErrorEvent("match does not exist")
}

fun Connection.handleMatchActionWithMatch(action: MatchAction, match: Match): Event? =
  when (action) {
    is PlaceEntity -> TODO()
    is JoinMatch   -> TODO()
    is Start       -> TODO()
    else           -> ErrorEvent()
  }
