import com.jonoaugustine.wargames.common.network.Event
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object Eventbus {

  private val eventFlow = MutableSharedFlow<Event>()
  val events = eventFlow.asSharedFlow()

  suspend inline operator fun <reified T : Event> invoke(noinline handler: (T) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    events.collect { event -> if (event is T) handler(event) }
  }

  suspend fun announce(event: Event) = this.eventFlow.emit(event)
}
