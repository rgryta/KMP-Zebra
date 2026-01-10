package eu.gryta.zebra

import android.graphics.Bitmap
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.pdf417.PDF417Writer
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.*

actual class BarcodeGenerator {
    actual fun generate(
        content: String,
        format: BarcodeFormat,
        config: GeneratorConfig
    ): BarcodeImage {
        val writer = when (format) {
            BarcodeFormat.QR_CODE -> QRCodeWriter()
            BarcodeFormat.PDF_417 -> PDF417Writer()
            BarcodeFormat.DATA_MATRIX -> DataMatrixWriter()
            BarcodeFormat.CODE_128 -> Code128Writer()
            BarcodeFormat.CODE_39 -> Code39Writer()
            BarcodeFormat.CODE_93 -> Code93Writer()
            BarcodeFormat.CODABAR -> CodaBarWriter()
            BarcodeFormat.EAN_8 -> EAN8Writer()
            BarcodeFormat.EAN_13 -> EAN13Writer()
            BarcodeFormat.UPC_A -> UPCAWriter()
            BarcodeFormat.UPC_E -> UPCEWriter()
            BarcodeFormat.ITF -> ITFWriter()
            else -> throw IllegalArgumentException("Unsupported format: $format")
        }

        val hints = mutableMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.MARGIN, config.margin)
            put(EncodeHintType.ERROR_CORRECTION, when (config.errorCorrection) {
                ErrorCorrectionLevel.LOW -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L
                ErrorCorrectionLevel.MEDIUM -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M
                ErrorCorrectionLevel.QUARTILE -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q
                ErrorCorrectionLevel.HIGH -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H
            })
        }

        val bitMatrix = writer.encode(
            content,
            format.toZXingFormat(),
            config.width,
            config.height,
            hints
        )

        return renderBitMatrix(bitMatrix, config)
    }

    private fun renderBitMatrix(matrix: BitMatrix, config: GeneratorConfig): BarcodeImage {
        val width = matrix.width
        val height = matrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = if (matrix[x, y]) config.foregroundColor else config.backgroundColor
                bitmap.setPixel(x, y, color)
            }
        }

        return BarcodeImage(bitmap)
    }
}

private fun BarcodeFormat.toZXingFormat(): com.google.zxing.BarcodeFormat {
    return when (this) {
        BarcodeFormat.QR_CODE -> com.google.zxing.BarcodeFormat.QR_CODE
        BarcodeFormat.PDF_417 -> com.google.zxing.BarcodeFormat.PDF_417
        BarcodeFormat.DATA_MATRIX -> com.google.zxing.BarcodeFormat.DATA_MATRIX
        BarcodeFormat.CODE_128 -> com.google.zxing.BarcodeFormat.CODE_128
        BarcodeFormat.CODE_39 -> com.google.zxing.BarcodeFormat.CODE_39
        BarcodeFormat.CODE_93 -> com.google.zxing.BarcodeFormat.CODE_93
        BarcodeFormat.CODABAR -> com.google.zxing.BarcodeFormat.CODABAR
        BarcodeFormat.EAN_8 -> com.google.zxing.BarcodeFormat.EAN_8
        BarcodeFormat.EAN_13 -> com.google.zxing.BarcodeFormat.EAN_13
        BarcodeFormat.UPC_A -> com.google.zxing.BarcodeFormat.UPC_A
        BarcodeFormat.UPC_E -> com.google.zxing.BarcodeFormat.UPC_E
        BarcodeFormat.ITF -> com.google.zxing.BarcodeFormat.ITF
        BarcodeFormat.AZTEC -> com.google.zxing.BarcodeFormat.AZTEC
        BarcodeFormat.MAXICODE -> com.google.zxing.BarcodeFormat.MAXICODE
        BarcodeFormat.RSS_14 -> com.google.zxing.BarcodeFormat.RSS_14
        BarcodeFormat.RSS_EXPANDED -> com.google.zxing.BarcodeFormat.RSS_EXPANDED
    }
}
