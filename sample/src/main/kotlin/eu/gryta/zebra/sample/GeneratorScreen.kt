package eu.gryta.zebra.sample

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import eu.gryta.zebra.core.BarcodeFormat
import eu.gryta.zebra.generator.BarcodeGenerator
import eu.gryta.zebra.generator.GeneratorConfig
import eu.gryta.zebra.sample.ImageUtils.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen() {
    var inputText by remember { mutableStateOf("https://github.com") }
    var selectedFormat by remember { mutableStateOf(BarcodeFormat.QR_CODE) }
    var expanded by remember { mutableStateOf(false) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val generator = remember { BarcodeGenerator() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Barcode Generator",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Text to encode") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedFormat.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Format") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                BarcodeFormat.entries.forEach { format ->
                    DropdownMenuItem(
                        text = { Text(format.displayName) },
                        onClick = {
                            selectedFormat = format
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (inputText.isNotBlank()) {
                    scope.launch {
                        isGenerating = true
                        errorMessage = null
                        generatedBitmap = null

                        try {
                            val barcodeImage = withContext(Dispatchers.Default) {
                                generator.generate(
                                    content = inputText,
                                    format = selectedFormat,
                                    config = GeneratorConfig.default()
                                )
                            }
                            generatedBitmap = barcodeImage.toBitmap()
                        } catch (e: Exception) {
                            errorMessage = "Generation failed: ${e.message}"
                        } finally {
                            isGenerating = false
                        }
                    }
                }
            },
            enabled = inputText.isNotBlank() && !isGenerating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Generate Barcode")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        generatedBitmap?.let { bitmap ->
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
                        text = "Generated ${selectedFormat.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated barcode",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    )
                    Text(
                        text = "${bitmap.width} x ${bitmap.height}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
