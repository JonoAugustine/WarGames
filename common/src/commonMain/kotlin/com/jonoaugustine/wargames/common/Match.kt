package com.jonoaugustine.wargames.common

import com.jonoaugustine.wargames.common.entities.Entity
import com.jonoaugustine.wargames.common.entities.EntityID
import kotlinx.serialization.Serializable

typealias LobbyID = String
typealias MatchID = String

@Serializable
data class Player(val user: User, val color: WgColor)

@Serializable
data class Lobby(
  val id: LobbyID,
  val hostID: UserID,
  val name: String = id,
  val players: Map<UserID, Player> = emptyMap(),
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
) {

  enum class State {
    PLACING,
    PLANNING,
    RUNNING
  }
}
