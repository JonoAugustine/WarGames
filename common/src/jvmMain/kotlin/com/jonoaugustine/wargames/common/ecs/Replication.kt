package com.jonoaugustine.wargames.common.ecs

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

typealias Snapshot = Map<Entity, List<Replicated>>

@Suppress("UNCHECKED_CAST")
fun World.replicationSnapshot(): Snapshot =
  snapshot().mapValues { (_, v) -> v.filter { it is Replicated } } as Snapshot

interface Replicated
