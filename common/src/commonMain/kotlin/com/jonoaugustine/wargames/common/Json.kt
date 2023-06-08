package com.jonoaugustine.wargames.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val JsonConfig = Json {
  prettyPrint = true
  ignoreUnknownKeys = true
  encodeDefaults = true
  serializersModule = SerializersModule {
    //polymorphic(Action::class) {
    //}
  }
}
