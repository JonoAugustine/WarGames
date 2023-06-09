package state

import Eventbus
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.jonoaugustine.wargames.common.Lobby
import com.jonoaugustine.wargames.common.User
import com.jonoaugustine.wargames.common.network.JsonConfig
import com.jonoaugustine.wargames.common.network.missives.*
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import state.Page.LOBBY
import state.Page.MAIN_MENU
import state.Page.MATCH_PLAY

enum class Page {
  MAIN_MENU,
  LOBBY,
  LOBBY_BROWSER,
  MATCH_PLAY,
}

data class AppStateData(
  val user: User = User("local", System.getProperty("os.name") ?: "LocalUser"),
  val lobby: Lobby? = null,
)

@OptIn(DelicateCoroutinesApi::class)
object AppState {

  private val mutex = Mutex()
  val client = HttpClient {
    //install()
    install(WebSockets) {
      contentConverter = KotlinxWebsocketSerializationConverter(JsonConfig)
      pingInterval = 1000L * 15
    }
    install(ContentNegotiation) { json(JsonConfig) }
    defaultRequest {
      host = "localhost"
      port = 8080
    }
  }
  var state: AppStateData by mutableStateOf(AppStateData())
    private set
  var world: World by mutableStateOf(configureWorld { })
  var selectedEntity: Entity? by mutableStateOf(null)
  var page: Page by mutableStateOf(MAIN_MENU)
    private set

  init {
    with(GlobalScope) {
      listenToUserEvents()
      listenToLobbyEvents()
      listenToWorldEvents()
      Eventbus<ErrorEvent> { event -> println("ERROR EVENT: ${event.message}") }
    }
  }

  private suspend fun update(block: (AppStateData) -> AppStateData) =
    this.mutex.withLock { state = block(state) }

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
    Eventbus<LobbyUpdated> { (lobby) -> update { it.copy(lobby = lobby) } }
    Eventbus<LobbyJoined> { (_, lobby) ->
      update { it.copy(lobby = lobby) }
      goTo(LOBBY)
    }
  }

  context(CoroutineScope)
  private fun listenToWorldEvents() {
    Eventbus<WorldUpdated> { (snapshot) ->
      goTo(MATCH_PLAY)
      @Suppress("UNCHECKED_CAST")
      runBlocking {
        world = configureWorld { }
          .apply { loadSnapshot(snapshot as Map<Entity, List<Component<*>>>) }
      }
    }
  }

  /**
   * Run the [block] in a concurrent-safe context of the [world]
   */
  context(CoroutineScope)
  suspend fun <T> inWorld(block: suspend World.() -> T): T =
    mutex.withLock { block(world) }

  fun goTo(page: Page) {
    if (page !== AppState.page) AppState.page = page
  }
}
