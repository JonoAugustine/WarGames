package com.jonoaugustine.wargames.server.ecs

import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import com.jonoaugustine.wargames.common.MatchID
import com.jonoaugustine.wargames.common.ecs.Replicated
import com.jonoaugustine.wargames.common.ecs.Snapshot
import com.jonoaugustine.wargames.common.network.missives.WorldUpdated
import com.jonoaugustine.wargames.server.managers.getConnection
import com.jonoaugustine.wargames.server.managers.getMatch
import com.jonoaugustine.wargames.server.send
import kotlinx.coroutines.runBlocking

class ServerReplicationSystem : IntervalSystem() {

  private val matchID: MatchID = inject()

  @Suppress("UNCHECKED_CAST")
  override fun onTick() {
    if (world.numEntities == 0) return
    // filter replicated components
    val snap = world.snapshot().mapValues { (_, v) -> v.filter { it is Replicated } }
    runBlocking {
      getMatch(matchID)!!.players.keys
        .mapNotNull { getConnection(it)?.session }
        .forEach { it.send(WorldUpdated(snap as Snapshot)) }
    }
  }
}
