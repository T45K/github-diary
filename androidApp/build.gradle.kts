plugins {
    id("com.android.application") version "9.0.0"
    kotlin("android") version "2.3.0"
    kotlin("plugin.compose") version "2.3.0"
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "io.github.t45k.githubDiary"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.t45k.githubDiary"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":app"))
    implementation("androidx.activity:activity-compose:1.12.2")
    
    // Compose
    implementation("org.jetbrains.compose.material:material:1.10.0")
    
    // Koin
    implementation(platform("io.insert-koin:koin-bom:4.1.1"))
    implementation("io.insert-koin:koin-core")
}
