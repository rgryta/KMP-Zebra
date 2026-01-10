package eu.gryta.zebra

expect class BarcodeGenerator() {
    fun generate(
        content: String,
        format: BarcodeFormat,
        config: GeneratorConfig = GeneratorConfig.default()
    ): BarcodeImage
}
