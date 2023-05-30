allprojects {
    group = "me.austin"
    version = "0.2.3"
}

plugins {
    java
    kotlin("jvm") version "1.8.21"
}

val kotlinVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin(module = "test", version = kotlinVersion))
    testImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.7.0-RC")
    testImplementation(project(":eventbus"))
    testImplementation(project(":lightweight"))
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        testLogging.showStandardStreams = true
        useJUnitPlatform()
    }
}