package com.jonoaugustine.wargames.server.ecs

import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import com.jonoaugustine.wargames.common.LobbyID
import com.jonoaugustine.wargames.common.ecs.replicationSnapshot
import com.jonoaugustine.wargames.common.network.missives.WorldUpdated
import com.jonoaugustine.wargames.server.managers.getConnection
import com.jonoaugustine.wargames.server.managers.getLobby
import com.jonoaugustine.wargames.server.send
import kotlinx.coroutines.runBlocking

class ServerReplicationSystem : IntervalSystem() {

  private val lobbyID: LobbyID = inject("lobby.id")

  @Suppress("UNCHECKED_CAST")
  override fun onTick() {
    if (world.numEntities == 0) return
    // filter replicated components
    runBlocking {
      getLobby(lobbyID)!!.players.keys
        .mapNotNull { getConnection(it)?.session }
        .forEach { it.send(WorldUpdated(world.replicationSnapshot())) }
    }
  }
}
