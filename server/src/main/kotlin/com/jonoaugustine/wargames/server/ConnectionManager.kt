package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.common.network.*
import io.ktor.server.websocket.WebSocketServerSession
import java.util.*
import java.util.Collections.synchronizedMap

data class Connection(val user: User, val session: WebSocketServerSession)

private val connections: MutableMap<String, Connection> = synchronizedMap(mutableMapOf())

private fun userOf(name: String?): User = UUID.randomUUID().toString()
  .let { User(it, name ?: it.substring(0..5)) }

suspend fun WebSocketServerSession.connectionFrom(name: String?): Connection =
  Connection(userOf(name), this)
    .also { synchronized(connections) { connections[it.user.id] = it } }
    .also { println("new connection:\t${it.user.id}") }
    .also { send(UserConnected(it.user)) }

fun getConnection(id: String): Connection? = synchronized(connections) { connections[id] }

@Suppress("UNREACHABLE_CODE")
suspend fun Connection.handleAction(action: Action): Event? = when (action) {
  is UserAction  -> handleUserAction(action)
  is MatchAction -> handleMatchAction(action)
}

fun Connection.handleUserAction(action: UserAction): Event? = when (action) {
  is UpdateUsername -> this.copy(user = this.user.copy(name = action.name))
    .also { synchronized(connections) { connections[this.user.id] = it } }
    .let { UsernameUpdated(it.user) }
}

suspend fun Connection.handleMatchAction(action: MatchAction): Event? = when (action) {
  CreateMatch        -> TODO("handle action")
  Start              -> TODO("handle action")
  is EntityPlacement -> TODO("handle action")
  is JoinMatch       -> TODO("handle action")
}
