package com.jonoaugustine.wargames.common.ecs

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.World
import com.jonoaugustine.wargames.common.WgSize
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
data class GameStateCmpnt(
  var state: GameState,
  val mapSize: WgSize
) : Component<GameStateCmpnt>, Replicated {

  operator fun hasNext() = state.ordinal < DONE.ordinal

  override fun type() = GameStateCmpnt

  companion object : ComponentType<GameStateCmpnt>()
}

val GameStateCmpnt.done get() = state === DONE
val GameStateCmpnt.notDone get() = state !== DONE

fun World.gameStateContainer(state: GameState, mapSize: WgSize) =
  entity { it += GameStateCmpnt(state, mapSize) }

val World.gameState: GameStateCmpnt?
  get() = family { all(GameStateCmpnt) }.firstOrNull()?.get(GameStateCmpnt)
