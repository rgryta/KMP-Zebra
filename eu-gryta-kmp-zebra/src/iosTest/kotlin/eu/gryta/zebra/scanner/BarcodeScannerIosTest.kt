package eu.gryta.zebra.scanner

import eu.gryta.zebra.core.BarcodeFormat
import eu.gryta.zebra.core.BarcodeImage
import eu.gryta.zebra.core.BarcodeResult
import eu.gryta.zebra.core.ImageFormat
import kotlinx.coroutines.runBlocking
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalEncodingApi::class)
class BarcodeScannerIosTest {

    // Real QR JPEG encoding "WELLMATE-TEST", verified to decode via OpenCV before embedding.
    private val qrJpegBase64 =
        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAMCAgICAgMCAgIDAwMDBAYEBAQEBAgGBgUGCQgKCgkICQkKDA8MCgsOCwkJDRENDg8QEBEQCgwSExIQEw8QEBD/2wBDAQMDAwQDBAgEBAgQCwkLEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBD/wAARCADoAOgDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9U6KKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigD+Veiiv6qKAP5V6K/qoooA/lXor+qiigD+Vev6qK/lXr+qigD+Veiiv6qKAP5V6K/qoooA/lXr+qiv5V6/qooA/lXoor+qigD+Veiv6qKKAP5V6K/qoooA/lXr+qiv5V6/qooAKKKKACiiigAooooAKKKKAP5V6/qor+Vev6qKAP5V6KKKACv6qK/lXr+qigD+Vev6qK/lXr+qigD+Veiv6qK/lXoAK/qoor+VegD+qiiv5V6/qooA/lXr+qiv5V6/qooA/lXooooAK/qor+Vev6qKAP5V6/qor+Vev6qKACiiigAooooAKKKKACiiigD+Vev6qK/lXr+qigD+Veiv6qKKAP5V6/qooooA/lXr+qiv5V6/qooA/lXr+qiiigD+Vev6qKK/lXoAK/qor+Vev6qKAP5V6/qor+Vev6qKAP5V6K/qoooA/lXr+qiiigD+Vev6qK/lXr+qigAooooAKKKKACiiigAooooA/lXor+qiigD+Veiv6qKKAP5V6K/qoooA/lXr+qiiigD+Vev6qKKKAP5V6/qoor+VegAr+qiv5V6/qooA/lXor+qiigD+Veiv6qKKAP5V6K/qoooA/lXr+qiiigAooooAKKKKACiiigAooooA/lXr+qiv5V6KAP6qK/lXoooAKKKKACiv6qKKAP5V6/qooooA/lXr+qiiigD+Veiiv6qKACiv5V6KACv6qK/lXooAKKK/qooA/lXr+qiiigAooooAKKKKACiiigAooooA/lXoor+qigAr+Veiv6qKAP5V6/qoor+VegAr+qiiigAr+VeiigAr+qiv5V6KAP6qKKK/lXoA/qoooooAKKK/lXoA/qor+Vev6qKKACiv5V6/qooAKKKKACiiigAooooAKKKKACv5V6/qoooAKKKKACiiigD+Veiv6qKKACv5V6KKACv6qK/lXooA/qor+Veiv6qKAP5V6K/qor+VegAor+qiv5V6ACiv6qKKACiv5V6/qooAKKKKACiiigAooooAKKKKAP5V6KK/qooA/lXor+qiigD+Veiv6qKKAP5V6/qor+Vev6qKAP5V6/qor+VeigAr+qiiv5V6ACiiv6qKAP5V6/qoor+VegAr+qiiv5V6ACv6qKKKAP5V6/qooooAKKKKACiiigAooooAKKKKAP5V6/qor+Vev6qKAP5V6KKKACv6qK/lXr+qigD+Vev6qK/lXr+qigD+VeiiigAor+qiv5V6ACv6qK/lXr+qigD+Vev6qKK/lXoAK/qoor+VegD+qiv5V6/qoooA/lXr+qiiigAooooAKKKKACiiigAooooA/lXr+qiv5V6/qooA/lXor+qiigD+Vev6qKKKAP5V6/qor+Vev6qKACv5V6/qoooAKK/lXr+qigAr+Vev6qK/lXoAKKKKACiv6qKKACv5V6KKACv6qKKKACiiigAooooAKKKKACiiigD+Veiv6qKKAP5V6K/qoooA/lXor+qiigD+Vev6qKKKAP5V6/qooooAK/lXr+qiigAr+Vev6qKKACv5V6/qoooAKKKKACv5V6/qoooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooA//Z"

    // Solid-white 64x64 JPEG: no barcode present.
    private val noiseJpegBase64 =
        "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAsKCwsNDhIQDQ4RDgsLEBYQERMUFRUVDA8XGBYUGBIUFRT/2wBDAQMEBAUEBQkFBQkUDQsNFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBQUFBT/wAARCABAAEADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9U6KKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAP/2Q=="

    @Test
    fun scan_decodes_known_qr() = runBlocking {
        val bytes = Base64.decode(qrJpegBase64)
        val image = BarcodeImage.fromByteArray(bytes, width = 0, height = 0, format = ImageFormat.JPEG)

        val result = BarcodeScanner().scan(image, BarcodeFormat.all(), ScanConfig.default())
        println("DIAG scan_decodes_known_qr result = $result")

        assertTrue(result is BarcodeResult.Success, "expected Success, got $result")
        assertEquals("WELLMATE-TEST", result.text)
        assertEquals(BarcodeFormat.QR_CODE, result.format)
    }

    @Test
    fun scan_returns_NotFound_for_noise() = runBlocking {
        val bytes = Base64.decode(noiseJpegBase64)
        val image = BarcodeImage.fromByteArray(bytes, width = 0, height = 0, format = ImageFormat.JPEG)

        val result = BarcodeScanner().scan(image, BarcodeFormat.all(), ScanConfig.default())

        assertEquals(BarcodeResult.NotFound, result)
    }
}
