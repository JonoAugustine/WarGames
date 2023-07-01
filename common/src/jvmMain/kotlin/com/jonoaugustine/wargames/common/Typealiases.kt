package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable

@Deprecated("will be replaced with value class")
typealias ID = String
@Deprecated("will be replaced with value class")
typealias UserID = ID
@Deprecated("will be replaced with value class")
typealias LobbyID = ID
@Deprecated("will be replaced with value class")
typealias MatchID = ID

@Serializable
sealed interface Id {

  val value: Int
}

@JvmInline
@Serializable
value class LobbyId(override val value: Int) : Id

@JvmInline
@Serializable
value class UserId(override val value: Int) : Id
