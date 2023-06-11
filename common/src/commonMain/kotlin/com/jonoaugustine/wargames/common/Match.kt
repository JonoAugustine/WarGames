package com.jonoaugustine.wargames.common

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
  val entities: List<Entity> = emptyList(),
  val background: WgColor = WgColor.Grass,
) {

  enum class State {
    PLACING,
    PLANNING,
    RUNNING
  }

  // TODO move functions to server match manager
  fun setState(state: State): Match = copy(state = state)

  fun addPlayer(user: User, color: WgColor): Match =
    copy(players = this.players + Pair(user.id, Player(user, color)))

  fun updateState(newState: State): Match = copy(state = newState)

  fun updateEntities(delta: Float): Match = this.copy(
    entities = entities.map { it.update(delta, this) }
  )
}
