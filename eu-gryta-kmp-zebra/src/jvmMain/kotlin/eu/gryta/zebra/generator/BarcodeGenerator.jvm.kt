package eu.gryta.zebra.generator

import com.google.zxing.BarcodeFormat as ZXingFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.Writer
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel as ZXingErrorCorrection
import com.google.zxing.pdf417.PDF417Writer
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.*
import eu.gryta.zebra.core.BarcodeFormat
import eu.gryta.zebra.core.BarcodeImage
import java.awt.image.BufferedImage

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
                ErrorCorrectionLevel.LOW -> ZXingErrorCorrection.L
                ErrorCorrectionLevel.MEDIUM -> ZXingErrorCorrection.M
                ErrorCorrectionLevel.QUARTILE -> ZXingErrorCorrection.Q
                ErrorCorrectionLevel.HIGH -> ZXingErrorCorrection.H
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
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = if (matrix[x, y]) config.foregroundColor else config.backgroundColor
                image.setRGB(x, y, color)
            }
        }

        return BarcodeImage(image)
    }
}

private fun BarcodeFormat.toZXingFormat(): ZXingFormat {
    return when (this) {
        BarcodeFormat.QR_CODE -> ZXingFormat.QR_CODE
        BarcodeFormat.PDF_417 -> ZXingFormat.PDF_417
        BarcodeFormat.DATA_MATRIX -> ZXingFormat.DATA_MATRIX
        BarcodeFormat.CODE_128 -> ZXingFormat.CODE_128
        BarcodeFormat.CODE_39 -> ZXingFormat.CODE_39
        BarcodeFormat.CODE_93 -> ZXingFormat.CODE_93
        BarcodeFormat.CODABAR -> ZXingFormat.CODABAR
        BarcodeFormat.EAN_8 -> ZXingFormat.EAN_8
        BarcodeFormat.EAN_13 -> ZXingFormat.EAN_13
        BarcodeFormat.UPC_A -> ZXingFormat.UPC_A
        BarcodeFormat.UPC_E -> ZXingFormat.UPC_E
        BarcodeFormat.ITF -> ZXingFormat.ITF
        BarcodeFormat.AZTEC -> ZXingFormat.AZTEC
        BarcodeFormat.MAXICODE -> ZXingFormat.MAXICODE
        BarcodeFormat.RSS_14 -> ZXingFormat.RSS_14
        BarcodeFormat.RSS_EXPANDED -> ZXingFormat.RSS_EXPANDED
    }
}
