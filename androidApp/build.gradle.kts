plugins {
    id("com.android.application")
    kotlin("plugin.compose")
}

android {
    namespace = "io.github.t45k.githubDiary.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.t45k.githubDiary"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

dependencies {
    implementation(project(":app"))
}
