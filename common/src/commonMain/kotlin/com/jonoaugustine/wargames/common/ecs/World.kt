package com.jonoaugustine.wargames.common.ecs

import com.jonoaugustine.wargames.common.entities.EntityID
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@JvmInline
@Serializable
value class ECSEntity(val id: EntityID)

context(WorldState)
inline fun <reified T : Component> ECSEntity.getComponent(): T? =
  entityComponentMap[id]?.filterIsInstance<T>()?.first()

interface System {
  val subjects: Set<KClass<out Component>>?

  context(WorldState)
  fun update(delta: Float): WorldState
}

interface Component

@Serializable
data class WorldState(
  val entities: List<ECSEntity>,
  val components: Map<KClass<out Component>, Set<Component>>,
  val componentEntityMap: Map<KClass<out Component>, Set<ECSEntity>>,
  val entityComponentMap: Map<EntityID, Set<Component>>,
  val systems: Set<System>,
)

inline fun <reified T : Component> WorldState.all(): Set<ECSEntity> =
  componentEntityMap[T::class] ?: emptySet()

fun WorldState.entityOf(block: ECSEntity.() -> Unit): ECSEntity =
  ECSEntity(entities.size.toString(12)).apply(block)
