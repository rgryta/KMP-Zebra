plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "eu.gryta.zebra.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "eu.gryta.zebra.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Use debug signing for sample app releases
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Zebra library
    implementation(project(":eu-gryta-kmp-zebra"))

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.10.2")

    // Image loading for displaying generated barcodes
    implementation(libs.coil.compose)

    // CameraX for scanning
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
}
