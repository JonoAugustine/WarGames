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
  implementation(ktor.server("call-logging"))
  implementation("ch.qos.logback:logback-classic:${Versions.logback}")
  implementation("io.ktor:ktor-server-cors-jvm:2.3.1")
  implementation("io.ktor:ktor-server-core-jvm:2.3.1")
  implementation("io.ktor:ktor-server-websockets-jvm:2.3.1")
}

application {
  mainClass.set("${group}.${rootProject.name}.$name.ServerKt")
  if (project.ext["production"] as Boolean? != true) {
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
  }
}
