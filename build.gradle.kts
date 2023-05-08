import org.jetbrains.kotlin.ir.backend.js.compile

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
    }

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.8.20") {
        exclude(module = "kotlin-stdlib")
    }

    compileOnly(project(":lightweight"))
}

java {
    withSourcesJar()
    withJavadocJar()
}

task("copyLicense") {
    outputs.file(File("$buildDir/LICENSE"))
    doLast {
        copy {
            from("LICENSE")
            into("$buildDir")
        }
    }
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
