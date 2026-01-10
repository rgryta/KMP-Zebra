package eu.gryta.zebra.generator

import eu.gryta.zebra.core.BarcodeFormat
import eu.gryta.zebra.core.BarcodeImage

expect class BarcodeGenerator() {
    fun generate(
        content: String,
        format: BarcodeFormat,
        config: GeneratorConfig = GeneratorConfig.default()
    ): BarcodeImage
}
