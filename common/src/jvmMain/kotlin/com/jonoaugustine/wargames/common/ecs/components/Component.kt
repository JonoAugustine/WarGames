package com.jonoaugustine.wargames.common.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.jonoaugustine.wargames.common.UserID
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.ecs.Replicated
import kotlinx.serialization.Serializable

@Serializable
data class PlayerCmpnt(
  var id: UserID,
  var name: String,
  var color: WgColor,
) : Component<PlayerCmpnt>, Replicated {

  override fun type() = PlayerCmpnt

  companion object : ComponentType<PlayerCmpnt>()
}

@Serializable
data class MapCmpnt(var background: WgColor) : Component<MapCmpnt>, Replicated {

  override fun type() = MapCmpnt

  companion object : ComponentType<MapCmpnt>()
}

