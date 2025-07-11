import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            // 🎨 Compose Core
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // 🔄 Lifecycle & ViewModel
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // 🔗 Shared Module
            implementation(projects.shared)

            // ⚡ Coroutines - versión compatible
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

            // 🎯 Material Icons Extended
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

            // 📄 Serialización - versión compatible
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

            // 📅 DateTime - versión compatible
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

            // 🌐 HTTP Client - versiones estables
            implementation("io.ktor:ktor-client-core:2.3.12")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
            implementation("io.ktor:ktor-client-logging:2.3.12")

            // 🧭 Navigation - versión estable
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")

            // 🎯 ViewModel Compose - versión compatible
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

            // 💾 DataStore para persistencia (añadido)
            implementation("androidx.datastore:datastore-preferences-core:1.1.1")

        }


        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        desktopMain.dependencies {
            // Desktop Compose
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // HTTP Client para Desktop - versión estable
            implementation("io.ktor:ktor-client-cio:2.3.12")

            // Google OAuth para Desktop
            implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")

            // Servidor local temporal para OAuth callback
            implementation("io.ktor:ktor-server-netty:2.3.12")
            implementation("io.ktor:ktor-server-core:2.3.12")
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}