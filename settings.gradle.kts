rootProject.name = "zebra"

include(":eu-gryta-kmp-zebra")
include(":sample")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
