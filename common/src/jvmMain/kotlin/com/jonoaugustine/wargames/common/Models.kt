package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class User(val id: UserID, val name: String)

fun userOf(name: String): User = User(UUID.randomUUID().toString(), name)

@Serializable
data class Player(val user: User, val color: WgColor)

val Player.id get() = user.id

@Serializable
data class Lobby(
  val id: LobbyID,
  val hostID: UserID,
  val name: String = id,
  val players: Map<UserID, Player> = emptyMap(),
  val mapSize: WgSize = WgSize(
    1000,
    1000
  )
)

@Serializable
data class LobbyPreview(
  val id: LobbyID,
  val name: String,
  val players: Int,
)
