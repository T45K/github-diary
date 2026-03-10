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

fun hasXcodeBuild(): Boolean = runCatching {
    val process = ProcessBuilder("/usr/bin/xcrun", "xcodebuild", "-version")
        .redirectErrorStream(true)
        .start()
    process.waitFor() == 0
}.getOrDefault(false)

val desktopJavaLanguageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
val desktopPackagingJavaHome = javaToolchains.launcherFor {
    languageVersion.set(desktopJavaLanguageVersion)
}.map { it.metadata.installationPath.asFile.absolutePath }
val enableAppleTargets = hasXcodeBuild()

kotlin {
    jvmToolchain {
        languageVersion.set(desktopJavaLanguageVersion)
    }

    android {
        namespace = "io.github.t45k.githubDiary"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }

    jvm("desktop")

    if (enableAppleTargets) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()

        targets.withType<KotlinNativeTarget> {
            binaries.framework {
                baseName = "GitHubDiary"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.navigation3.ui)

            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.arrow.core)

            implementation(libs.jetbrains.markdown)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        if (enableAppleTargets) {
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.cio)
                implementation(libs.logback.classic)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.junit.jupiter)
                implementation(libs.junit.platform.launcher)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.t45k.githubDiary.MainKt"
        javaHome = desktopPackagingJavaHome.get()

        nativeDistributions {
            targetFormats(Dmg)
            packageName = "GitHub Diary"
            packageVersion = libs.versions.appVersionName.get()

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
