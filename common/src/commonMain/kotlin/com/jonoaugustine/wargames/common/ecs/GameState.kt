package com.jonoaugustine.wargames.common.ecs

import com.jonoaugustine.wargames.common.ecs.GameState.DONE
import kotlinx.serialization.Serializable

enum class GameState {
  STARTING,
  PLACING,
  PLANNING,
  RUNNING,
  POST,
  DONE,
}

@Serializable
data class GameStateContainer(var state: GameState) {

  operator fun hasNext() = state.ordinal < DONE.ordinal
}

val GameStateContainer.done get() = state === DONE
val GameStateContainer.notDone get() = state !== DONE
