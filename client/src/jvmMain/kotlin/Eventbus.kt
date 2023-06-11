import com.jonoaugustine.wargames.common.network.missives.ErrorEvent
import com.jonoaugustine.wargames.common.network.missives.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
object Eventbus {

  private val eventFlow = MutableSharedFlow<Event>()
  val events = eventFlow.asSharedFlow()

  context(CoroutineScope)
  inline operator fun <reified T : Event> invoke(noinline handler: suspend (T) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    launch { events.collect { event -> if (event is T) handler(event) } }
  }

  suspend fun announce(event: Event) = this.eventFlow.emit(event)
}
