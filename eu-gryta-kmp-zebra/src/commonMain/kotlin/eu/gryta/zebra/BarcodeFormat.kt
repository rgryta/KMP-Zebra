package eu.gryta.zebra

enum class BarcodeFormat(val displayName: String) {
    // 1D Formats
    CODE_128("Code 128"),
    CODE_39("Code 39"),
    CODE_93("Code 93"),
    CODABAR("Codabar"),
    EAN_8("EAN-8"),
    EAN_13("EAN-13"),
    UPC_A("UPC-A"),
    UPC_E("UPC-E"),
    ITF("ITF"),

    // 2D Formats
    QR_CODE("QR Code"),
    DATA_MATRIX("Data Matrix"),
    PDF_417("PDF417"),
    AZTEC("Aztec"),
    MAXICODE("MaxiCode"),

    // Postal
    RSS_14("RSS-14"),
    RSS_EXPANDED("RSS Expanded");

    companion object {
        fun all(): Set<BarcodeFormat> = entries.toSet()
        fun oneD(): Set<BarcodeFormat> = setOf(
            CODE_128, CODE_39, CODE_93, CODABAR,
            EAN_8, EAN_13, UPC_A, UPC_E, ITF,
            RSS_14, RSS_EXPANDED
        )
        fun twoD(): Set<BarcodeFormat> = setOf(
            QR_CODE, DATA_MATRIX, PDF_417, AZTEC, MAXICODE
        )
    }
}
