package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.network.missives.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

/**
 * [PlayerID] -> [Match]
 *
 * [MatchID] -> [Match]
 */
private var matches: Map<String, Match> = mapOf()
private val mutex = Mutex()

suspend fun getMatch(id: MatchID): Match? = mutex.withLock { matches[id] }
suspend fun getMatchOf(user: User): Match? = getMatch(user.id)
suspend fun Match.save() = mutex.withLock {
  matches = matches + (players.keys.map { it to this } + (id to this))
}

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
    ?.let {
      when (it) {
        is Infantry -> it.copy(id = UUID.randomUUID().toString())
      }
    }
    ?.let { this.copy(entities = this.entities + it) }

suspend fun Connection.handleMatchAction(action: MatchAction): ActionResponse? =
  when (action) {
    is CreateMatch     -> getLobbyOf(user)
      ?.let { newMatch(it) }
      ?.also { it.save() }
      ?.let { MatchCreated(it) to it.players.keys }
      ?: (ErrorEvent("lobby does not exist") to setOf(id))

    is LiveMatchAction -> getMatchOf(user)
      ?.let { handleLiveMatchAction(action, it) }
      ?: (ErrorEvent("match does not exist") to setOf(id))
  }

suspend fun Connection.handleLiveMatchAction(
  action: LiveMatchAction,
  match: Match
): ActionResponse? =
  when (action) {
    StartMatch     -> TODO()
    is PlaceEntity -> match.addEntity(action.entity)
      ?.also { it.save() }
      ?.let { MatchUpdated(it) to match.players.keys }
      ?: (ErrorEvent("entity would collide") to setOf(id))

    is JoinMatch   -> TODO()
    is MoveEntity  -> TODO()
  }
