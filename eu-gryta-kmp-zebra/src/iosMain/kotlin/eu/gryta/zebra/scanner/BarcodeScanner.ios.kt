@file:OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package eu.gryta.zebra.scanner

import eu.gryta.zebra.core.BarcodeFormat
import eu.gryta.zebra.core.BarcodeImage
import eu.gryta.zebra.core.BarcodeResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNBarcodeSymbologyAztec
import platform.Vision.VNBarcodeSymbologyCodabar
import platform.Vision.VNBarcodeSymbologyCode128
import platform.Vision.VNBarcodeSymbologyCode39
import platform.Vision.VNBarcodeSymbologyCode93
import platform.Vision.VNBarcodeSymbologyDataMatrix
import platform.Vision.VNBarcodeSymbologyEAN13
import platform.Vision.VNBarcodeSymbologyEAN8
import platform.Vision.VNBarcodeSymbologyITF14
import platform.Vision.VNBarcodeSymbologyPDF417
import platform.Vision.VNBarcodeSymbologyQR
import platform.Vision.VNBarcodeSymbologyUPCE
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler

private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
}

// VNBarcodeSymbology is a typealias for String? in Kotlin/Native
private fun symbologyToFormat(symbology: String?): BarcodeFormat? = when (symbology) {
    VNBarcodeSymbologyEAN13 -> BarcodeFormat.EAN_13
    VNBarcodeSymbologyEAN8 -> BarcodeFormat.EAN_8
    VNBarcodeSymbologyUPCE -> BarcodeFormat.UPC_E
    VNBarcodeSymbologyCode128 -> BarcodeFormat.CODE_128
    VNBarcodeSymbologyCode39 -> BarcodeFormat.CODE_39
    VNBarcodeSymbologyCode93 -> BarcodeFormat.CODE_93
    VNBarcodeSymbologyCodabar -> BarcodeFormat.CODABAR
    VNBarcodeSymbologyITF14 -> BarcodeFormat.ITF
    VNBarcodeSymbologyQR -> BarcodeFormat.QR_CODE
    VNBarcodeSymbologyDataMatrix -> BarcodeFormat.DATA_MATRIX
    VNBarcodeSymbologyPDF417 -> BarcodeFormat.PDF_417
    VNBarcodeSymbologyAztec -> BarcodeFormat.AZTEC
    else -> null
}

private fun formatsToSymbologies(formats: Set<BarcodeFormat>): List<String> =
    formats.mapNotNull { fmt ->
        when (fmt) {
            BarcodeFormat.EAN_13 -> VNBarcodeSymbologyEAN13
            BarcodeFormat.EAN_8 -> VNBarcodeSymbologyEAN8
            BarcodeFormat.UPC_E -> VNBarcodeSymbologyUPCE
            BarcodeFormat.UPC_A -> VNBarcodeSymbologyEAN13 // Vision reports UPC-A as EAN-13
            BarcodeFormat.CODE_128 -> VNBarcodeSymbologyCode128
            BarcodeFormat.CODE_39 -> VNBarcodeSymbologyCode39
            BarcodeFormat.CODE_93 -> VNBarcodeSymbologyCode93
            BarcodeFormat.CODABAR -> VNBarcodeSymbologyCodabar
            BarcodeFormat.ITF -> VNBarcodeSymbologyITF14
            BarcodeFormat.QR_CODE -> VNBarcodeSymbologyQR
            BarcodeFormat.DATA_MATRIX -> VNBarcodeSymbologyDataMatrix
            BarcodeFormat.PDF_417 -> VNBarcodeSymbologyPDF417
            BarcodeFormat.AZTEC -> VNBarcodeSymbologyAztec
            else -> null // MAXICODE, RSS_14, RSS_EXPANDED unsupported by Vision
        }
    }.filterNotNull().distinct()

private fun observationToResult(obs: VNBarcodeObservation): BarcodeResult {
    val text = obs.payloadStringValue ?: return BarcodeResult.NotFound
    val format = symbologyToFormat(obs.symbology) ?: BarcodeFormat.QR_CODE
    return BarcodeResult.Success(text = text, format = format, raw = null, boundingBox = null)
}

actual class BarcodeScanner {
    actual suspend fun scan(
        image: BarcodeImage,
        formats: Set<BarcodeFormat>,
        config: ScanConfig
    ): BarcodeResult = withContext(Dispatchers.Default) {
        runCatching {
            detect(image, formats).firstOrNull() ?: BarcodeResult.NotFound
        }.getOrElse { e -> BarcodeResult.Error("iOS Vision scan failed: ${e.message}", e) }
    }

    actual suspend fun scanMultiple(
        image: BarcodeImage,
        formats: Set<BarcodeFormat>,
        config: ScanConfig
    ): List<BarcodeResult> = withContext(Dispatchers.Default) {
        runCatching { detect(image, formats) }
            .getOrElse { e -> listOf(BarcodeResult.Error("iOS Vision scan failed: ${e.message}", e)) }
    }

    private fun detect(image: BarcodeImage, formats: Set<BarcodeFormat>): List<BarcodeResult> {
        val data = image.toByteArray().toNSData()

        val request = VNDetectBarcodesRequest()
        val symbologies = formatsToSymbologies(formats)
        if (symbologies.isNotEmpty()) {
            request.symbologies = symbologies
        }

        val handler = VNImageRequestHandler(data = data, options = mapOf<Any?, Any?>())
        handler.performRequests(listOf(request), error = null)

        @Suppress("UNCHECKED_CAST")
        val observations = (request.results as? List<VNBarcodeObservation>).orEmpty()
        if (observations.isEmpty()) return listOf(BarcodeResult.NotFound)
        return observations.map { observationToResult(it) }
    }
}
