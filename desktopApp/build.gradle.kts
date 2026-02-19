import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.animation)
    implementation(compose.foundation)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
}

compose.desktop {
    application {
        mainClass = "com.discordtokenget.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "Discord Token"
            packageVersion = "1.0.0"
            description = "Discord Token Extractor"
            copyright = "© 2024 Rhyan57"

            windows {
                menuGroup = "Discord Token"
                upgradeUuid = "2D1A4B9C-E3F7-4A88-B012-9C3D5E7F8A01"
                iconFile.set(project.file("src/main/resources/icon.ico"))
                shortcut = true
                perUserInstall = true
                dirChooser = true
            }

            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
            }

            macOS {
                iconFile.set(project.file("src/main/resources/icon.icns"))
                bundleID = "com.discordtokenget"
            }
        }
    }
}
