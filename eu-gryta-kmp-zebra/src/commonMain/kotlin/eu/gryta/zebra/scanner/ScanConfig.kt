package eu.gryta.zebra.scanner

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
