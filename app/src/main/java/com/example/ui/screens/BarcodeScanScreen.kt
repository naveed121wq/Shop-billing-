package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.entity.ProductEntity
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.BarcodeHelper
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit,
    onProductFound: ((ProductEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission needed to scan barcodes", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var matchedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var notFoundBarcode by remember { mutableStateOf<String?>(null) }
    var manualInput by remember { mutableStateOf("") }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    fun processBarcode(barcode: String) {
        coroutineScope.launch {
            scannedBarcode = barcode
            val product = viewModel.repository.getProductByBarcode(barcode)
            if (product != null) {
                matchedProduct = product
            } else {
                notFoundBarcode = barcode
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode Scanner (بارکوڈ اسکین)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControl?.enableTorch(isFlashOn)
                        }
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flashlight"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val cameraExecutor = Executors.newSingleThreadExecutor()

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val analyzer = BarcodeHelper.BarcodeScannerAnalyzer { barcode ->
                                if (scannedBarcode == null) {
                                    processBarcode(barcode)
                                }
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor, analyzer)
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                                cameraControl = camera.cameraControl
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Reticle & Viewfinder overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(280.dp, 160.dp)
                            .border(2.dp, PrimaryGreen, RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission is required to scan barcodes")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Bottom Manual Barcode Input Card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter barcode manually...") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (manualInput.isNotBlank()) {
                                processBarcode(manualInput.trim())
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Search")
                    }
                }
            }

            // Matched Product Result Dialog
            matchedProduct?.let { product ->
                AlertDialog(
                    onDismissRequest = {
                        matchedProduct = null
                        scannedBarcode = null
                    },
                    title = { Text("Product Found!", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(text = product.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Price: Rs ${product.salePrice.toInt()} | Stock: ${product.currentStock.toInt()} ${product.unit}")
                            Text(text = "Barcode: ${product.barcode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.addToCart(product, 1.0)
                                Toast.makeText(context, "Added ${product.name} to cart!", Toast.LENGTH_SHORT).show()
                                matchedProduct = null
                                scannedBarcode = null
                                onBack()
                            }
                        ) {
                            Text("Add to Cart (+1)")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                matchedProduct = null
                                scannedBarcode = null
                            }
                        ) {
                            Text("Scan Another")
                        }
                    }
                )
            }

            // Not Found Barcode Result Dialog
            notFoundBarcode?.let { barcode ->
                AlertDialog(
                    onDismissRequest = {
                        notFoundBarcode = null
                        scannedBarcode = null
                    },
                    title = { Text("Barcode Not Found") },
                    text = {
                        Text("No product exists with barcode: $barcode.\nWould you like to add a new product?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Add product with this barcode pre-filled
                                notFoundBarcode = null
                                scannedBarcode = null
                                onBack()
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                notFoundBarcode = null
                                scannedBarcode = null
                            }
                        ) {
                            Text("Scan Again")
                        }
                    }
                )
            }
        }
    }
}
