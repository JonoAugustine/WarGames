package com.jonoaugustine.wargames.server

import com.jonoaugustine.wargames.common.Match
import java.util.Collections.synchronizedMap
import java.util.UUID

private val matches: MutableMap<String, Match> = synchronizedMap(mutableMapOf())

fun getMatch(id: String): Match? = synchronized(matches) { matches[id] }

fun newMatch(): Match = Match(
  id = UUID.randomUUID().toString(),
  state = Match.State.PLACING,
  players = emptyMap(),
  entities = emptyList()
)

