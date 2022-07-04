import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.7.0"
    id("org.jetbrains.dokka") version "1.7.0"
}

version "2.1"
group "me.austin.rush"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.jodah:typetools:0.6.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
    }

    implementation("org.jetbrains.kotlin:kotlin-test") {
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

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<JavaCompile> {
        targetCompatibility = "17"
        sourceCompatibility = "17"
        options.encoding = "UTF-8"
    }

    named<Jar>("javadocJar") {
        from(named("dokkaJavadoc"))
    }
}
