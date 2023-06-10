package state

import Eventbus
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jonoaugustine.wargames.common.Lobby
import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.common.network.missives.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import state.Page.LOBBY
import state.Page.MAIN_MENU
import state.Page.MATCH_PLAY

enum class Page {
  MAIN_MENU,
  LOBBY,
  MATCH_PLAY,
}

data class AppStateData(
  val user: User = User("local", System.getProperty("os.name") ?: "LocalUser"),
  val lobby: Lobby? = null,
  val match: Match? = null,
)

@OptIn(DelicateCoroutinesApi::class)
object AppState {

  private val mutex = Mutex()
  var state: AppStateData by mutableStateOf(AppStateData())
    private set
  var page: Page by mutableStateOf(MAIN_MENU)
    private set

  init {
    with(GlobalScope) {
      listenToUserEvents()
      listenToLobbyEvents()
      listenToMatchEvents()
    }
  }

  private suspend fun update(block: (AppStateData) -> AppStateData) {
    println("STATE: updating data ${block(state)}")
    this.mutex.withLock { state = block(state) }
  }

  context(CoroutineScope)
  private fun listenToUserEvents() {
    Eventbus<UserEvent> { event -> update { it.copy(user = event.user) } }
  }

  context(CoroutineScope)
  private fun listenToLobbyEvents() {
    Eventbus<LobbyCreated> { (lobby) ->
      update { it.copy(lobby = lobby) }
      goTo(LOBBY)
    }
  }

  context(CoroutineScope)
  private fun listenToMatchEvents() {
    Eventbus<MatchEvent> { event ->
      update { it.copy(match = event.match) }
      if (event is MatchCreated) goTo(MATCH_PLAY)
    }
  }

  fun goTo(page: Page) {
    AppState.page = page
  }
}
