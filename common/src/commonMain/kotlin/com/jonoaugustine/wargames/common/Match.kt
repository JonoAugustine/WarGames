package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable

@Serializable
data class Player(val user: User)

@Serializable
data class Match(
  val id: String,
  val state: State,
  val players: Map<String, Player>,
  val entities: List<Entity>,
) {

  enum class State {
    PLACING,
    PLANNING,
    RUNNING
  }

  fun addPlayer(user: User): Match =
    copy(players = this.players + Pair(user.id, Player(user)))

  fun updateState(newState: State): Match = copy(state = newState)

  fun updateEntities(delta: Float): Match = this.copy(
    entities = entities.map { it.update(delta, this) }
  )
}
