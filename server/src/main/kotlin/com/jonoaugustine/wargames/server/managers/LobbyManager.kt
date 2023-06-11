package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.network.missives.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

/**
 * dan niki
 *
 * [PlayerID] -> [Lobby]
 *
 * [LobbyID] -> [Lobby]
 */
private var lobbies: Map<LobbyID, Lobby> = mapOf()
private val mutex = Mutex()

suspend fun getLobby(id: LobbyID): Lobby? = mutex.withLock { lobbies[id] }
suspend fun getLobbyOf(user: User): Lobby? = getLobby(user.id)
suspend fun allLobbies(): Collection<Lobby> = mutex.withLock { lobbies.values.toSet() }

suspend fun removeLobby(id: LobbyID): Lobby? = mutex.withLock {
  lobbies[id]?.also { lby -> lobbies = lobbies.filterValues { it.id != lby.id } }
}

suspend fun Lobby.save() = mutex.withLock {
  lobbies += (players.keys.map { it to this } + (id to this))
}

fun newLobby(host: Player): Lobby =
  Lobby(
    id = UUID.randomUUID().toString(),
    hostID = host.user.id,
    name = "${host.user.name}'s Lobby",
    players = mapOf(host.user.id to host)
  )

/**
 * Returns an updated [Lobby] state where the [player] was added
 */
fun Lobby.addPlayer(player: Player): Lobby =
  copy(players = players + (player.user.id to player))

fun Lobby.removePlayer(playerID: String): Lobby = copy(players = players - playerID)

suspend fun Connection.handleLobbyAction(action: LobbyAction): ActionResponse =
  when (action) {
    CreateLobby        -> getLobbyOf(user)
      ?.let { LobbyJoined(it.players[id]!!, it) to setOf(id) }
      ?: newLobby(Player(user, WgColor.Red))
        .also { it.save() }
        .let { LobbyCreated(it) to setOf(id) }

    is JoinLobby       ->
      getLobby(action.lobbyID)
        ?.also { lobby ->
          lobby.players[id]?.also { return LobbyJoined(it, lobby) to setOf(id) }
        }
        ?.let { it to Player(user, WgColor.Blue) }
        ?.let { (lobby, player) -> lobby.addPlayer(player) to player }
        ?.also { (lobby) -> lobby.save() }
        ?.let { (lobby, player) -> LobbyJoined(player, lobby) to lobby.players.keys }
        ?: (ErrorEvent("missing access") to setOf(id))

    is UpdateLobbyName -> getLobbyOf(user)
      ?.takeIf { it.hostID == user.id }
      ?.copy(name = action.name)
      ?.also { it.save() }
      ?.let { LobbyUpdated(it) to it.players.keys }
      ?: (ErrorEvent("missing access") to setOf(id))

    is CloseLobby      -> TODO()
  }
