plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(11)
    androidTarget()
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform (JetBrains — supports Android, Desktop, iOS, Web)
                implementation(compose.ui)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)

                // Coroutines & DateTime (multiplatform)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)

                // Koin (core + compose are multiplatform)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                // Lifecycle ViewModel — JetBrains Multiplatform
                implementation(libs.lifecycle.viewmodel.ktx)
                implementation(libs.lifecycle.viewmodel.compose)

                // Coil 3 (multiplatform)
                implementation(libs.coil.compose)

                // SQLDelight runtime + coroutines extensions (multiplatform)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)

                // Navigation — JetBrains Multiplatform
                implementation(libs.navigation.compose)
            }
        }

        val androidMain by getting {
            dependencies {
                // Android-specific
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)

                // Koin Android (androidContext, etc.)
                implementation(libs.koin.android)

                // SQLDelight Android driver
                implementation(libs.sqldelight.android.driver)

                // Media3 / ExoPlayer
                implementation(libs.media3.exoplayer)
                implementation(libs.media3.ui)
                implementation(libs.media3.session)
                implementation(libs.media3.common)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
    }
}

android {
    namespace = "com.example.sonor.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

sqldelight {
    databases {
        create("SonorDatabase") {
            packageName.set("com.example.sonor.sqldelight")
        }
    }
}
