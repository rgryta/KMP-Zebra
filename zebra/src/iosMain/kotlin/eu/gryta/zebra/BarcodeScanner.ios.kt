package eu.gryta.zebra

// iOS stub implementation
// This compiles without CocoaPods and allows cross-platform builds
// Full Vision Framework implementation available in IMPLEMENTATION_GUIDE.md

actual class BarcodeScanner {
    actual suspend fun scan(
        image: BarcodeImage,
        formats: Set<BarcodeFormat>,
        config: ScanConfig
    ): BarcodeResult {
        return BarcodeResult.Error(
            "iOS Vision Framework scanner not yet implemented. " +
            "This stub allows cross-platform compilation. " +
            "See IMPLEMENTATION_GUIDE.md for complete working implementation."
        )
    }

    actual suspend fun scanMultiple(
        image: BarcodeImage,
        formats: Set<BarcodeFormat>,
        config: ScanConfig
    ): List<BarcodeResult> {
        return listOf(
            BarcodeResult.Error(
                "iOS Vision Framework scanner not yet implemented. " +
                "See IMPLEMENTATION_GUIDE.md for complete working implementation."
            )
        )
    }
}
