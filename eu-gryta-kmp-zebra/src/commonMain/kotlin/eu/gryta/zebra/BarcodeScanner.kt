package eu.gryta.zebra

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
