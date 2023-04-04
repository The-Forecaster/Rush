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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
    }
    
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
    }

    testImplementation(kotlin("test"))

    compileOnly("org.jetbrains:annotations:24.0.1")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(17)
}
