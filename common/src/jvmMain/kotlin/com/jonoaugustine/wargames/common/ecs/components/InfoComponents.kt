package com.jonoaugustine.wargames.common.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.jonoaugustine.wargames.common.ecs.Replicated
import kotlinx.serialization.Serializable

@Serializable
data class OwnerCmpnt(var ownerID: String) : Component<OwnerCmpnt>, Replicated {

  override fun type() = OwnerCmpnt

  companion object : ComponentType<OwnerCmpnt>()
}

@JvmInline
@Serializable
value class NameCmpnt(val name: String) : Component<NameCmpnt>, Replicated {

  override fun type() = NameCmpnt

  companion object : ComponentType<NameCmpnt>()
}
