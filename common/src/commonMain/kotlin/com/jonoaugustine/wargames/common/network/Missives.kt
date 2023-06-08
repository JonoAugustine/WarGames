package com.jonoaugustine.wargames.common.network

import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.Player
import com.jonoaugustine.wargames.common.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Actions are made by the client. CLIENT -> SERVER */
@Serializable
sealed class Action

/** Events are distributed by the server. SERVER -> CLIENT */
@Serializable
sealed class Event
/*
 * Errors
 */

@Serializable
@SerialName("error")
open class ErrorEvent(val message: String = "an unknown error occurred") : Event()

@Serializable
sealed class UserAction : Action()

@Serializable
sealed class UserEvent : Event()

@Serializable
@SerialName("connected")
data class UserConnected(val user: User) : UserEvent()
/*
 * Match Actions & Events
 */

@Serializable
sealed class MatchAction : Action()

@Serializable
sealed class MatchEvent : Event()

@Serializable
@SerialName("create")
object CreateMatch : MatchAction()

@Serializable
@SerialName("created")
data class MatchCreated(val match: Match) : MatchEvent()

@Serializable
@SerialName("join")
data class JoinMatch(val matchID: String) : MatchAction()

@Serializable
@SerialName("joined")
data class JoinedMatch(val player: Player, val match: Match) : MatchEvent()

@Serializable
@SerialName("left")
data class LeftMatch(val playerID: String, val match: Match) : MatchEvent()

@Serializable
@SerialName("start")
object Start : MatchAction()

@Serializable
@SerialName("started")
data class MatchStarted(val match: Match) : MatchEvent()
