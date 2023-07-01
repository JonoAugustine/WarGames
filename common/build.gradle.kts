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


        //implementation("com.soywiz.korlibs.kbox2d:kbox2d:3.3.0")
        //implementation("org.jbox2d:jbox2d-library:2.2.1.1")
        //implementation("org.jbox2d:jbox2d-serialization:2.2.1.1")
        //implementation("com.google.protobuf:protobuf-java:3.23.0")
      }
    }
  }
}
