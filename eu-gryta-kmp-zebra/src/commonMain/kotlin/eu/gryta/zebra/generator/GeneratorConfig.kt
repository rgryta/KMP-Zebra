package eu.gryta.zebra.generator

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
