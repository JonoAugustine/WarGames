package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.network.missives.*
import io.ktor.server.websocket.WebSocketServerSession
import java.util.*
import java.util.Collections.synchronizedMap

data class Connection(val user: User, val session: WebSocketServerSession)

private val connections: MutableMap<String, Connection> = synchronizedMap(mutableMapOf())

private fun userOf(name: String): User =
  User(UUID.randomUUID().toString(), name)

/** Returns an existing [Connection] with the given [id] or a new [Connection] */
suspend fun WebSocketServerSession.fillConnection(id: String, name: String): Connection =
  getConnection(id)
    ?.also { println("reestablished connection ${it.user.id}") }
    ?: Connection(userOf(name), this)
      .also { synchronized(connections) { connections[it.user.id] = it } }
      .also { println("new connection: ${it.user.id}") }

suspend fun WebSocketServerSession.onClose(uid: String) {
  synchronized(connections) { connections.remove(uid) }
  // TODO("handle player leaving match")
}

fun getConnection(id: String): Connection? = synchronized(connections) { connections[id] }
fun setConnection(connection: Connection) = synchronized(connections) {
  connections[connection.user.id] = connection
}

fun Connection.handleAction(action: Action): Event? = when (action) {
  is UserAction  -> handleUserAction(action)
  is LobbyAction -> handleLobbyAction(action)
  is MatchAction -> handleMatchAction(action)
}

fun Connection.handleUserAction(action: UserAction): Event? = when (action) {
  is UpdateUsername -> this.copy(user = this.user.copy(name = action.name))
    .also { synchronized(connections) { connections[this.user.id] = it } }
    .let { UserUpdated(it.user) }
}

fun Connection.handleLobbyAction(action: LobbyAction): Event? = when (action) {
  CreateLobby   -> LobbyCreated(newLobby(Player(user, Color.Red)))
  is CloseLobby -> TODO()
  is JoinLobby  -> TODO()
}

fun Connection.handleMatchAction(action: MatchAction): Event? = when (action) {
  is CreateMatch -> {
    val lobby = getLobby(action.lobbyID)
    if (lobby == null) ErrorEvent("lobby does not exist")
    else if (!lobby.players.containsKey(user.id)) ErrorEvent("user does not have access")
    else MatchCreated(newMatch(lobby))
  }

  else           -> {
    val match = getMatch("")
    if (match == null) ErrorEvent("match does not exist")
    else handleMatchActionWithMatch(action, match)
  }
}

fun Connection.handleMatchActionWithMatch(action: MatchAction, match: Match): Event? =
  when (action) {
    is EntityPlacement -> TODO()
    is JoinMatch       -> TODO()
    is Start           -> TODO()
    else               -> ErrorEvent()
  }
