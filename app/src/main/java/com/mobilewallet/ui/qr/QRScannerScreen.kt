package com.mobilewallet.ui.qr

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.regex.Pattern

/**
 * Data class representing the scanner state
 */
data class QRScannerState(
    val isScanning: Boolean = false,
    val hasPermission: Boolean = false,
    val isFlashOn: Boolean = false,
    val scannedData: String? = null,
    val errorMessage: String? = null,
    val isValidAddress: Boolean = false
)

/**
 * Sealed class for scanner actions
 */
sealed class QRScannerAction {
    object OnBackClick : QRScannerAction()
    object OnToggleFlash : QRScannerAction()
    object OnRetry : QRScannerAction()
    object OnDismissError : QRScannerAction()
    data class OnAddressScanned(val address: String) : QRScannerAction()
}

/**
 * QR Scanner screen using CameraX for reading cryptocurrency addresses
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    state: QRScannerState,
    onAction: (QRScannerAction) -> Unit,
    onAddressConfirmed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            onAction(QRScannerAction.OnBackClick)
        }
    }
    
    // Check and request camera permission
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                // Permission already granted
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Scan QR Code",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { onAction(QRScannerAction.OnBackClick) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                if (state.hasPermission) {
                    IconButton(onClick = { onAction(QRScannerAction.OnToggleFlash) }) {
                        Icon(
                            imageVector = if (state.isFlashOn) Icons.Default.FlashOff else Icons.Default.FlashOn,
                            contentDescription = if (state.isFlashOn) "Turn off flash" else "Turn on flash"
                        )
                    }
                }
            }
        )
        
        if (state.hasPermission) {
            // Camera preview with scanning overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                CameraPreview(
                    onBarcodeScanned = { barcode ->
                        onAction(QRScannerAction.OnAddressScanned(barcode))
                    },
                    isFlashOn = state.isFlashOn,
                    lifecycleOwner = lifecycleOwner,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Scanning overlay
                ScanningOverlay(
                    modifier = Modifier.fillMaxSize()
                )
                
                // Instructions
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = "Position the QR code within the frame to scan",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Permission denied state
            PermissionDeniedContent(
                onRetry = { onAction(QRScannerAction.OnRetry) },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Show scanned result
        state.scannedData?.let { address ->
            ScannedResultCard(
                address = address,
                isValid = state.isValidAddress,
                onConfirm = { onAddressConfirmed(address) },
                onRetry = { onAction(QRScannerAction.OnRetry) },
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Error handling
        state.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    TextButton(onClick = { onAction(QRScannerAction.OnDismissError) }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

/**
 * Camera preview composable using CameraX
 */
@Composable
private fun CameraPreview(
    onBarcodeScanned: (String) -> Unit,
    isFlashOn: Boolean,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = Executors.newSingleThreadExecutor()
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, BarcodeAnalyzer(onBarcodeScanned))
                    }
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    // Handle camera binding errors
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = modifier
    )
    
    // Handle flashlight toggle
    LaunchedEffect(isFlashOn) {
        camera?.cameraControl?.enableTorch(isFlashOn)
    }
}

/**
 * Scanning overlay with targeting frame
 */
@Composable
private fun ScanningOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(64.dp)
        ) {
            // Scanning frame
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // Corner indicators
                ScannerCorners(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Scanning animation could be added here
    }
}

/**
 * Scanner corner indicators
 */
@Composable
private fun ScannerCorners(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val cornerSize = 32.dp
        val strokeWidth = 4.dp
        val cornerColor = MaterialTheme.colorScheme.primary
        
        // Top-left corner
        Canvas(
            modifier = Modifier
                .size(cornerSize)
                .align(Alignment.TopStart)
        ) {
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, strokeWidth.toPx())
                    lineTo(0f, 0f)
                    lineTo(strokeWidth.toPx(), 0f)
                    moveTo(0f, cornerSize.toPx() / 2)
                    lineTo(0f, cornerSize.toPx())
                    lineTo(cornerSize.toPx() / 2, cornerSize.toPx())
                },
                color = cornerColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx())
            )
        }
        
        // Add other corners similarly...
    }
}

/**
 * Permission denied content
 */
@Composable
private fun PermissionDeniedContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This app needs camera access to scan QR codes. Please enable camera permission in settings.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("Grant Permission")
        }
    }
}

/**
 * Scanned result card
 */
@Composable
private fun ScannedResultCard(
    address: String,
    isValid: Boolean,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isValid) "Valid Address Found" else "Invalid Address",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isValid) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isValid) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Scan Again")
                }
                
                if (isValid) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Use Address")
                    }
                }
            }
        }
    }
}

/**
 * Barcode analyzer using ML Kit
 */
private class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {
    
    private val scanner = BarcodeScanning.getClient()
    private var lastScanTime = 0L
    private val scanCooldown = 2000L // 2 seconds between scans
    
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime < scanCooldown) {
            imageProxy.close()
            return
        }
        
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            if (isValidCryptocurrencyAddress(value)) {
                                lastScanTime = currentTime
                                onBarcodeScanned(value)
                                return@addOnSuccessListener
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
    
    /**
     * Validate if the scanned text is a valid cryptocurrency address
     */
    private fun isValidCryptocurrencyAddress(address: String): Boolean {
        return when {
            // Bitcoin address patterns
            Pattern.matches("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$", address) -> true
            Pattern.matches("^bc1[a-z0-9]{39,59}$", address) -> true
            
            // Litecoin address patterns
            Pattern.matches("^[LM3][a-km-zA-HJ-NP-Z1-9]{26,33}$", address) -> true
            Pattern.matches("^ltc1[a-z0-9]{39,59}$", address) -> true
            
            // Dogecoin address patterns
            Pattern.matches("^D{1}[5-9A-HJ-NP-U]{1}[1-9A-HJ-NP-Za-km-z]{32}$", address) -> true
            
            // Ethereum address patterns
            Pattern.matches("^0x[a-fA-F0-9]{40}$", address) -> true
            
            else -> false
        }
    }
}