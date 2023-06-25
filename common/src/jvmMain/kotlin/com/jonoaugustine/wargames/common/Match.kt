package com.jonoaugustine.wargames.common

import com.jonoaugustine.wargames.common.entities.Entity
import com.jonoaugustine.wargames.common.entities.EntityID
import kotlinx.serialization.Serializable

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
    500,
    500
  )
)

@Serializable
data class LobbyPreview(
  val id: LobbyID,
  val name: String,
  val players: Int,
)

@Serializable
data class Match(
  val id: MatchID,
  val lobbyID: String,
  val state: State = State.PLACING,
  val players: Map<UserID, Player> = emptyMap(),
  val entities: Map<EntityID, Entity> = emptyMap(),
  val background: WgColor = WgColor.Grass,
  val mapSize: WgSize = WgSize(
    800,
    800
  )
) {

  enum class State {
    PLACING,
    PLANNING,
    RUNNING
  }
}
