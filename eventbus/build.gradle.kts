plugins {
    kotlin("jvm") version "1.8.21"
    id("org.jetbrains.dokka") version "1.8.10"
}

val kotlinVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.7.0-RC")
    implementation(kotlin(module = "reflect", version = kotlinVersion))
    implementation(kotlin(module = "stdlib-jdk8", version = kotlinVersion))
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