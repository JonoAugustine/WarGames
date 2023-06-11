package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.*
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
 * Returns an updated [Lobby] state if the [player] was added
 */
fun Lobby.addPlayer(player: Player): Lobby? =
  takeUnless { players.containsKey(player.user.id) }
    ?.copy(players = players + (player.user.id to player))

fun Lobby.removePlayer(playerID: String): Lobby = copy(players = players - playerID)

