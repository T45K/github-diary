import org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "2.3.0"
    kotlin("plugin.power-assert") version "2.3.0"
    kotlin("plugin.compose") version "2.3.0"
    id("org.jetbrains.compose") version "1.10.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("com.android.application") version "8.12.3"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    jvmToolchain(21)

    androidTarget()

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
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)

                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
                implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.0-alpha01")

                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

                implementation("io.ktor:ktor-client-core:3.3.3")
                implementation("io.ktor:ktor-client-content-negotiation:3.3.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
                implementation("io.ktor:ktor-client-logging:3.3.3")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0-RC")

                implementation("io.arrow-kt:arrow-core:2.2.1.1")

                // Koin
                implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.1.1"))
                implementation("io.insert-koin:koin-core")
                implementation("io.insert-koin:koin-compose")
                implementation("io.insert-koin:koin-compose-viewmodel")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.insert-koin:koin-test")
                implementation("org.junit.jupiter:junit-jupiter:6.0.2")
                implementation("io.ktor:ktor-client-mock:3.3.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:3.3.3")
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.3.3")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
                implementation("io.ktor:ktor-client-cio:3.3.3")
                implementation("ch.qos.logback:logback-classic:1.5.25")
            }
        }

        val iosX64Main by getting {
            dependsOn(iosMain)
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val desktopTest by getting {
            dependencies {
                implementation("org.junit.platform:junit-platform-launcher:6.0.2")
            }
        }

        val iosTest by creating {
            dependsOn(commonTest)
        }

        val iosX64Test by getting {
            dependsOn(iosTest)
        }

        val iosArm64Test by getting {
            dependsOn(iosTest)
        }

        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.t45k.githubDiary.MainKt"

        nativeDistributions {
            javaHome = System.getProperty("java.home")
            targetFormats(Dmg)
            packageName = "GitHub Diary"
            packageVersion = "1.0.1"

            modules("java.instrument", "java.management", "java.naming", "java.prefs", "java.sql", "jdk.unsupported")

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
