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
    mavenLocal()
  }

  tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
  }
}

