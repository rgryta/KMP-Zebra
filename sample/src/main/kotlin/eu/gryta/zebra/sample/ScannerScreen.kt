package eu.gryta.zebra.sample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.gryta.zebra.core.BarcodeFormat
import eu.gryta.zebra.core.BarcodeResult
import eu.gryta.zebra.scanner.BarcodeScanner
import eu.gryta.zebra.scanner.ScanConfig
import eu.gryta.zebra.sample.ImageUtils.toBarcodeImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

@Composable
fun ScannerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scanner = remember { BarcodeScanner() }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scanResult by remember { mutableStateOf<BarcodeResult?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedBitmap = loadBitmapFromUri(context, it)
            scanResult = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Barcode Scanner",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pick Image from Gallery")
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedBitmap?.let { bitmap ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selected Image",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        isScanning = true
                        scanResult = null

                        try {
                            val barcodeImage = withContext(Dispatchers.Default) {
                                bitmap.toBarcodeImage()
                            }

                            val result = withContext(Dispatchers.Default) {
                                scanner.scan(
                                    image = barcodeImage,
                                    formats = BarcodeFormat.all(),
                                    config = ScanConfig.default()
                                )
                            }

                            scanResult = result
                        } catch (e: Exception) {
                            scanResult = BarcodeResult.Error("Scan failed: ${e.message}")
                        } finally {
                            isScanning = false
                        }
                    }
                },
                enabled = !isScanning,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Scan for Barcodes")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        scanResult?.let { result ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (result) {
                        is BarcodeResult.Success -> MaterialTheme.colorScheme.primaryContainer
                        is BarcodeResult.NotFound -> MaterialTheme.colorScheme.tertiaryContainer
                        is BarcodeResult.Error -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (result) {
                        is BarcodeResult.Success -> {
                            Text(
                                text = "Barcode Found!",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Format: ${result.format.displayName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Content: ${result.text}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            result.boundingBox?.let { box ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Position: (${box.x}, ${box.y}) ${box.width}x${box.height}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        is BarcodeResult.NotFound -> {
                            Text(
                                text = "No Barcode Found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Try a different image or ensure it contains a barcode",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        is BarcodeResult.Error -> {
                            Text(
                                text = "Scan Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = result.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    }
}
