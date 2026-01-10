plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.native.cocoapods) apply false
    alias(libs.plugins.vanniktech) apply false
    alias(libs.plugins.versions)
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any {
            candidate.version.uppercase().contains(it)
        }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(candidate.version)
        isStable.not()
    }
}
