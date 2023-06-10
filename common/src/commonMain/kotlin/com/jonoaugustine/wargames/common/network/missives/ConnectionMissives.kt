package com.jonoaugustine.wargames.common.network.missives

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ConnectionEvent : Event

@Serializable
@SerialName("connection.open")
data class ConnectionOpened(val address: String) : ConnectionEvent
