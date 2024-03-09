val kotlinVersion: String by project
val coroutinesVersion: String by project

plugins {
    java
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.dokka") version "1.9.0"
    `maven-publish`
}

group = "me.austin"
version = "0.2.3"

repositories {
    mavenCentral()
}

dependencies {
    // Test library
    testImplementation(kotlin("test", kotlinVersion))

    // We need this for the rubBlocking function
    testImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)

    // Reflection library
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = kotlinVersion) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }

    // Coroutine library
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)

    // Standard library
    implementation(kotlin("stdlib", kotlinVersion))

    // Annotations
    compileOnly(group = "org.jetbrains", name = "annotations", version = "24.0.1")
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        testLogging.showStandardStreams = true
        useJUnitPlatform()
    }

    jar {
        into("META-INF") {
            from("LICENSE")
        }
    }

    named<Jar>("javadocJar") {
        from(named("dokkaJavadoc"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.austin"
            artifactId = "rush"
            version = "0.2.3"

            from(components["java"])
        }
    }
}