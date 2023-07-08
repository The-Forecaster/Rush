val kotlinVersion: String by project
val coroutinesVersion: String by project

dependencies {
    // Standard library
    implementation(kotlin("stdlib", kotlinVersion))

    // Reflection library
    implementation(kotlin("reflect", kotlinVersion))

    // Coroutine library
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
}