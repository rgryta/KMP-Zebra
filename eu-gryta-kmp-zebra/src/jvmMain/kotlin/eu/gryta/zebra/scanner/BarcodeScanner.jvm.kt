package eu.gryta.zebra.scanner

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.ResultPoint
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import eu.gryta.zebra.core.BarcodeFormat
import eu.gryta.zebra.core.BarcodeImage
import eu.gryta.zebra.core.BarcodeResult
import eu.gryta.zebra.core.BoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.zxing.BarcodeFormat as ZXingFormat

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

private fun ZXingFormat.toKMP(): BarcodeFormat {
    return when (this) {
        ZXingFormat.QR_CODE -> BarcodeFormat.QR_CODE
        ZXingFormat.PDF_417 -> BarcodeFormat.PDF_417
        ZXingFormat.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        ZXingFormat.CODE_128 -> BarcodeFormat.CODE_128
        ZXingFormat.CODE_39 -> BarcodeFormat.CODE_39
        ZXingFormat.CODE_93 -> BarcodeFormat.CODE_93
        ZXingFormat.CODABAR -> BarcodeFormat.CODABAR
        ZXingFormat.EAN_8 -> BarcodeFormat.EAN_8
        ZXingFormat.EAN_13 -> BarcodeFormat.EAN_13
        ZXingFormat.UPC_A -> BarcodeFormat.UPC_A
        ZXingFormat.UPC_E -> BarcodeFormat.UPC_E
        ZXingFormat.ITF -> BarcodeFormat.ITF
        ZXingFormat.AZTEC -> BarcodeFormat.AZTEC
        ZXingFormat.MAXICODE -> BarcodeFormat.MAXICODE
        ZXingFormat.RSS_14 -> BarcodeFormat.RSS_14
        ZXingFormat.RSS_EXPANDED -> BarcodeFormat.RSS_EXPANDED
        else -> throw IllegalArgumentException("Unsupported format: $this")
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
