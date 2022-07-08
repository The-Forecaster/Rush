import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.7.0"
}

version = "2.1.1"
group = "me.austin.rush"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("net.jodah:typetools:0.6.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
    }
    
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.0") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.0") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
    }

    compileOnly("org.jetbrains:annotations:23.0.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)

        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}