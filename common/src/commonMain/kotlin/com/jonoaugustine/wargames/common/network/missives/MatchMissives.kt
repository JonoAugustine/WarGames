package com.jonoaugustine.wargames.common.network.missives

import com.jonoaugustine.wargames.common.Entity
import com.jonoaugustine.wargames.common.EntityID
import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.Vector
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
data class MatchLeft(val playerID: String, override val match: Match) : MatchEvent

@Serializable
@SerialName("match.start")
object StartMatch : LiveMatchAction

//@Serializable
//@SerialName("match.started")
//data class MatchStarted(override val match: Match) : MatchEvent
@Serializable
@SerialName("match.place")
data class PlaceEntity(val matchID: String, val entity: Entity) : LiveMatchAction

//@Serializable
//@SerialName("match.placed")
//data class EntityPlaced(val entity: Entity, override val match: Match) : MatchEvent
@Serializable
@SerialName("match.move")
data class MoveEntity(val entityID: EntityID, val position: Vector) : LiveMatchAction

