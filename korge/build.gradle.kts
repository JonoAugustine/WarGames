import korlibs.korge.gradle.KorgeGradlePlugin
import korlibs.korge.gradle.Orientation.DEFAULT
import korlibs.korge.gradle.korge

buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven { url = uri("https://plugins.gradle.org/m2/") }
  }
  dependencies {
    classpath("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:${Versions.korge}")
  }
}

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

apply<KorgeGradlePlugin>()

korge {
  id = "${project.group}.${project.name}.korge".lowercase()
  version = project.version as String
  name = project.name
  orientation = DEFAULT

  targetJvm()
  serializationJson()

  dependencies {
    add("jvmMainImplementation", project(":common"))
    add("commonMainApi", "ch.qos.logback:logback-classic:1.2.3")
    add("commonMainApi", ktor.client("websockets"))
    add("jvmMainApi", ktor.client("cio"))
  }
}
