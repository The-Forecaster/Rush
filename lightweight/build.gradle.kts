plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.dokka") version "1.8.10"
}

group = "me.austin"
version = "0.0.1"

dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.8.20") {
        exclude(module = "kotlin-stdlib")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(17)
}