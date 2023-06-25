package com.jonoaugustine.wargames.common.ecs.entities

import kotlinx.serialization.Serializable

@Serializable
sealed interface WorldUnit

@Serializable
sealed interface FieldableUnit : WorldUnit

@Serializable
object CombatUnit : FieldableUnit
