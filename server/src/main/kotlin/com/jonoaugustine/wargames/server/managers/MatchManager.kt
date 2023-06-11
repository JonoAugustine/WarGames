package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.Match.State.PLACING
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
    .takeIf { this.entities.values.none { o -> it.collidesWith(o) } }
    ?.let {
      when (it) {
        is Infantry -> it.copy(id = UUID.randomUUID().toString())
        is Archer   -> it.copy(id = UUID.randomUUID().toString())
      }
    }
    ?.let { this.copy(entities = this.entities + (it.id to it)) }

suspend fun Connection.handleMatchAction(action: MatchAction): ActionResponse? =
  when (action) {
    is CreateMatch     -> getLobbyOf(user)
      ?.let { newMatch(it) }
      ?.also { it.save() }
      ?.let { MatchCreated(it) to it.players.keys }
      ?: errorEventOf("lobby does not exist")

    is LiveMatchAction -> getMatchOf(user)
      ?.let { handleLiveMatchAction(action, it) }
      ?: errorEventOf("match does not exist")
  }

suspend fun Connection.handleLiveMatchAction(
  action: LiveMatchAction,
  match: Match
): ActionResponse? =
  when (action) {
    StartMatch     -> TODO()
    is PlaceEntity -> action.entity
      .takeIf { (it as BattleUnit).color == match.players[id]!!.color }
      .also { it ?: return errorEventOf("mismatch color") }
      ?.let { match.addEntity(it) }
      ?.also { it.save() }
      ?.let { MatchUpdated(it) to match.players.keys }
      ?: errorEventOf("entity would collide")

    is MoveEntity  -> match
      .takeIf { it.state == PLACING }
      .also { if (it == null) return errorEventOf("invalid match state") }
      ?.entities
      ?.get(action.entityID)
      .also { if (it == null) return errorEventOf("entity does not exist") }
      ?.let {
        when (it) {
          is Infantry -> it.copy(
            position = action.position,
            rotation = action.rotation ?: it.rotation
          )

          is Archer   -> it.copy(
            position = action.position,
            rotation = action.rotation ?: it.rotation
          )
        }
      }
      ?.takeIf {
        match.entities.values
          .filterNot { o -> it.id == o.id }
          .none { o -> it.collidesWith(o) }
      }
      .let { it ?: return errorEventOf("entity would collide") }
      .let { match.copy(entities = match.entities + (it.id to it)) }
      .also { it.save() }
      .let { MatchUpdated(it) to match.players.keys }

    is JoinMatch   -> TODO()
  }
