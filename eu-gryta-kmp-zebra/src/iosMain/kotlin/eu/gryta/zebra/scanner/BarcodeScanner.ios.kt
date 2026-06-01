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

        // Detect all Vision-supported symbologies; restricting request.symbologies to an
        // explicit list proved unreliable across Vision revisions (a requested symbology
        // could be silently excluded). We filter the results by `formats` afterward instead.
        val request = VNDetectBarcodesRequest()

        val handler = VNImageRequestHandler(data = data, options = mapOf<Any?, Any?>())
        handler.performRequests(listOf(request), error = null)

        @Suppress("UNCHECKED_CAST")
        val observations = (request.results as? List<VNBarcodeObservation>).orEmpty()
        if (observations.isEmpty()) return listOf(BarcodeResult.NotFound)

        val results = observations.map { observationToResult(it) }
        val matchAll = formats.isEmpty() || formats == BarcodeFormat.all()
        val filtered = if (matchAll) {
            results
        } else {
            results.filter { it is BarcodeResult.Success && it.format in formats }
        }
        return filtered.ifEmpty { listOf(BarcodeResult.NotFound) }
    }
}
