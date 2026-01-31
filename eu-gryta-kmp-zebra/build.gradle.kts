import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech)
}

group = "eu.gryta"
val library: String = "zebra"

val versions = Properties().apply {
    load(file("version.properties").inputStream())
}

val baseVersion = versions.getProperty("version")
val isCi = System.getenv("GITHUB_ACTIONS") == "true"

val appVersion = if (isCi) {
    baseVersion
} else {
    "${bumpPatchVersion(baseVersion)}-SNAPSHOT"
}

version = appVersion

kotlin {
    jvmToolchain(25)

    androidLibrary {
        namespace = "$group.$library"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        }
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // iOS targets configured without CocoaPods
    // MLKit can be linked via Swift Package Manager or manually
    // For now, iOS uses stub implementation (BarcodeScanner.ios.kt)
    // To enable MLKit: Configure cinterop manually or use ZXing alternative

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.zxing.core)
        }

        androidMain.dependencies {
            implementation(libs.mlkit.barcode.scanning)
        }

        jvmMain.dependencies {
            implementation(libs.zxing.javase)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

fun bumpPatchVersion(version: String): String {
    val parts = version.split(".")
    if (parts.size != 3) return version
    val patch = parts[2].toIntOrNull() ?: return version
    return "${parts[0]}.${parts[1]}.${patch + 1}"
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/rgryta/KMP-Zebra")
            credentials {
                username =
                    project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_TOKEN")
            }
        }
        mavenLocal()
    }
}

mavenPublishing {
    coordinates(
        groupId = group.toString(),
        artifactId = library,
        version = version.toString()
    )

    pom {
        name.set("Zebra - Kotlin Multiplatform Barcode Library")
        description.set("Cross-platform barcode scanning and generation library for Android, iOS, and Desktop (JVM)")
        inceptionYear.set("2025")
        url.set("https://github.com/rgryta/KMP-Zebra")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("rgryta")
                name.set("Radosław Gryta")
                email.set("radek.gryta@gmail.com")
                url.set("https://github.com/rgryta/")
            }
        }

        scm {
            url.set("https://github.com/rgryta/KMP-Zebra")
            connection.set("scm:git:git://github.com/rgryta/KMP-Zebra.git")
            developerConnection.set("scm:git:ssh://git@github.com/rgryta/KMP-Zebra.git")
        }
    }
}
