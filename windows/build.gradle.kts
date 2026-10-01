plugins {
    id 'org.jetbrains.kotlin.jvm'
    id 'org.jetbrains.compose'
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
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
        }
    }
}
