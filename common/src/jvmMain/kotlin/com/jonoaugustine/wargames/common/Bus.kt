package com.jonoaugustine.wargames.common

import com.jonoaugustine.wargames.common.network.missives.Action
import com.jonoaugustine.wargames.common.network.missives.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

typealias ActionBus = Bus<Action>
typealias EventBus = Bus<Event>

class Bus<T> {

  private val flow = MutableSharedFlow<T>()
  val events get() = flow.asSharedFlow()

  /** Add a listener to the bus */
  context(CoroutineScope)
  inline operator fun <reified Sub : T> invoke(noinline handler: suspend (Sub) -> Unit) {
    launch { events.collect { sub -> if (sub is Sub) handler(sub) } }
  }

  suspend fun announce(t: T) = this.flow.emit(t)
}

