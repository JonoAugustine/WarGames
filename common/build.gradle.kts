plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}


kotlin {
  jvm {
    compilations.all { kotlinOptions.jvmTarget = "17" }
    withJava()
    testRuns["test"].executionTask.configure { useJUnitPlatform() }
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(kotlinx("coroutines-core", Versions.coroutines))
        implementation(kotlinx("serialization-json", Versions.serialization))
      }
    }
  }
}
