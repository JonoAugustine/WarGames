import com.jonoaugustine.wargames.common.Entity
import com.jonoaugustine.wargames.common.User

data class Player(val user: User)

data class Match(
  val state: State,
  val players: MutableMap<String, Player>,
  val entities: List<Entity>,
) {

  enum class State {
    PLACING,
    PLANNING,
    RUNNING
  }

  fun updateState(newState: State): Match = copy(state = newState)

  fun updateEntities(delta: Float): Match = this.copy(
    entities = entities.map { it.update(delta, this) }
  )
}
