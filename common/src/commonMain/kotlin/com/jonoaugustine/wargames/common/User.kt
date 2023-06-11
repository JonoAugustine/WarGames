package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable

typealias UserID = String

@Serializable
data class User(val id: UserID, val name: String)
