package com.jonoaugustine.wargames.common

import com.jonoaugustine.wargames.common.entities.Entity
import com.jonoaugustine.wargames.common.entities.Infantry
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
      // USER
      subclass(UpdateUsername::class)
      // LOBBY
      subclass(CreateLobby::class)
      subclass(UpdateLobbyName::class)
      subclass(JoinLobby::class)
      subclass(CloseLobby::class)
      // MATCH
      subclass(CreateMatch::class)
      subclass(StartMatch::class)
      subclass(PlaceEntity::class)
      subclass(SetMatchState::class)
      subclass(SetEntityPath::class)
    }
    polymorphic(Event::class) {
      subclass(ErrorEvent::class)
      // USER
      subclass(UserConnected::class)
      // Lobby
      subclass(LobbyClosed::class)
      subclass(LobbyJoined::class)
      subclass(LobbyLeft::class)
      subclass(LobbyUpdated::class)
      // Match
      subclass(MatchCreated::class)
      subclass(MatchLeft::class)
      subclass(MatchUpdated::class)
      subclass(MatchUpdated::class)
      //subclass(MatchStarted::class)
      //subclass(EntityPlaced::class)
    }
    polymorphic(Entity::class) {
      subclass(Infantry::class)
    }
  }
}
