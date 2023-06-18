package com.jonoaugustine.wargames.common.network.missives

import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.entities.Entity
import com.jonoaugustine.wargames.common.entities.EntityID
import com.jonoaugustine.wargames.common.math.Vector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Match Actions & Events
 */

@Serializable
sealed interface MatchAction : Action

@Serializable
sealed interface LiveMatchAction : MatchAction

@Serializable
sealed interface MatchEvent : Event {

  val match: Match

  operator fun component1() = match
}

@Serializable
@SerialName("match.update")
data class MatchUpdated(override val match: Match) : MatchEvent

@Serializable
@SerialName("match.create")
data class CreateMatch(val lobbyID: String) : MatchAction

@Serializable
@SerialName("match.created")
data class MatchCreated(override val match: Match) : MatchEvent

@Serializable
@SerialName("match.join")
data class JoinMatch(val matchID: String) : LiveMatchAction

@Serializable
@SerialName("match.left")
data class MatchLeft(override val match: Match, val playerID: String) : MatchEvent

@Serializable
@SerialName("match.start")
object StartMatch : LiveMatchAction

@Serializable
@SerialName("match.state")
data class SetMatchState(val state: Match.State) : LiveMatchAction

//@Serializable
//@SerialName("match.started")
//data class MatchStarted(override val match: Match) : MatchEvent
@Serializable
@SerialName("match.place")
data class PlaceEntity(val matchID: String, val entity: Entity) : LiveMatchAction
//@Serializable
//@SerialName("match.placed")
//data class EntityPlaced(val entity: Entity, override val match: Match) : MatchEvent
/**
 * @property position new position
 * @property rotation new rotation
 */
@Serializable
@SerialName("match.move")
data class MoveEntity(
  val entityID: EntityID,
  val position: Vector,
  val rotation: Float? = null
) : LiveMatchAction

@Serializable
@SerialName("match.path")
data class SetEntityPath(val entityID: EntityID, val path: List<Vector>) : LiveMatchAction

