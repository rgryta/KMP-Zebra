package eu.gryta.zebra.core

// iOS BarcodeImage stores the source bytes verbatim. For ImageFormat.JPEG the
// bytes are an encoded JPEG that BarcodeScanner decodes via UIImage(data:).
// For raw formats (RGB/RGBA/GRAYSCALE/YUV) the bytes are the raw pixel buffer.

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
