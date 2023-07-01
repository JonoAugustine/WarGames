package com.jonoaugustine.wargames.common.network.missives

import com.jonoaugustine.wargames.common.Lobby
import com.jonoaugustine.wargames.common.LobbyID
import com.jonoaugustine.wargames.common.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface LobbyEvent : Event
sealed interface LobbyAction : Action

@Serializable
@SerialName("lobby.create")
object CreateLobby : LobbyAction

@Serializable
@SerialName("lobby.created")
data class LobbyCreated(val lobby: Lobby) : LobbyEvent

@Serializable
@SerialName("lobby.updated")
data class LobbyUpdated(val lobby: Lobby) : LobbyEvent

@Serializable
@SerialName("lobby.close")
data class CloseLobby(val lobbyID: Lobby) : LobbyAction

@Serializable
@SerialName("lobby.closed")
data class LobbyClosed(val lobbyID: Lobby) : LobbyEvent

@Serializable
@SerialName("lobby.join")
data class JoinLobby(val lobbyID: String) : LobbyAction

@Serializable
@SerialName("lobby.joined")
data class LobbyJoined(val player: Player, val lobby: Lobby) : LobbyEvent

@Serializable
@SerialName("lobby.left")
data class LobbyLeft(val playerID: String, val lobby: Lobby) : LobbyEvent

@Serializable
@SerialName("lobby.updateName")
data class UpdateLobbyName(val lobbyID: LobbyID, val name: String) : LobbyAction

@Serializable
@SerialName("lobby.start")
object StartLobby : LobbyAction
