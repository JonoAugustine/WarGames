import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
}

group = "com.jonoaugustine"
version = "1.0"

repositories {
  google()
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
  targets {
    all {
      compilations {
        all {
          compilerOptions.configure {
            freeCompilerArgs.add("-Xcontext-receivers")
          }
        }
      }
    }
  }
  jvm {
    jvmToolchain(11)
    withJava()
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
        implementation("org.jetbrains.kotlinx:multik-core:0.2.2")
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
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
      packageName = "wargames"
      packageVersion = "1.0.0"
    }
  }
}
