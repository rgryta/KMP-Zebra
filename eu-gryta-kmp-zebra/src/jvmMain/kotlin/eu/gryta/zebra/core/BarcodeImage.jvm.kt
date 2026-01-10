package eu.gryta.zebra.core

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual class BarcodeImage(val bufferedImage: BufferedImage) {
    actual val width: Int get() = bufferedImage.width
    actual val height: Int get() = bufferedImage.height

    actual fun toByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "PNG", outputStream)
        return outputStream.toByteArray()
    }

    actual companion object {
        actual fun fromByteArray(
            bytes: ByteArray,
            width: Int,
            height: Int,
            format: ImageFormat
        ): BarcodeImage {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

            when (format) {
                ImageFormat.RGB -> {
                    var pixelIndex = 0
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            if (pixelIndex + 2 < bytes.size) {
                                val r = bytes[pixelIndex++].toInt() and 0xFF
                                val g = bytes[pixelIndex++].toInt() and 0xFF
                                val b = bytes[pixelIndex++].toInt() and 0xFF
                                val rgb = (r shl 16) or (g shl 8) or b
                                image.setRGB(x, y, rgb)
                            }
                        }
                    }
                }
                ImageFormat.RGBA -> {
                    var pixelIndex = 0
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            if (pixelIndex + 3 < bytes.size) {
                                val r = bytes[pixelIndex++].toInt() and 0xFF
                                val g = bytes[pixelIndex++].toInt() and 0xFF
                                val b = bytes[pixelIndex++].toInt() and 0xFF
                                pixelIndex++ // Skip alpha
                                val rgb = (r shl 16) or (g shl 8) or b
                                image.setRGB(x, y, rgb)
                            }
                        }
                    }
                }
                ImageFormat.GRAYSCALE -> {
                    var pixelIndex = 0
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            if (pixelIndex < bytes.size) {
                                val gray = bytes[pixelIndex++].toInt() and 0xFF
                                val rgb = (gray shl 16) or (gray shl 8) or gray
                                image.setRGB(x, y, rgb)
                            }
                        }
                    }
                }
                ImageFormat.YUV -> {
                    // Simple YUV to RGB conversion
                    var pixelIndex = 0
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            if (pixelIndex < bytes.size) {
                                val gray = bytes[pixelIndex++].toInt() and 0xFF
                                val rgb = (gray shl 16) or (gray shl 8) or gray
                                image.setRGB(x, y, rgb)
                            }
                        }
                    }
                }
            }

            return BarcodeImage(image)
        }
    }
}
