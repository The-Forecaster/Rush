plugins {
    java
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.dokka") version "1.8.10"
}

version = "2.2"
group = "me.austin"

dependencies {
    testImplementation(kotlin(module = "test"))

    testImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.7.0-RC")

    testImplementation(project(":eventbus"))

    testImplementation(project(":lightweight"))
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