package eu.gryta.zebra.core

// Simplified stub implementation for iOS
// This compiles without CocoaPods and allows the project to build
// To enable full functionality, implement using iOS Vision framework (see IOS_ALTERNATIVES.md)

actual class BarcodeImage internal constructor(
    actual val width: Int,
    actual val height: Int,
    internal val data: ByteArray
) {
    actual fun toByteArray(): ByteArray = data

    actual companion object {
        actual fun fromByteArray(
            bytes: ByteArray,
            width: Int,
            height: Int,
            format: ImageFormat
        ): BarcodeImage {
            return BarcodeImage(width, height, bytes)
        }
    }
}
