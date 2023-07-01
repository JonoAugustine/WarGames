import korlibs.korge.gradle.KorgeGradlePlugin
import korlibs.korge.gradle.Orientation.DEFAULT
import korlibs.korge.gradle.korge
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

tasks.withType<KotlinCompile>().all {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xcontext-receivers")
    jvmTarget = "17"
  }
}

korge {
  id = "${project.group}.${project.name}.korge".lowercase()
  version = project.version as String
  name = project.name
  orientation = DEFAULT

  entryPoint = "main"
  jvmMainClassName = "${project.group}.wargames.korge.KorgeMainKt"

  targetJvm()
  serializationJson()

  dependencies {
    add("jvmMainImplementation", project(":common"))
    add("commonMainApi", "ch.qos.logback:logback-classic:1.2.3")
    add("commonMainApi", ktor.client("cio"))
    add("commonMainApi", ktor.client("websockets"))
    add("commonMainApi", ktor.client("content-negotiation"))
    add("commonMainApi", ktor("serialization-kotlinx-json"))
    add("commonMainApi", "io.github.quillraven.fleks:Fleks:${Versions.fleks}")
  }
}