import org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.power-assert") version "2.3.0"

    kotlin("plugin.compose") version "2.3.0"
    id("org.jetbrains.compose") version "1.10.0"

    kotlin("plugin.serialization") version "2.3.0"
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
    implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.0-alpha01")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    implementation("io.ktor:ktor-client-core:3.3.3")
    implementation("io.ktor:ktor-client-cio:3.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
    implementation("io.ktor:ktor-client-logging:3.3.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0-RC")

    implementation("ch.qos.logback:logback-classic:1.5.23")

    implementation("io.arrow-kt:arrow-core:2.2.0")

    // Koin for Compose Desktop
    implementation(platform("io.insert-koin:koin-bom:4.1.1"))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-compose")
    implementation("io.insert-koin:koin-compose-viewmodel")

    testImplementation(kotlin("test"))
    testImplementation("io.insert-koin:koin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.3")
    testImplementation("io.ktor:ktor-client-mock:3.3.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

compose.desktop {
    application {
        mainClass = "io.github.t45k.githubDiary.MainKt"

        nativeDistributions {
            targetFormats(Dmg)
            packageName = "GitHub Diary"
            packageVersion = "1.0.1"

            macOS {
                bundleID = "io.github.t45k.github-diary"
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
