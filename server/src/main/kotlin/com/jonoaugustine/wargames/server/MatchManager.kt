package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.Entity
import com.jonoaugustine.wargames.common.Lobby
import com.jonoaugustine.wargames.common.Match
import java.util.Collections.synchronizedMap

private val matches: MutableMap<String, Match> = synchronizedMap(mutableMapOf())

fun getMatch(id: String): Match? = synchronized(matches) { matches[id] }
fun setMatch(match: Match) = synchronized(matches) { matches[match.id] = match }

fun newMatch(lobby: Lobby): Match =
  Match(lobbyID = lobby.id, players = lobby.players)
    .also { synchronized(matches) { matches[it.id] = it } }

/**
 * Returns an updated Match IF no existing entities collide with the given [entity]
 */
fun Match.addEntity(entity: Entity): Match? =
  entity
    .takeIf { this.entities.none { o -> it.collidesWith(o) } }
    ?.let { this.copy(entities = this.entities + it) }
