package com.jonoaugustine.wargames.common.network.missives

import com.jonoaugustine.wargames.common.Entity
import com.jonoaugustine.wargames.common.Match
import com.jonoaugustine.wargames.common.Player
import com.jonoaugustine.wargames.common.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Actions are made by the client. CLIENT -> SERVER */
@Serializable
sealed interface Action

/** Events are distributed by the server. SERVER -> CLIENT */
@Serializable
sealed interface Event
/*
 * Errors
 */

@Serializable
@SerialName("error")
open class ErrorEvent(val message: String = "an unknown error occurred") : Event

@Serializable
sealed interface UserAction : Action

@Serializable
sealed interface UserEvent : Event {

  val user: User
}

@Serializable
@SerialName("connected")
data class UserConnected(override val user: User) : UserEvent

@Serializable
@SerialName("update.name")
data class UpdateUsername(val name: String) : UserAction

@Serializable
@SerialName("update.name")
data class UserUpdated(override val user: User) : UserEvent
