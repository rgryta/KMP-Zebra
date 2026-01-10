# TODO: KMP-Zebra Library Improvements

This document lists structural and organizational improvements for the KMP-Zebra library based on comparison with Compose-Elements best practices.

---

## 🔴 High Priority (Production Readiness)

### 1. Add CI/CD Pipeline
**Problem:** No automated workflows - releases are manual and error-prone.

**Action Items:**
- [ ] Create `.github/workflows/tag.yaml` - Auto-tag on version bump
  - Trigger: Push to `main` branch
  - Check if `zebra/version.properties` changed
  - Create git tag matching new version
  - Use action: `rgryta/Check-Bump@main` or similar

- [ ] Create `.github/workflows/release.yaml` - Auto-release creation
  - Trigger: Tag push matching `v*` pattern
  - Generate changelog from commits
  - Create GitHub release with notes

- [ ] Create `.github/workflows/publish.yaml` - Publish to Maven
  - Trigger: Release published event
  - Build on macOS runner (for iOS support)
  - Run `./gradlew publishAllPublicationsToGitHubPackagesRepository`
  - Requires secrets: `GPR_USERNAME`, `GPR_TOKEN`

**Impact:** Eliminates manual release steps, reduces human error, enables continuous delivery.

**Reference:** See Compose-Elements workflows at `/config/workspace/WellMate/Compose-Elements/.github/workflows/`

---

### 2. Migrate to venniktech Maven Publishing Plugin
**Problem:** Manual publishing configuration is verbose and lacks features like signing.

**Current State:**
```kotlin
// zebra/build.gradle.kts
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/...")
            credentials { ... }
        }
    }
}
```

**Action Items:**
- [ ] Add plugin to root `build.gradle.kts`:
```kotlin
plugins {
    id("com.vanniktech.maven.publish") version "0.31.0" apply false
}
```

- [ ] Replace publishing block in `zebra/build.gradle.kts`:
```kotlin
plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    coordinates("eu.gryta", "kmp-zebra", version)

    pom {
        name.set("KMP-Zebra")
        description.set("Kotlin Multiplatform barcode scanning and generation library")
        url.set("https://github.com/rgryta/KMP-Zebra")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("rgryta")
                name.set("Your Name")
                email.set("your.email@example.com")
            }
        }

        scm {
            url.set("https://github.com/rgryta/KMP-Zebra")
            connection.set("scm:git:git://github.com/rgryta/KMP-Zebra.git")
            developerConnection.set("scm:git:ssh://git@github.com/rgryta/KMP-Zebra.git")
        }
    }

    publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
    signAllPublications()
}
```

- [ ] Remove old publishing configuration
- [ ] Test local publishing: `./gradlew publishToMavenLocal`

**Impact:** Industry-standard publishing, automatic POM generation, signing support, less boilerplate.

---

### 3. Optimize Gradle Build Configuration
**Problem:** Build configuration lacks modern optimization features.

**Action Items:**
- [ ] Update `gradle.properties`:
```properties
# Increase heap for KMP builds
org.gradle.jvmargs=-Xmx4096m

# Enable modern Gradle features
org.gradle.configuration-cache=true
org.gradle.caching=true

# Android optimizations
android.useAndroidX=true
android.nonTransitiveRClass=true

# Kotlin settings
kotlin.code.style=official
kotlin.mpp.stability.nowarn=true
```

- [ ] Test build performance: `./gradlew clean build --scan`
- [ ] Monitor configuration cache hits in subsequent builds

**Impact:** Faster builds (30-50% improvement with cache), reduced memory issues, smaller APK size.

---

### 4. Add Comprehensive Testing
**Problem:** `commonTest/` directory exists but is empty - no test coverage.

**Action Items:**
- [ ] Create `zebra/src/commonTest/kotlin/eu/gryta/zebra/BarcodeFormatTest.kt`
  - Test format enum completeness
  - Test format string representations

- [ ] Create `zebra/src/jvmTest/kotlin/eu/gryta/zebra/BarcodeGeneratorTest.kt`
  - Test QR code generation (ZXing backend)
  - Test various formats (Code128, EAN13, etc.)
  - Verify image dimensions match config
  - Test error handling for invalid inputs

- [ ] Create `zebra/src/jvmTest/kotlin/eu/gryta/zebra/BarcodeScannerTest.kt`
  - Generate barcode, then scan it (round-trip test)
  - Test multiple formats
  - Test `NotFound` result for invalid images
  - Test `Error` result for corrupted images

