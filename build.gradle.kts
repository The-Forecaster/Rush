plugins {
    java
    kotlin("jvm") version "1.8.20"
}

version = "2.2"
group = "me.austin"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin(module = "test"))

    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.6.4") {
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-common")
    }

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.8.20") {
        exclude(module = "kotlin-stdlib")
    }

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = "1.8.20") {
        exclude(module = "annotations")
        exclude(module = "kotlin-stdlib-common")
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
}

kotlin {
    jvmToolchain(17)
}
