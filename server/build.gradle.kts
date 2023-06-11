plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  application
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":common"))
  implementation(kotlin("stdlib-jdk8"))
  implementation(ktor("serialization-kotlinx-json"))
  implementation(ktor.server("core"))
  implementation(ktor.server("cio"))
  implementation(ktor.server("cors"))
  implementation(ktor.server("content-negotiation"))
  implementation(ktor.server("websockets"))
  implementation(ktor.server("auth"))
  implementation(ktor.server("call-logging"))
  implementation("ch.qos.logback:logback-classic:${Versions.logback}")
  implementation("io.ktor:ktor-server-cors-jvm:2.3.1")
  implementation("io.ktor:ktor-server-core-jvm:2.3.1")
  implementation("io.ktor:ktor-server-websockets-jvm:2.3.1")
  implementation("io.ktor:ktor-server-sessions-jvm:2.3.1")
  implementation("io.ktor:ktor-server-auth-jvm:2.3.1")

  testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.1")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.21")
}

application {
  // dynamically set main class name
  mainClass.set("${group}.${rootProject.name}.$name.ServerKt")
  // set development mode
  if (project.ext["production"] as Boolean? != true) {
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
  }
}