- [ ] Create `zebra/src/androidUnitTest/kotlin/eu/gryta/zebra/BarcodeConfigTest.kt`
  - Test config presets (fast/default/accurate)
  - Verify timeout values
  - Test error correction levels

- [ ] Add test coverage reporting:
```kotlin
// zebra/build.gradle.kts
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
    }
}
```

**Impact:** Catch regressions, validate platform implementations, build confidence for users.

**Note:** iOS tests require physical device/simulator - focus on JVM tests initially.

---

## 🟡 Medium Priority (Developer Experience)

### 5. Rename Module Directory to Match Maven Coordinates
**Problem:** Module directory `zebra/` doesn't match published artifact name, causing confusion.

**Action Items:**
- [ ] Rename directory: `zebra/` → `eu-gryta-kmp-zebra/`
- [ ] Update `settings.gradle.kts`:
```kotlin
include(":eu-gryta-kmp-zebra")
```
- [ ] Update root `build.gradle.kts` references (if any)
- [ ] Update documentation paths in README files
- [ ] Test build: `./gradlew :eu-gryta-kmp-zebra:build`

**Impact:** Clearer project structure, easier to identify module in multi-module workspace, prevents naming conflicts.

**Reference:** Compose-Elements uses `eu-gryta-compose-elements/` directory matching `eu.gryta:compose.elements` artifact.

---

### 6. Improve Gradle Dependency Management
**Problem:** Version catalog exists but could be more comprehensive.

**Action Items:**
- [ ] Review `gradle/libs.versions.toml` for completeness
- [ ] Add missing library versions (if any external deps added)
- [ ] Consider adding plugin versions to catalog:
```toml
[versions]
kotlin = "2.2.21"
agp = "8.13.2"
compose = "1.8.1"

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
android-library = { id = "com.android.library", version.ref = "agp" }
cocoapods = { id = "org.jetbrains.kotlin.native.cocoapods", version.ref = "kotlin" }
maven-publish = { id = "com.vanniktech.maven.publish", version = "0.31.0" }
```

