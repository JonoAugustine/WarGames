package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.Entity
import com.jonoaugustine.wargames.common.Lobby
import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.Player
import java.util.Collections.synchronizedMap

/** dan niki */
private val lobbies: MutableMap<String, Lobby> = synchronizedMap(mutableMapOf())

fun getLobby(id: String): Lobby? = synchronized(lobbies) { lobbies[id] }

fun removeLobby(id: String): Lobby? = synchronized(lobbies) { lobbies.remove(id) }

fun newLobby(host: Player): Lobby =
  Lobby(players = mapOf(host.user.id to host))
    .also { synchronized(lobbies) { lobbies[it.id] = it } }

/**
 * Returns an updated [Lobby] state if the [player] was added
 */
fun Lobby.addPlayer(player: Player): Lobby? =
  takeUnless { players.containsKey(player.user.id) }
    ?.copy(players = players + (player.user.id to player))

fun Lobby.removePlayer(playerID: String): Lobby =
  copy(players = players - playerID)

