import org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.power-assert")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    android {
        namespace = "io.github.t45k.githubDiary"
        compileSdk = 36
        minSdk = 26

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }

    jvm("desktop")

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<KotlinNativeTarget> {
        binaries.framework {
            baseName = "GitHubDiary"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)

            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
            implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.0-alpha01")

            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

            implementation("io.ktor:ktor-client-core:3.4.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.1")
            implementation("io.ktor:ktor-client-logging:3.4.1")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0-RC")

            implementation("io.arrow-kt:arrow-core:2.2.2")

            // Koin
            implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.1.1"))
            implementation("io.insert-koin:koin-core")
            implementation("io.insert-koin:koin-compose")
            implementation("io.insert-koin:koin-compose-viewmodel")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("io.insert-koin:koin-test")
            implementation("org.junit.jupiter:junit-jupiter:6.0.3")
            implementation("io.ktor:ktor-client-mock:3.4.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }

        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.4.1")
        }

        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.4.1")
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
                implementation("io.ktor:ktor-client-cio:3.4.1")
                implementation("ch.qos.logback:logback-classic:1.5.25")
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation("org.junit.platform:junit-platform-launcher:6.0.3")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.t45k.githubDiary.MainKt"

        nativeDistributions {
            targetFormats(Dmg)
            packageName = "GitHub Diary"
            packageVersion = "1.0.1"

            modules("java.instrument", "java.management", "java.naming", "java.prefs", "java.sql", "jdk.unsupported", "java.xml")

            macOS {
                bundleID = "io.github.t45k.github-diary"
                dockName = "GitHub Diary"
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
