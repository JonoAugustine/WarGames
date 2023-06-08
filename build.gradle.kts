import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("multiplatform") apply false
  kotlin("plugin.serialization") apply false
}

allprojects {
  group = "com.jonoaugustine"
  version = extra["wargames.version"] as String

  repositories {
    mavenCentral()
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
  }
}

