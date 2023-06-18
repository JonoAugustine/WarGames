package com.jonoaugustine.wargames.common.ecs

import com.jonoaugustine.wargames.common.MatchID
import com.jonoaugustine.wargames.common.Player
import com.jonoaugustine.wargames.common.UserID
import kotlinx.serialization.Serializable

const val updateInterval = 1 / 60.0

@Serializable
data class EcsMatch(
  val id: MatchID,
  val state: State = State.PLACING,
  val players: Map<UserID, Player> = emptyMap(),
) {

  enum class State {
    PLACING,
    PLANNING,
    RUNNING
  }
}
