package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import logic.models.Entity

enum class GameState {
  PLANNING,
  RUNNING,
}

data class Game(val entities: List<Entity> = mutableStateListOf()) {
  var state: GameState by mutableStateOf(GameState.PLANNING)
  var background: Color by mutableStateOf(Color(0, 196, 0))
}
