val kotlinVersion: String by project
val coroutinesVersion: String by project

dependencies {
    // Reflection library
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = kotlinVersion) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }

    // Coroutine library
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
}