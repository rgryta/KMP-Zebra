package eu.gryta.zebra.sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import eu.gryta.zebra.BarcodeImage
import eu.gryta.zebra.ImageFormat
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun BarcodeImage.toBitmap(): Bitmap {
        val bytes = this.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun Bitmap.toBarcodeImage(): BarcodeImage {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()

        return BarcodeImage.fromByteArray(
            bytes = bytes,
            width = width,
            height = height,
            format = ImageFormat.RGBA
        )
    }
}
