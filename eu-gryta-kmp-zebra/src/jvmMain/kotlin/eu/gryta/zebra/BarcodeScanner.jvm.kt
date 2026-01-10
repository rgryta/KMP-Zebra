package eu.gryta.zebra

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class BarcodeScanner {
    private val reader = MultiFormatReader()

    actual suspend fun scan(
        image: BarcodeImage,
        formats: Set<BarcodeFormat>,
        config: ScanConfig
    ): BarcodeResult = withContext(Dispatchers.Default) {
        try {
            val hints = mutableMapOf<DecodeHintType, Any>().apply {
                put(DecodeHintType.POSSIBLE_FORMATS, formats.map { it.toZXingFormat() })
                if (config.tryHarder) {
                    put(DecodeHintType.TRY_HARDER, true)
                }
                if (config.pureBarcode) {
                    put(DecodeHintType.PURE_BARCODE, true)
                }
            }

            reader.setHints(hints)

            val luminanceSource = BufferedImageLuminanceSource(image.bufferedImage)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(luminanceSource))

            val result = reader.decode(binaryBitmap)

            val boundingBox = result.resultPoints?.let { points ->
                if (points.size >= 2) {
                    val minX = points.minOf { it.x }.toInt()
                    val maxX = points.maxOf { it.x }.toInt()
                    val minY = points.minOf { it.y }.toInt()
                    val maxY = points.maxOf { it.y }.toInt()
                    BoundingBox(minX, minY, maxX - minX, maxY - minY)
                } else null
            }

            BarcodeResult.Success(
                text = result.text,
                format = result.barcodeFormat.toKMP(),
                raw = result.rawBytes,
                boundingBox = boundingBox
            )
        } catch (e: NotFoundException) {
            BarcodeResult.NotFound
        } catch (e: Exception) {
            BarcodeResult.Error(e.message ?: "Scan failed", e)
        }
    }

    actual suspend fun scanMultiple(
        image: BarcodeImage,
        formats: Set<BarcodeFormat>,
        config: ScanConfig
    ): List<BarcodeResult> = withContext(Dispatchers.Default) {
        // ZXing's GenericMultipleBarcodeReader doesn't work well for all formats
        // Fallback to single scan for simplicity
        val result = scan(image, formats, config)
        listOf(result)
    }
}

private fun com.google.zxing.BarcodeFormat.toKMP(): BarcodeFormat {
    return when (this) {
        com.google.zxing.BarcodeFormat.QR_CODE -> BarcodeFormat.QR_CODE
        com.google.zxing.BarcodeFormat.PDF_417 -> BarcodeFormat.PDF_417
        com.google.zxing.BarcodeFormat.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        com.google.zxing.BarcodeFormat.CODE_128 -> BarcodeFormat.CODE_128
        com.google.zxing.BarcodeFormat.CODE_39 -> BarcodeFormat.CODE_39
        com.google.zxing.BarcodeFormat.CODE_93 -> BarcodeFormat.CODE_93
        com.google.zxing.BarcodeFormat.CODABAR -> BarcodeFormat.CODABAR
        com.google.zxing.BarcodeFormat.EAN_8 -> BarcodeFormat.EAN_8
        com.google.zxing.BarcodeFormat.EAN_13 -> BarcodeFormat.EAN_13
        com.google.zxing.BarcodeFormat.UPC_A -> BarcodeFormat.UPC_A
        com.google.zxing.BarcodeFormat.UPC_E -> BarcodeFormat.UPC_E
        com.google.zxing.BarcodeFormat.ITF -> BarcodeFormat.ITF
        com.google.zxing.BarcodeFormat.AZTEC -> BarcodeFormat.AZTEC
        com.google.zxing.BarcodeFormat.MAXICODE -> BarcodeFormat.MAXICODE
        com.google.zxing.BarcodeFormat.RSS_14 -> BarcodeFormat.RSS_14
        com.google.zxing.BarcodeFormat.RSS_EXPANDED -> BarcodeFormat.RSS_EXPANDED
        else -> throw IllegalArgumentException("Unsupported format: $this")
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
