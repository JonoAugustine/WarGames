package logic

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import logic.models.Entity

enum class GameState {
  PLANNING,
  RUNNING,
}

enum class MatchState {
  PLACING,
  PLANNING,
  RUNNING
}

class Match {

  var state: MatchState by mutableStateOf(MatchState.PLACING)
  var entities: List<Entity> by mutableStateOf(emptyList())
  var background: Color by mutableStateOf(Color(0, 130, 0))
}

class Game {
  var match: Match? by mutableStateOf(null)

  fun update(delta: Float) {
    match?.entities?.forEach { it.update(delta, this) }
  }
}
