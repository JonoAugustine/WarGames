package com.jonoaugustine.wargames.common

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: UserID, val name: String)
