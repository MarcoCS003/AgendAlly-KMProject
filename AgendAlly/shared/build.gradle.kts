
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    
}

kotlin {
    iosX64()
    iosArm64()

    iosSimulatorArm64()
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

