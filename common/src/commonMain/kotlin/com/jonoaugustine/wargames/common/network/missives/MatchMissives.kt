package com.jonoaugustine.wargames.common.network.missives

import com.jonoaugustine.wargames.common.Entity
import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Match Actions & Events
 */

@Serializable
sealed interface MatchAction : Action

@Serializable
sealed interface MatchEvent : Event {

  val match: Match
}

@Serializable
@SerialName("create")
data class CreateMatch(val lobbyID: String) : MatchAction

@Serializable
@SerialName("created")
data class MatchCreated(override val match: Match) : MatchEvent

@Serializable
@SerialName("join")
data class JoinMatch(val matchID: String) : MatchAction

@Serializable
@SerialName("joined")
data class MatchJoined(val player: Player, override val match: Match) : MatchEvent

@Serializable
@SerialName("left")
data class MatchLeft(val playerID: String, override val match: Match) : MatchEvent

@Serializable
@SerialName("start")
data class Start(val matchID: String) : MatchAction

@Serializable
@SerialName("started")
data class MatchStarted(override val match: Match) : MatchEvent

@Serializable
@SerialName("placement")
data class PlaceEntity(val matchID: String, val entity: Entity) : MatchAction

@Serializable
@SerialName("placed")
data class EntityPlaced(override val match: Match) : MatchEvent
