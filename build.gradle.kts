val kotlinVersion: String by project
val coroutinesVersion: String by project

plugins {
    java
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.dokka") version "1.8.10"
}

allprojects {
    group = "me.austin"
    version = "0.2.3"
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.dokka")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib", kotlinVersion))
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    kotlin {
        jvmToolchain(17)
    }

    tasks {
        named<Jar>("javadocJar") {
            from(named("dokkaJavadoc"))
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Test library
    testImplementation(kotlin("test", kotlinVersion))

    // We need this for the rubBlocking function
    testImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)

    // Subprojects
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