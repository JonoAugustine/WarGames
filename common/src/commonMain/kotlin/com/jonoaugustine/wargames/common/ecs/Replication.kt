package com.jonoaugustine.wargames.common.ecs

import com.github.quillraven.fleks.Entity

typealias Snapshot = Map<Entity, List<Replicated>>

interface Replicated
