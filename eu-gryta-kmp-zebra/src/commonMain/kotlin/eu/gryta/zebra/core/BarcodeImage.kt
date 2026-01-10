package eu.gryta.zebra.core

enum class ImageFormat {
    RGB,
    RGBA,
    GRAYSCALE,
    YUV
}

expect class BarcodeImage {
    val width: Int
    val height: Int

    fun toByteArray(): ByteArray

    companion object {
        fun fromByteArray(
            bytes: ByteArray,
            width: Int,
            height: Int,
            format: ImageFormat = ImageFormat.RGB
        ): BarcodeImage
    }
}
