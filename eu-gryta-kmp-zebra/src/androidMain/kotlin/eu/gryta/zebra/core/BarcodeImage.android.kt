package eu.gryta.zebra.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual class BarcodeImage(val bitmap: Bitmap) {
    actual val width: Int get() = bitmap.width
    actual val height: Int get() = bitmap.height

    actual fun toByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    actual companion object {
        actual fun fromByteArray(
            bytes: ByteArray,
            width: Int,
            height: Int,
            format: ImageFormat
        ): BarcodeImage {
            val bitmap = when (format) {
                ImageFormat.RGB -> {
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    var pixelIndex = 0
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            if (pixelIndex + 2 < bytes.size) {
                                val r = bytes[pixelIndex++].toInt() and 0xFF
                                val g = bytes[pixelIndex++].toInt() and 0xFF
                                val b = bytes[pixelIndex++].toInt() and 0xFF
                                val color = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                                bmp.setPixel(x, y, color)
                            }
                        }
                    }
                    bmp
                }
                ImageFormat.RGBA -> {
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    var pixelIndex = 0
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            if (pixelIndex + 3 < bytes.size) {
                                val r = bytes[pixelIndex++].toInt() and 0xFF
                                val g = bytes[pixelIndex++].toInt() and 0xFF
                                val b = bytes[pixelIndex++].toInt() and 0xFF
                                val a = bytes[pixelIndex++].toInt() and 0xFF
                                val color = (a shl 24) or (r shl 16) or (g shl 8) or b
                                bmp.setPixel(x, y, color)
                            }
                        }
                    }
                    bmp
                }
                ImageFormat.GRAYSCALE -> {
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    var pixelIndex = 0
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            if (pixelIndex < bytes.size) {
                                val gray = bytes[pixelIndex++].toInt() and 0xFF
                                val color = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
                                bmp.setPixel(x, y, color)
                            }
                        }
                    }
                    bmp
                }
                ImageFormat.YUV -> {
                    // YUV NV21 format (common from camera)
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    // Just use Y channel (luminance) for simplicity
                    var pixelIndex = 0
                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            if (pixelIndex < bytes.size) {
                                val gray = bytes[pixelIndex++].toInt() and 0xFF
                                val color = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
                                bmp.setPixel(x, y, color)
                            }
                        }
                    }
                    bmp
                }
            }

            return BarcodeImage(bitmap)
        }
    }
}
