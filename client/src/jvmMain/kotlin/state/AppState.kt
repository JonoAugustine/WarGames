package state

import Eventbus
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.common.network.UsernameUpdated
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import logic.Game
import state.Page.MAIN_MENU

enum class Page {
  MAIN_MENU,
  MATCH_CREATOR,
  MATCH_PLAY,
}

data class AppStateData(
  val game: Game = Game(),
  val user: User = User("local", System.getProperty("os.name") ?: "LocalUser"),
)

@OptIn(DelicateCoroutinesApi::class)
object AppState {

  var data: AppStateData by mutableStateOf(AppStateData())
    private set
  var page: Page by mutableStateOf(MAIN_MENU)
    private set

  init {
    GlobalScope.launch {
      Eventbus<UsernameUpdated> { (user) ->
        println("updating state username")
        update { it.copy(user = it.user.copy(name = user.name)) }
      }
    }
  }

  fun update(block: (AppStateData) -> AppStateData) {
    data = block(data)
  }

  fun goTo(page: Page) {
    AppState.page = page
  }
}
