# Zebra Sample App

A simple Android application demonstrating the KMP-Zebra barcode library functionality.

## Features

This sample app demonstrates both core capabilities of the KMP-Zebra library:

### 1. Barcode Generation
- Generate QR codes and other barcode formats from text input
- Support for all barcode formats: QR Code, Code 128, EAN-13, Data Matrix, PDF417, and more
- Real-time preview of generated barcodes
- Simple text input interface

### 2. Barcode Scanning
- Scan barcodes from images in your gallery
- Detect all supported barcode formats
- Display decoded content and format information
- Shows bounding box coordinates when available

## Screenshots

The app has two main screens accessible via bottom navigation:

- **Generate Tab**: Create barcodes by entering text and selecting a format
- **Scan Tab**: Pick images from your gallery and scan them for barcodes

## Building and Running

### Prerequisites
- Android Studio or Gradle
- Android SDK with minimum API level 26 (Android 8.0)
- Device or emulator for testing

### Build Commands

From the KMP-Zebra root directory:

```bash
# Build debug APK
./gradlew :sample:assembleDebug

# Install on connected device
./gradlew :sample:installDebug

# Build and install
./gradlew :sample:assembleDebug :sample:installDebug
```

The generated APK will be located at:
```
sample/build/outputs/apk/debug/sample-debug.apk
```

### From Android Studio

1. Open the KMP-Zebra project in Android Studio
2. Select the `sample` run configuration
3. Click Run or Debug

## Permissions

The app requests the following permissions:

- **CAMERA**: For future camera-based scanning (currently gallery-only)
- **READ_MEDIA_IMAGES**: For picking images from gallery (Android 13+)

Both permissions are optional - the app will work with gallery access only if camera permission is denied.

## Code Structure

```
sample/src/main/kotlin/eu/gryta/zebra/sample/
├── MainActivity.kt        # Main activity with navigation
├── GeneratorScreen.kt     # Barcode generation UI
├── ScannerScreen.kt       # Barcode scanning UI
└── ImageUtils.kt          # Conversion utilities (Bitmap ↔ BarcodeImage)
```

## Key Implementation Details

### BarcodeImage Conversion

The library uses a platform-agnostic `BarcodeImage` type. This sample app provides conversion utilities:

```kotlin
// Convert library BarcodeImage to Android Bitmap for display
fun BarcodeImage.toBitmap(): Bitmap

// Convert Android Bitmap to library BarcodeImage for scanning
fun Bitmap.toBarcodeImage(): BarcodeImage
```

### Generation Example

```kotlin
val generator = BarcodeGenerator()
val barcodeImage = generator.generate(
    content = "https://github.com",
    format = BarcodeFormat.QR_CODE,
    config = GeneratorConfig.default()
)
val bitmap = barcodeImage.toBitmap() // Display in UI
```

### Scanning Example

```kotlin
val scanner = BarcodeScanner()
val result = scanner.scan(
    image = bitmap.toBarcodeImage(),
    formats = BarcodeFormat.all(),
    config = ScanConfig.default()
)

when (result) {
    is BarcodeResult.Success -> println("Found: ${result.text}")
    is BarcodeResult.NotFound -> println("No barcode detected")
    is BarcodeResult.Error -> println("Error: ${result.message}")
}
```

## Testing the Library

### Generation Testing
1. Open the app and go to the "Generate" tab
2. Enter any text (e.g., "Hello World" or a URL)
3. Select a barcode format from the dropdown
4. Tap "Generate Barcode" to see the result

### Scanning Testing
1. First, generate a barcode and take a screenshot
2. Go to the "Scan" tab
3. Tap "Pick Image from Gallery"
4. Select the barcode image you just created
5. Tap "Scan for Barcodes" to decode it

## Limitations

- Currently uses gallery image picker (no live camera scanning)
- iOS and Desktop platforms not implemented in this sample
- Basic UI without advanced features (no save, share, history, etc.)

## Future Enhancements

Potential improvements for this sample app:

- [ ] Live camera scanning with preview
- [ ] Save generated barcodes to gallery
- [ ] Share barcode images
- [ ] Scan history
- [ ] Batch scanning (multiple barcodes in one image)
- [ ] Custom barcode size and color options
- [ ] Desktop (JVM) version of the sample app

## License

This sample app is part of the KMP-Zebra project. See the LICENSE file in the root directory.
