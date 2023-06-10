package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Player(val user: User, val color: Color)

@Serializable
data class Lobby(
  val id: String = UUID.randomUUID().toString(),
  val players: Map<String, Player> = emptyMap()
)

@Serializable
data class Match(
  val id: String = UUID.randomUUID().toString(),
  val lobbyID: String,
  val state: State = State.PLACING,
  val players: Map<String, Player> = emptyMap(),
  val entities: List<Entity> = emptyList(),
  val background: Color = Color.Grass,
) {

  enum class State {
    PLACING,
    PLANNING,
    RUNNING
  }

  fun setState(state: State): Match = copy(state = state)

  fun addPlayer(user: User, color: Color): Match =
    copy(players = this.players + Pair(user.id, Player(user, color)))

  fun updateState(newState: State): Match = copy(state = newState)

  fun updateEntities(delta: Float): Match = this.copy(
    entities = entities.map { it.update(delta, this) }
  )
}
