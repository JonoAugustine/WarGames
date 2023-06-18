package com.jonoaugustine.wargames.common.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.jonoaugustine.wargames.common.WgColor
import com.jonoaugustine.wargames.common.WgSize
import com.jonoaugustine.wargames.common.ecs.Replicated
import com.jonoaugustine.wargames.common.math.Vector
import kotlinx.serialization.Serializable

@Serializable
data class TransformCmpnt(var position: Vector, var rotation: Float = 0f) :
    Component<TransformCmpnt>, Replicated {

  override fun type() = TransformCmpnt

  companion object : ComponentType<TransformCmpnt>()
}

@Serializable
data class SpriteCmpnt(val size: WgSize, val color: WgColor) :
    Component<SpriteCmpnt>, Replicated {

  override fun type() = SpriteCmpnt

  companion object : ComponentType<SpriteCmpnt>()
}

infix fun TransformCmpnt.centeredOn(sprite: SpriteCmpnt): Vector =
  Vector(position.x - sprite.size.width / 2, position.y - sprite.size.height / 2)

