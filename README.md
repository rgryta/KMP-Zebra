# Zebra - Kotlin Multiplatform Barcode Library

A comprehensive Kotlin Multiplatform library for QR/barcode scanning and generation, supporting Android, iOS, and Desktop (JVM) platforms.

## Features

- **All Major Barcode Formats**: QR Code, PDF417, Data Matrix, Code 128, EAN-13, UPC-A, and more
- **Cross-Platform**: Android, iOS (placeholder), and Desktop/JVM
- **Hybrid Approach**:
  - Android: Google MLKit (optimized mobile performance)
  - Desktop: ZXing (mature, proven library)
  - iOS: Placeholder (requires MLKit CocoaPods setup)
- **Detection Logic Only**: No camera UI - your app provides camera frames
- **Both Scanning and Generation**: Complete barcode solution

## Supported Formats

### 1D Barcodes
- Code 128, Code 39, Code 93
- Codabar
- EAN-8, EAN-13
- UPC-A, UPC-E
- ITF (Interleaved 2 of 5)
- RSS-14, RSS Expanded

### 2D Barcodes
- QR Code
- PDF417
- Data Matrix
- Aztec
- MaxiCode

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("eu.gryta:zebra:1.0.0")
}
```

## Usage

### Scanning Barcodes (Android with CameraX)

```kotlin
import eu.gryta.zebra.*
import androidx.camera.core.ImageAnalysis

class BarcodeAnalyzer : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanner()

    override fun analyze(imageProxy: ImageProxy) {
        val bytes = imageProxy.planes[0].buffer.toByteArray()

        val barcodeImage = BarcodeImage.fromByteArray(
            bytes = bytes,
            width = imageProxy.width,
            height = imageProxy.height,
            format = ImageFormat.YUV
        )

        lifecycleScope.launch {
            when (val result = scanner.scan(barcodeImage, ScanConfig.fast())) {
                is BarcodeResult.Success -> {
                    println("Found: ${result.text} (${result.format.displayName})")
                }
                is BarcodeResult.NotFound -> {
                    // Continue scanning
                }
                is BarcodeResult.Error -> {
                    println("Error: ${result.message}")
                }
            }
        }

        imageProxy.close()
    }
}
```

### Generating Barcodes (Desktop/JVM)

```kotlin
import eu.gryta.zebra.*
import java.io.File

fun main() {
    val generator = BarcodeGenerator()

    // Generate QR code
    val qrCode = generator.generate(
        content = "https://example.com",
        format = BarcodeFormat.QR_CODE,
        config = GeneratorConfig.highQuality()
    )
    File("qrcode.png").writeBytes(qrCode.toByteArray())

    // Generate PDF417
    val pdf417 = generator.generate(
        content = "License: ABC123",
        format = BarcodeFormat.PDF_417,
        config = GeneratorConfig(width = 400, height = 100)
    )
    File("pdf417.png").writeBytes(pdf417.toByteArray())

    // Generate EAN-13
    val ean = generator.generate(
        content = "1234567890128",  // Must be 13 digits
        format = BarcodeFormat.EAN_13
    )
    File("ean13.png").writeBytes(ean.toByteArray())
}
```

### Scanning Multiple Barcodes

```kotlin
val results = scanner.scanMultiple(
    image = barcodeImage,
    formats = BarcodeFormat.all(),
    config = ScanConfig.accurate()
)

results.forEach { result ->
    when (result) {
        is BarcodeResult.Success -> println("Found: ${result.text}")
        is BarcodeResult.NotFound -> println("No barcode found")
        is BarcodeResult.Error -> println("Error: ${result.message}")
    }
}
```

## Configuration

### Scan Configuration

```kotlin
// Fast scanning (real-time camera)
ScanConfig.fast()  // 1 second timeout, optimized for speed

// Default scanning
ScanConfig.default()  // 5 second timeout, balanced

// Accurate scanning
ScanConfig.accurate()  // 10 second timeout, try harder

// Custom configuration
ScanConfig(
    tryHarder = true,      // More thorough but slower
    pureBarcode = false,   // Allow surrounding content
    timeout = 5000         // Milliseconds
)
```

### Generation Configuration

```kotlin
// Default configuration
GeneratorConfig.default()  // 300x300, medium error correction

// High quality
GeneratorConfig.highQuality()  // 600x600, high error correction

