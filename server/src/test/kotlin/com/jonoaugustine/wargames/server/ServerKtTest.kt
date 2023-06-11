package com.jonoaugustine.wargames.server

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.server.testing.testApplication
import kotlin.test.Test

class ServerKtTest {

  @Test
  fun testGetApiLobbies() = testApplication {
    application {
      configuration()
    }
    val response = client.get("/api/lobbies")
    assert(response.body<Any>() is Collection<*>)
  }
}
