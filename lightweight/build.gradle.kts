plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.dokka") version "1.8.10"
}

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    named<Jar>("javadocJar") {
        from(named("dokkaJavadoc"))
    }
}