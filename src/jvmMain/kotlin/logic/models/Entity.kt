package logic.models

import Position
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

sealed interface Entity {
  val position: Position
}

class BattleUnit(position: Position, speed: Double = 0.0) : Entity {
  override var position by mutableStateOf(position)
  var speed by mutableStateOf(speed)
  var path = mutableStateListOf<Offset>()

}
