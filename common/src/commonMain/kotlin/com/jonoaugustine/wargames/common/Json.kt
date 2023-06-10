package com.jonoaugustine.wargames.common

import com.jonoaugustine.wargames.common.network.missives.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val JsonConfig = Json {
  prettyPrint = true
  ignoreUnknownKeys = true
  encodeDefaults = true
  serializersModule = SerializersModule {
    polymorphic(Action::class) {
      subclass(CreateMatch::class)
      subclass(Start::class)
      subclass(JoinMatch::class)
    }
    polymorphic(Event::class) {
      subclass(ErrorEvent::class)
      subclass(MatchJoined::class)
      subclass(MatchLeft::class)
      subclass(MatchCreated::class)
      subclass(MatchStarted::class)
      subclass(UserConnected::class)
    }
  }
}
