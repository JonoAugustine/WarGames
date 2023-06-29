object Versions {

  const val kotlin = "1.8.20"
  const val coroutines = "1.7.1"
  const val serialization = "1.5.1"
  const val ktor = "2.3.1"
  const val logback = "1.4.7"
  const val compose = "1.4.2"
  const val fleks = "SNAPSHOT"
  const val korge = "4.0.7"
}

fun kotlinx(project: String, version: String) = """org.jetbrains.kotlinx:kotlinx-$project:$version"""

object ktor {
  operator fun invoke(module: String, version: String = Versions.ktor) =
    """io.ktor:ktor-$module:$version"""
  fun server(module: String, version: String = Versions.ktor) =
    invoke("server-$module", version)
  fun client(module: String, version: String = Versions.ktor) =
    invoke("client-$module", version)
}
