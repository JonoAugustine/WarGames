package com.jonoaugustine.wargames.common.network

import com.jonoaugustine.wargames.common.ecs.GameStateCmpnt
import com.jonoaugustine.wargames.common.ecs.Replicated
import com.jonoaugustine.wargames.common.ecs.components.*
import com.jonoaugustine.wargames.common.ecs.entities.CombatUnit
import com.jonoaugustine.wargames.common.ecs.entities.WorldUnit
import com.jonoaugustine.wargames.common.network.missives.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val JsonConfig = Json {
  prettyPrint = true
  ignoreUnknownKeys = true
  encodeDefaults = true
  serializersModule = SerializersModule {
    actions()
    events()
    replicatedComponents()
    polymorphic(WorldUnit::class) {
      subclass(CombatUnit::class)
    }
  }
}

private fun SerializersModuleBuilder.actions() = polymorphic(Action::class) {
  // USER
  subclass(UpdateUsername::class)
  // LOBBY
  subclass(CreateLobby::class)
  subclass(UpdateLobbyName::class)
  subclass(JoinLobby::class)
  subclass(CloseLobby::class)
  subclass(StartLobby::class)
  // WORLD
  subclass(SpawnUnit::class)
  subclass(MoveUnit::class)
  subclass(SetUnitPath::class)
  subclass(SetUnitDestination::class)
}

private fun SerializersModuleBuilder.events() = polymorphic(Event::class) {
  subclass(ErrorEvent::class)
  subclass(DisconnectEvent::class)
  // USER
  subclass(UserConnected::class)
  // Lobby
  subclass(LobbyClosed::class)
  subclass(LobbyJoined::class)
  subclass(LobbyLeft::class)
  subclass(LobbyUpdated::class)
  // World
  subclass(WorldUpdated::class)
}

private fun SerializersModuleBuilder.replicatedComponents() =
  polymorphic(Replicated::class) {
    subclass(OwnerCmpnt::class)
    subclass(PlayerCmpnt::class)
    subclass(TransformCmpnt::class)
    subclass(CollisionCmpnt::class)
    subclass(PathMovementCmpnt::class)
    subclass(SpriteCmpnt::class)
    subclass(NameCmpnt::class)
    subclass(MapCmpnt::class)
    subclass(GameStateCmpnt::class)
    subclass(PathingCmpnt::class)
  }
