plugins {
    java
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.dokka") version "1.8.10"
}

version = "2.2"
group = "me.austin"

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

allprojects {
    repositories {
        mavenCentral()
    }
}

kotlin {
    jvmToolchain(17)
}