package eu.gryta.zebra.scanner

import eu.gryta.zebra.core.BarcodeFormat
import eu.gryta.zebra.core.BarcodeImage
import eu.gryta.zebra.core.BarcodeResult

expect class BarcodeScanner() {
    suspend fun scan(
        image: BarcodeImage,
        formats: Set<BarcodeFormat> = BarcodeFormat.all(),
        config: ScanConfig = ScanConfig.default()
    ): BarcodeResult

    suspend fun scanMultiple(
        image: BarcodeImage,
        formats: Set<BarcodeFormat> = BarcodeFormat.all(),
        config: ScanConfig = ScanConfig.default()
    ): List<BarcodeResult>
}
