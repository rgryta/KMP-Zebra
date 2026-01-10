package eu.gryta.zebra

import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class BarcodeScanner {
    actual suspend fun scan(
        image: BarcodeImage,
        formats: Set<BarcodeFormat>,
        config: ScanConfig
    ): BarcodeResult = suspendCancellableCoroutine { continuation ->
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                formats.first().toMLKitFormat(),
                *formats.drop(1).map { it.toMLKitFormat() }.toIntArray()
            )
            .build()

        val scanner = BarcodeScanning.getClient(options)
        val inputImage = InputImage.fromBitmap(image.bitmap, 0)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val result = if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    val boundingBox = barcode.boundingBox?.let {
                        BoundingBox(it.left, it.top, it.width(), it.height())
                    }

                    BarcodeResult.Success(
                        text = barcode.rawValue ?: "",
                        format = barcode.format.toKMP(),
                        raw = barcode.rawBytes,
                        boundingBox = boundingBox
                    )
                } else {
                    BarcodeResult.NotFound
                }
                continuation.resume(result)
            }
            .addOnFailureListener { e ->
                continuation.resume(BarcodeResult.Error(e.message ?: "Scan failed", e))
            }

        continuation.invokeOnCancellation {
            scanner.close()
        }
    }

    actual suspend fun scanMultiple(
        image: BarcodeImage,
        formats: Set<BarcodeFormat>,
        config: ScanConfig
    ): List<BarcodeResult> = suspendCancellableCoroutine { continuation ->
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                formats.first().toMLKitFormat(),
                *formats.drop(1).map { it.toMLKitFormat() }.toIntArray()
            )
            .build()

        val scanner = BarcodeScanning.getClient(options)
        val inputImage = InputImage.fromBitmap(image.bitmap, 0)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val results = if (barcodes.isNotEmpty()) {
                    barcodes.map { barcode ->
                        val boundingBox = barcode.boundingBox?.let {
                            BoundingBox(it.left, it.top, it.width(), it.height())
                        }

                        BarcodeResult.Success(
                            text = barcode.rawValue ?: "",
                            format = barcode.format.toKMP(),
                            raw = barcode.rawBytes,
                            boundingBox = boundingBox
                        )
                    }
                } else {
                    listOf(BarcodeResult.NotFound)
                }
                continuation.resume(results)
            }
            .addOnFailureListener { e ->
                continuation.resume(listOf(BarcodeResult.Error(e.message ?: "Scan failed", e)))
            }

        continuation.invokeOnCancellation {
            scanner.close()
        }
    }
}

private fun BarcodeFormat.toMLKitFormat(): Int {
    return when (this) {
        BarcodeFormat.QR_CODE -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
        BarcodeFormat.PDF_417 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417
        BarcodeFormat.DATA_MATRIX -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX
        BarcodeFormat.CODE_128 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128
        BarcodeFormat.CODE_39 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39
        BarcodeFormat.CODE_93 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93
        BarcodeFormat.CODABAR -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR
        BarcodeFormat.EAN_8 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8
        BarcodeFormat.EAN_13 -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13
        BarcodeFormat.UPC_A -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A
        BarcodeFormat.UPC_E -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E
        BarcodeFormat.ITF -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF
        BarcodeFormat.AZTEC -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC
        else -> com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UNKNOWN
    }
}

private fun Int.toKMP(): BarcodeFormat {
    return when (this) {
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF -> BarcodeFormat.ITF
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
        else -> BarcodeFormat.QR_CODE
    }
}
