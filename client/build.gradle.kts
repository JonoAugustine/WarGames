import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("org.jetbrains.compose")
}

repositories {
  google()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
  jvm {
    jvmToolchain(11)
    withJava()
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(project(":common"))
        implementation(kotlinx("coroutines-core", "1.7.1"))
        implementation(ktor("serialization-kotlinx-json"))
        implementation(ktor.client("core"))
        implementation(ktor.client("cio"))
        implementation(ktor.client("websockets"))
        implementation(ktor.client("logging"))
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation("ch.qos.logback:logback-classic:${Versions.logback}")
      }
    }
    val jvmTest by getting
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = rootProject.name
      packageVersion = rootProject.version as String
    }
  }
}
