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
    for (any in listOf(
        kotlin(module = "test"),
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-RC",
        project(path = ":eventbus"),
        project(path = ":lightweight"),
    )) {
        testImplementation(any)
    }
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