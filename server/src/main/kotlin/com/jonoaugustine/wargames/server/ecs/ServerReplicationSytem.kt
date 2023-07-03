package com.jonoaugustine.wargames.server.ecs

import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.inject
import com.jonoaugustine.wargames.common.LobbyID
import com.jonoaugustine.wargames.common.ecs.replicationSnapshot
import com.jonoaugustine.wargames.common.network.missives.WorldUpdated
import com.jonoaugustine.wargames.server.managers.getConnection
import com.jonoaugustine.wargames.server.managers.getLobby
import com.jonoaugustine.wargames.server.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

class ServerReplicationSystem : IntervalSystem() {

  private val lobbyID: LobbyID = inject("lobby.id")
  private val scope: CoroutineScope = inject()

  override fun onTick() = runBlocking(scope.coroutineContext) {
    if (world.numEntities == 0) return@runBlocking // !NEEDED!
    val result = world.replicationSnapshot()
    if (result.isFailure) {
      TODO("handle failed snapshot")
      return@runBlocking
    }
    val updateEvent = WorldUpdated(result.getOrThrow())
    getLobby(lobbyID)!!
      .players
      .mapNotNull { getConnection(it.key)?.session }
      .forEach { it.send(updateEvent) }
  }
}
