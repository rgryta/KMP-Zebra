package eu.gryta.zebra

data class ScanConfig(
    val tryHarder: Boolean = false,
    val pureBarcode: Boolean = false,
    val timeout: Long = 5000
) {
    companion object {
        fun default() = ScanConfig()
        fun fast() = ScanConfig(tryHarder = false, timeout = 1000)
        fun accurate() = ScanConfig(tryHarder = true, timeout = 10000)
    }
}

enum class ErrorCorrectionLevel {
    LOW,
    MEDIUM,
    QUARTILE,
    HIGH
}

data class GeneratorConfig(
    val width: Int = 300,
    val height: Int = 300,
    val margin: Int = 1,
    val foregroundColor: Int = 0xFF000000.toInt(),
    val backgroundColor: Int = 0xFFFFFFFF.toInt(),
    val errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.MEDIUM
) {
    companion object {
        fun default() = GeneratorConfig()
        fun highQuality() = GeneratorConfig(
            width = 600,
            height = 600,
            errorCorrection = ErrorCorrectionLevel.HIGH
        )
    }
}
