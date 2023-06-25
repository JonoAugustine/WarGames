plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}


kotlin {
  jvm {
    withJava()
    jvmToolchain(17)
    compilations.all { kotlinOptions.jvmTarget = "17" }
    testRuns["test"].executionTask.configure { useJUnitPlatform() }
  }

  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(kotlinx("coroutines-core", Versions.coroutines))
        implementation(kotlinx("serialization-json", Versions.serialization))
        implementation("io.github.quillraven.fleks:Fleks:${Versions.fleks}")
      }
    }
  }
}
