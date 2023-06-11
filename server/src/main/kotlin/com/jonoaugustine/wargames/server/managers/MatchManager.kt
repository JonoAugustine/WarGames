package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.Collections.synchronizedMap

/**
 * [PlayerID] -> [Match]
 *
 * [MatchID] -> [Match]
 */
private var matches: Map<String, Match> = mapOf()
private val mutex = Mutex()

suspend fun getMatch(id: MatchID): Match? = mutex.withLock { matches[id] }
suspend fun getMatchOf(user: User): Match? = getMatch(user.id)
suspend fun Match.save() = mutex.withLock { matches = matches + (id to this) }

/** Creates a new [Match] using an existing [Lobby] */
fun newMatch(lobby: Lobby): Match = Match(
  id = UUID.randomUUID().toString(),
  lobbyID = lobby.id,
  players = lobby.players
)

/**
 * Creates a new [Match] state IF no existing entities collide with the given [entity]
 */
fun Match.addEntity(entity: Entity): Match? =
  entity
    .takeIf { this.entities.none { o -> it.collidesWith(o) } }
    ?.let { this.copy(entities = this.entities + it) }