// Custom configuration
GeneratorConfig(
    width = 400,
    height = 400,
    margin = 2,
    foregroundColor = 0xFF000000.toInt(),  // Black
    backgroundColor = 0xFFFFFFFF.toInt(),  // White
    errorCorrection = ErrorCorrectionLevel.HIGH
)
```

## Platform-Specific Notes

### Android
- Uses **Google MLKit** for scanning (hardware-accelerated, auto-zoom)
- Uses **ZXing** for generation
- Requires camera permissions in AndroidManifest.xml:
  ```xml
  <uses-permission android:name="android.permission.CAMERA" />
  ```

### Desktop (JVM)
- Uses **ZXing** for both scanning and generation
- Works with webcam or image files
- No camera UI provided - integrate with your preferred camera library

### iOS
- **⚠️ Stub Implementation (Scanner/Generator Not Yet Functional)**
- The library compiles for iOS targets but returns placeholder errors at runtime
- **No CocoaPods required** - builds cleanly without external dependencies
- **No macOS needed** - compiles in CI/CD (Linux/Windows)

**To enable iOS functionality:**

1. **Recommended: iOS Vision Framework** (zero dependencies, built into iOS 15.0+)
   - Native performance, no external dependencies
   - Replace stub in `zebra/src/iosMain/kotlin/eu/gryta/zebra/BarcodeScanner.ios.kt`
   - Use `platform.Vision.VNDetectBarcodesRequest` for scanning
   - See commit history for reference implementation

2. **Alternative: Pure Kotlin ZXing**
   - Cross-platform consistency, larger binary size
   - Move generation logic to `commonMain` for all platforms

Add camera permission to Info.plist:
```xml
<key>NSCameraUsageDescription</key>
<string>We need camera access to scan barcodes</string>
```

## Architecture

### Detection Logic Only
This library provides **barcode detection logic** without camera UI. Your app:
1. Captures camera frames (using CameraX, AVFoundation, etc.)
2. Converts frames to `BarcodeImage`
3. Passes images to `BarcodeScanner.scan()`
4. Receives `BarcodeResult`

### Hybrid Backend
- **Android & iOS**: MLKit (best mobile performance)
- **Desktop**: ZXing (only available option)
- **Generation**: ZXing on all platforms (consistent output)

## API Reference

### BarcodeScanner

```kotlin
suspend fun scan(
    image: BarcodeImage,
    formats: Set<BarcodeFormat> = BarcodeFormat.all(),
    config: ScanConfig = ScanConfig.default()
): BarcodeResult

suspend fun scanMultiple(
    image: BarcodeImage,
    formats: Set<BarcodeFormat> = BarcodeFormat.all(),
    config: ScanConfig = ScanConfig.default()
): List<BarcodeResult>
```

### BarcodeGenerator

```kotlin
fun generate(
    content: String,
    format: BarcodeFormat,
    config: GeneratorConfig = GeneratorConfig.default()
): BarcodeImage
```

### BarcodeResult

```kotlin
sealed class BarcodeResult {
    data class Success(
        val text: String,
        val format: BarcodeFormat,
        val raw: ByteArray? = null,
        val boundingBox: BoundingBox? = null
    ) : BarcodeResult()

    data object NotFound : BarcodeResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : BarcodeResult()
}
```

## Sample App

A fully functional Android sample app is included at `/sample`:

```bash
# Build and install the sample app
./gradlew :sample:assembleDebug
./gradlew :sample:installDebug
```

**Features:**
- Generate barcodes (QR Code, Code 128, EAN-13, etc.)
- Scan barcodes from gallery images
- Material 3 UI with bottom navigation
- Demonstrates all library APIs

See [sample/README.md](sample/README.md) for detailed usage instructions.

## Building from Source

```bash
# Build library
./gradlew :zebra:build

# Run tests
./gradlew :zebra:test

# Publish to Maven Local
./gradlew :zebra:publishToMavenLocal

# Build sample app
./gradlew :sample:assembleDebug
```

## Requirements

- Kotlin 2.2.21+
- Android SDK 26+ (Android 8.0)
- JVM 21+ (Desktop)
- iOS 15.0+ (placeholder implementation)

## Dependencies

- ZXing 3.5.3 (barcode processing)
- Google MLKit Barcode Scanning 17.3.0 (Android)
- Kotlinx Coroutines 1.10.2

## License

[Your License Here]

## Contributing

Contributions welcome! Especially for completing the iOS MLKit integration.

## Credits

Built with:
- [ZXing](https://github.com/zxing/zxing) - Multi-format 1D/2D barcode library
- [Google MLKit](https://developers.google.com/ml-kit) - Mobile barcode scanning
