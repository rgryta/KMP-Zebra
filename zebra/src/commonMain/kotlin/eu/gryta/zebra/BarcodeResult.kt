package eu.gryta.zebra

sealed class BarcodeResult {
    data class Success(
        val text: String,
        val format: BarcodeFormat,
        val raw: ByteArray? = null,
        val boundingBox: BoundingBox? = null
    ) : BarcodeResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Success

            if (text != other.text) return false
            if (format != other.format) return false
            if (raw != null) {
                if (other.raw == null) return false
                if (!raw.contentEquals(other.raw)) return false
            } else if (other.raw != null) return false
            if (boundingBox != other.boundingBox) return false

            return true
        }

        override fun hashCode(): Int {
            var result = text.hashCode()
            result = 31 * result + format.hashCode()
            result = 31 * result + (raw?.contentHashCode() ?: 0)
            result = 31 * result + (boundingBox?.hashCode() ?: 0)
            return result
        }
    }

    data object NotFound : BarcodeResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : BarcodeResult()
}

data class BoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)
