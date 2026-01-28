package eu.gryta.zebra.sample

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import eu.gryta.zebra.core.BarcodeFormat
import eu.gryta.zebra.core.BarcodeImage
import eu.gryta.zebra.core.BarcodeResult
import eu.gryta.zebra.core.ImageFormat
import eu.gryta.zebra.scanner.BarcodeScanner
import eu.gryta.zebra.scanner.ScanConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

data class DetectedBarcode(
    val text: String,
    val format: BarcodeFormat,
    val boundingBox: eu.gryta.zebra.core.BoundingBox?
)

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var detectedBarcode by remember { mutableStateOf<DetectedBarcode?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var imageWidth by remember { mutableIntStateOf(0) }
    var imageHeight by remember { mutableIntStateOf(0) }

    val scanner = remember { BarcodeScanner() }
    val analysisScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.await()

        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(Dispatchers.Default.asExecutor()) { imageProxy ->
                    analysisScope.launch {
                        processImage(imageProxy, scanner) { barcode, width, height ->
                            detectedBarcode = barcode
                            imageWidth = width
                            imageHeight = height
                        }
                    }
                }
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalyzer
        )

        previewView?.let { preview.setSurfaceProvider(it.surfaceProvider) }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView = it }
                },
                modifier = Modifier.fillMaxSize()
            )

            detectedBarcode?.boundingBox?.let { box ->
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (imageWidth > 0 && imageHeight > 0) {
                        val scaleX = size.width / imageHeight
                        val scaleY = size.height / imageWidth

                        val left = box.y * scaleX
                        val top = box.x * scaleY
                        val width = box.height * scaleX
                        val height = box.width * scaleY

                        drawRect(
                            color = Color.Green,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            style = Stroke(width = 8f)
                        )
                    }
                }
            }
        }

        detectedBarcode?.let { barcode ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Barcode Detected!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Format: ${barcode.format.displayName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Content: ${barcode.text}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } ?: run {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Point camera at a barcode",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private suspend fun processImage(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onResult: (DetectedBarcode?, Int, Int) -> Unit
) {
    try {
        val barcodeImage = imageProxy.toBarcodeImage()
        val result = scanner.scan(
            image = barcodeImage,
            formats = BarcodeFormat.all(),
            config = ScanConfig.default()
        )

        val detectedBarcode = when (result) {
            is BarcodeResult.Success -> DetectedBarcode(
                text = result.text,
                format = result.format,
                boundingBox = result.boundingBox
            )
            else -> null
        }

        onResult(detectedBarcode, imageProxy.width, imageProxy.height)
    } catch (e: Exception) {
        onResult(null, imageProxy.width, imageProxy.height)
    } finally {
        imageProxy.close()
    }
}

private fun ImageProxy.toBarcodeImage(): BarcodeImage {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    return BarcodeImage.fromByteArray(
        bytes = nv21ToRgba(nv21, width, height),
        width = width,
        height = height,
        format = ImageFormat.RGBA
    )
}

private fun nv21ToRgba(nv21: ByteArray, width: Int, height: Int): ByteArray {
    val rgba = ByteArray(width * height * 4)
    val frameSize = width * height

    for (j in 0 until height) {
        for (i in 0 until width) {
            val yIndex = j * width + i
            val y = nv21[yIndex].toInt() and 0xFF
            val uvIndex = frameSize + (j shr 1) * width + (i and 1.inv())
            val v = (nv21[uvIndex].toInt() and 0xFF) - 128
            val u = (nv21[uvIndex + 1].toInt() and 0xFF) - 128

            var r = y + (1.370705f * v).toInt()
            var g = y - (0.337633f * u).toInt() - (0.698001f * v).toInt()
            var b = y + (1.732446f * u).toInt()

            r = r.coerceIn(0, 255)
            g = g.coerceIn(0, 255)
            b = b.coerceIn(0, 255)

            val rgbaIndex = yIndex * 4
            rgba[rgbaIndex] = r.toByte()
            rgba[rgbaIndex + 1] = g.toByte()
            rgba[rgbaIndex + 2] = b.toByte()
            rgba[rgbaIndex + 3] = 255.toByte()
        }
    }

    return rgba
}
