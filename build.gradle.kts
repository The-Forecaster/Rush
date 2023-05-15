allprojects {
    group = "me.austin"
    version = "0.2.2"
}

plugins {
    java
    kotlin("jvm") version "1.8.20"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin(module = "test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-RC")
    testImplementation(project(path = ":eventbus"))
    testImplementation(project(path = ":lightweight"))
}

tasks {
    test {
        testLogging.showStandardStreams = true
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(17)
}