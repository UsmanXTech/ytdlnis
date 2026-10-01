plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "com.deniscerri.ytdl.windows"
version = "0.1.0"

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}

compose.desktop {
    application {
        mainClass = "com.deniscerri.ytdl.windows.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageName = "YTDLnis"
            packageVersion = "0.1.0"
            description = "YTDLnis Windows desktop downloader"
            vendor = "YTDLnis"
            modules("java.net.http")
            windows {
                menu = true
                perUserInstall = true
                dirChooser = true
                upgradeUuid = "9c6f4f6b-8d9a-4f35-9b62-7f8e0a1c4d25"
            }
        }
    }
}
