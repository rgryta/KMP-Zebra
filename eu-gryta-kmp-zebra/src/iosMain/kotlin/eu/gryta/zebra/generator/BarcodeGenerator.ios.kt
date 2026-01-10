package eu.gryta.zebra.generator
import eu.gryta.zebra.core.*

// iOS stub implementation for barcode generation
// ZXing (Java library) is not available on iOS Native
// See IMPLEMENTATION_GUIDE.md for pure Kotlin alternatives (qrcode-kotlin library)

actual class BarcodeGenerator {
    actual fun generate(
        content: String,
        format: BarcodeFormat,
        config: GeneratorConfig
    ): BarcodeImage {
        throw UnsupportedOperationException(
            "iOS barcode generation not yet implemented. " +
            "ZXing is JVM-only and not available on iOS Native. " +
            "See IMPLEMENTATION_GUIDE.md for implementation options:\n" +
            "1. Use qrcode-kotlin library for QR codes (pure Kotlin, works on all platforms)\n" +
            "2. Use iOS Core Image CIBarcodeGenerator filter (native, QR/PDF417/Code128 only)\n" +
            "3. Share generation logic in commonMain if using a KMP-compatible library"
        )
    }
}