- [ ] Use plugin references in root build:
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.maven.publish) apply false
}
```

**Impact:** Centralized version management, easier updates, type-safe plugin references.

---

### 7. Add Dependency Update Checks
**Problem:** No automated way to track outdated dependencies.

**Action Items:**
- [ ] Add plugin to root `build.gradle.kts`:
```kotlin
plugins {
    id("com.github.ben-manes.versions") version "0.51.0"
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
```

- [ ] Run dependency check: `./gradlew dependencyUpdates`
- [ ] Add to CI/CD as informational step

**Impact:** Stay current with security patches, know when dependencies are outdated.

---

### 8. Simplify Local Version Management
**Problem:** Custom version bump logic is complex and hard to maintain.

**Current State:**
```kotlin
val baseVersion = file("version.properties").readText().trim()
version = if (isCI) baseVersion else "$baseVersion-${bumpPatchVersion(baseVersion)}-SNAPSHOT"

fun bumpPatchVersion(version: String): String {
    val parts = version.split(".")
    val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return "${parts[0]}.${parts[1]}.${patch + 1}"
}
```

**Action Items:**
- [ ] Simplify to fixed SNAPSHOT suffix:
```kotlin
val baseVersion = file("version.properties").readText().trim()
val isCI = System.getenv("GITHUB_ACTIONS") == "true"
version = if (isCI) baseVersion else "$baseVersion-SNAPSHOT"
```

- [ ] Remove `bumpPatchVersion()` function
- [ ] Document in README: "Local builds use -SNAPSHOT suffix automatically"

**Alternative (Keep Auto-Bump):**
- [ ] Extract version logic to `buildSrc/` plugin for reusability
- [ ] Add unit tests for version parsing

**Impact:** Simpler build script, easier to understand, less maintenance burden.

**Trade-off:** Loses automatic local version increment (developers can manually bump if needed).

---

### 9. Organize Code into Feature Subdirectories (Future-Proofing)
**Problem:** Flat package structure works now but won't scale if library grows.

**Current Structure:**
```
eu.gryta.zebra/
├── BarcodeScanner.kt
├── BarcodeGenerator.kt
├── BarcodeImage.kt
├── BarcodeResult.kt
├── BarcodeFormat.kt
└── BarcodeConfig.kt
```

**Proposed Structure (Optional - Only if adding more features):**
```
eu.gryta.zebra/
├── scanner/
│   ├── BarcodeScanner.kt
│   └── ScanConfig.kt
├── generator/
│   ├── BarcodeGenerator.kt
│   └── GeneratorConfig.kt
├── core/
│   ├── BarcodeImage.kt
│   ├── BarcodeResult.kt
│   └── BarcodeFormat.kt
└── Zebra.kt (top-level facade if needed)
```

**Action Items:**
- [ ] **Do NOT implement now** - current structure is fine for 6 files
- [ ] Add TODO comment in README: "Consider subdirectories if adding >10 components"
- [ ] Revisit if adding features like:
  - Advanced image processing
  - Camera integration helpers
  - Analytics/logging modules

**Impact:** Better organization at scale, logical grouping, easier navigation.

**Reference:** Compose-Elements uses `datelist/`, `generics/`, `infinitelist/`, `theme/` subdirectories.

---

## 🟢 Low Priority (Nice-to-Have)

### 10. Add Explicit Compiler Target Configuration
**Problem:** JVM target is implicit (might cause issues with mixed-version projects).

**Action Items:**
- [ ] Add explicit compiler options to `zebra/build.gradle.kts`:
```kotlin
kotlin {
    jvmToolchain(21)

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
```

**Impact:** Explicit configuration prevents compatibility issues, documents requirements clearly.

---

### 11. Add Preview/Sample App (Optional)
**Problem:** No visual way to demo library capabilities.

**Action Items:**
- [ ] Create `sample/` module with simple Android app:
  - Camera preview with barcode scanning overlay
  - Generate barcode screen
  - Format selection UI

- [ ] Add to `settings.gradle.kts`:
```kotlin
include(":sample")
```

- [ ] Link from README: "See `sample/` for working examples"

**Impact:** Easier onboarding for new users, visual demos for README, testing playground.

**Reference:** Many KMP libraries include sample apps (Coil, Ktor, etc.).

---

### 12. Consider Maven Central Publishing (Long-Term)
**Problem:** GitHub Packages requires authentication even for public artifacts.

**Action Items:**
- [ ] Research Maven Central requirements (Sonatype OSSRH)
- [ ] Prepare for stricter validation:
  - POM completeness (already good)
  - Javadoc/KDoc JAR generation
  - GPG signing (venniktech plugin supports this)

- [ ] Update `mavenPublishing` block:
```kotlin
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
}
```

**Impact:** Wider distribution, easier for users (no GitHub auth), industry standard for OSS libraries.

**Timeline:** Consider after 5+ releases to GitHub Packages (validate demand first).

---

## 📋 Implementation Order

### Phase 1: Foundation (Week 1)
1. Add CI/CD pipeline (Item #1)
2. Optimize Gradle configuration (Item #3)
3. Migrate to venniktech publishing (Item #2)

### Phase 2: Quality (Week 2)
4. Add comprehensive testing (Item #4)
5. Add dependency update checks (Item #7)

### Phase 3: Developer Experience (Week 3)
6. Rename module directory (Item #5)
7. Simplify version management (Item #8)
8. Improve dependency management (Item #6)

### Phase 4: Polish (As Needed)
9. Explicit compiler targets (Item #10)
10. Feature subdirectories (Item #9) - Only if library grows
11. Sample app (Item #11) - If community requests demos
12. Maven Central (Item #12) - After validation of demand

---

## 📊 Success Metrics

After implementing these improvements:
- ✅ **Zero manual release steps** - Push to main triggers everything
- ✅ **Test coverage >70%** - Core logic thoroughly tested
- ✅ **Build time <2 minutes** - With configuration cache
- ✅ **Clear module naming** - Directory matches artifact name
- ✅ **Dependency transparency** - Know when updates available
- ✅ **Production-ready** - Automated workflows, tested, optimized

---

## 🔗 References

- Compose-Elements workflows: `/config/workspace/WellMate/Compose-Elements/.github/workflows/`
- venniktech plugin docs: https://github.com/vanniktech/gradle-maven-publish-plugin
- Gradle optimization: https://docs.gradle.org/current/userguide/configuration_cache.html
- KMP testing: https://kotlinlang.org/docs/multiplatform-run-tests.html

---

## Notes

- All recommendations based on structural comparison with Compose-Elements library
- Priority levels consider production readiness and developer experience
- Breaking changes are allowed (no backwards compatibility required per WellMate standards)
- Focus on automation and modern tooling over manual processes
