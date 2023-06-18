package com.jonoaugustine.wargames.server.managers

import com.jonoaugustine.wargames.common.*
import com.jonoaugustine.wargames.common.Match.State.PLACING
import com.jonoaugustine.wargames.common.Match.State.PLANNING
import com.jonoaugustine.wargames.common.Match.State.RUNNING
import com.jonoaugustine.wargames.common.entities.Archer
import com.jonoaugustine.wargames.common.entities.BattleUnit
import com.jonoaugustine.wargames.common.entities.Entity
import com.jonoaugustine.wargames.common.entities.Infantry
import com.jonoaugustine.wargames.common.network.missives.*
import com.jonoaugustine.wargames.server.send
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
private val threadContext = newSingleThreadContext("match.loops")
private val matchExecScope = CoroutineScope(threadContext)

/**
 * [UserID] -> [Match]
 *
 * [MatchID] -> [Match]
 */
private var matches: Map<String, Match> = mutableMapOf()
private var matchJobs: Map<MatchID, Job> = mutableMapOf()
private val mutex = Mutex()

suspend fun getMatch(id: String): Match? = mutex.withLock { matches[id] }
suspend fun getMatchOf(user: User): Match? = getMatch(user.id)
suspend fun Match.save() = mutex.withLock {
  matches = matches + (players.keys.map { it to this } + (id to this))
}

/** Creates a new [Match] using an existing [Lobby] */
fun newMatch(lobby: Lobby): Match = Match(
  id = UUID.randomUUID().toString(), lobbyID = lobby.id, players = lobby.players
)

/**
 * Creates a new [Match] state IF no existing entities collide with the given [entity]
 */
fun Match.addEntity(entity: Entity): Match? =
  entity.takeIf { this.entities.values.none { o -> it.collidesWith(o) } }?.let {
    when (it) {
      is Infantry -> it.copy(id = UUID.randomUUID().toString())
      is Archer   -> it.copy(id = UUID.randomUUID().toString())
    }
  }?.let { this.copy(entities = this.entities + (it.id to it)) }

// TODO create match should start match job
suspend fun Connection.handleMatchAction(action: MatchAction): ActionResponse =
  when (action) {
    is CreateMatch     -> getLobbyOf(user)
      ?.let { newMatch(it) }
      ?.also { it.save() }
      ?.also { startWorld(it) }
      ?.let { MatchCreated(it) to it.players.keys }
      ?: errorEventOf("lobby does not exist")

    is LiveMatchAction -> getMatchOf(user)
      ?.let { handleLiveMatchAction(action, it) }
      ?: errorEventOf("match does not exist")
  }

// TODO Should add to match action Queue and return null
// TODO Map Bounding
private suspend fun Connection.handleLiveMatchAction(
  action: LiveMatchAction,
  match: Match
): ActionResponse = when (action) {
  is SetMatchState -> setMatchState(match, action)
  is SetEntityPath -> setEntityPath(match, action)
  is PlaceEntity   -> placeEntity(match, action)
  is MoveEntity    -> moveEntity(match, action)
  StartMatch       -> TODO("start match action. probably going to delete")
  is JoinMatch     -> TODO("join match in progress (probably going to delete)")
}

private suspend fun Connection.setMatchState(
  match: Match, action: SetMatchState
): ActionResponse = match
  .takeUnless { it.state == RUNNING }
  .let { it ?: return errorEventOf("match is running") }
  .copy(state = action.state)
  .also { it.save() }
  .also { if (it.state == RUNNING) startMatchJob(it) }
  .let { MatchUpdated(it) to it.players.keys }

private suspend fun Connection.placeEntity(
  match: Match, action: PlaceEntity
): ActionResponse = match.takeIf { it.state == PLACING }
  .let { it ?: return errorEventOf("invalid match state") }
  .let { action.entity }
  .takeIf { (it as BattleUnit).color == match.players[id]!!.color }
  .let { it ?: return errorEventOf("mismatch color") }
  .let { match.addEntity(it) }
  .let { it ?: return errorEventOf("entity would collide") }
  .also { it.save() }
  .let { MatchUpdated(it) to it.players.keys }

private suspend fun Connection.setEntityPath(
  match: Match, action: SetEntityPath
): ActionResponse = match.takeIf { it.state == PLANNING }
  .let {
    it ?: return errorEventOf("invalid match state")
  }.entities[action.entityID].let { it ?: return errorEventOf("entity does not exist") }
  .let { knownEntity ->
    when (knownEntity) {
      is Infantry -> knownEntity.copy(path = action.path)
      is Archer   -> knownEntity.copy(path = action.path)
    }
  }
  .let { match.copy(entities = match.entities + (it.id to it)) }
  .also { it.save() }
  .let { MatchUpdated(it) to it.players.keys }

private suspend fun Connection.moveEntity(
  match: Match,
  action: MoveEntity
): ActionResponse = match.takeIf { it.state == PLACING }
  .let {
    it ?: return errorEventOf("invalid match state")
  }.entities[action.entityID].let { it ?: return errorEventOf("entity does not exist") }
  .let { knownEntity ->
    when (knownEntity) {
      is Infantry -> knownEntity.copy(
        position = action.position, rotation = action.rotation ?: knownEntity.rotation
      )

      is Archer   -> knownEntity.copy(
        position = action.position, rotation = action.rotation ?: knownEntity.rotation
      )
    }
  }
  .takeIf {
    match.entities.values.filterNot { o -> it.id == o.id }
      .none { o -> it.collidesWith(o) }
  }
  .let { it ?: return errorEventOf("entity would collide") }
  .let { match.copy(entities = match.entities + (it.id to it)) }
  .also { it.save() }
  .let { MatchUpdated(it) to it.players.keys }

private suspend fun startMatchJob(match: Match) = with(matchExecScope) {
  val job = launch(threadContext) {
    var _match: Match = match
    while (_match.state == RUNNING) {
      _match = getMatch(match.id) ?: break
      val updatedEntities = _match.entities
        .mapValues { it.value.update(1 / 60f, _match) }
      _match = _match.copy(entities = updatedEntities).also { it.save() }
      // break after all movement complete
      // TODO this may have to change if the round doesn't end after movement completes
      val movementIncomplete = _match.entities
        .values
        .filterIsInstance<BattleUnit>()
        .any { it.path.isNotEmpty() }
      if (!movementIncomplete) break
      _match.players.keys
        .mapNotNull { getConnection(it) }
        .forEach { it.session.send(MatchUpdated(_match)) }
      delay(1.seconds / 60)
    }
    println("MATCH JOB END: ${_match.id}")
    _match.copy(state = PLACING)
      .also { it.save() }
      .also {
        it.players.keys
          .mapNotNull { uid -> getConnection(uid) }
          .forEach { con -> con.session.send(MatchUpdated(it)) }
      }
  }
  mutex.withLock { matchJobs += (match.id to job) }
}
