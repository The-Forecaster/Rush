plugins {
    kotlin("jvm") version "1.8.22"
    id("org.jetbrains.dokka") version "1.8.10"
}

repositories {
    mavenCentral()
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