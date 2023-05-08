plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.dokka") version "1.8.10"
}

group = "me.austin"
version = "0.2.2"

dependencies {
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.7.0-RC") {
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-common")
    }

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

tasks {
    named<Jar>("javadocJar") {
        from(named("dokkaJavadoc"))
    }
}