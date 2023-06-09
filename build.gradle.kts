import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val production = System.getenv("ENV")
  ?.contains("PROD", true)
  ?: false

plugins {
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
}

allprojects {
  group = "com.jonoaugustine"
  version = extra["wargames.version"] as String
  extra["production"] = production

  repositories {
    mavenCentral()
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
  }
}

