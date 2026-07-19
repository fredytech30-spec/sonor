plugins {
    kotlin("jvm")
}

group = "com.example.sonor"
version = "1.0"

dependencies {
    implementation(project(":shared"))
    implementation(libs.koin.core)
}

// Compose Desktop is temporarily disabled to unblock Android/shared compilation.
