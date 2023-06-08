package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.JsonConfig
import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.common.network.Event
import com.jonoaugustine.wargames.common.network.UserConnected
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString
import java.util.*
import java.util.Collections.synchronizedMap

data class Connection(val user: User, val session: WebSocketServerSession)

private val connections: MutableMap<String, Connection> = synchronizedMap(mutableMapOf())

suspend fun WebSocketServerSession.connectionFrom(name: String): Connection =
  Connection(User(UUID.randomUUID().toString(), name), this)
    .also { synchronized(connections) { connections[it.user.id] = it } }
    .also { println("new connection:\t${it.user.id}") }
    .also { send(JsonConfig.encodeToString<Event>(UserConnected(it.user))) }

fun getConnection(id: String): Connection? = synchronized(connections) { connections[id] }

