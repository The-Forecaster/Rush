plugins {
    java
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.dokka") version "1.8.10"
}

version = "2.2"
group = "me.austin"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin(module = "test"))

    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.7.0-RC") {
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-common")
        isTransitive = false
    }

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.8.20") {
        exclude(module = "kotlin-stdlib")
        isTransitive = false
    }

    compileOnly(group = "org.jetbrains", name = "annotations", version = "24.0.1")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    test {
        testLogging.showStandardStreams = true
        useJUnitPlatform()
    }

    named<Jar>("javadocJar") {
        from(named("dokkaJavadoc"))
    }
}

kotlin {
    jvmToolchain(17)
}
