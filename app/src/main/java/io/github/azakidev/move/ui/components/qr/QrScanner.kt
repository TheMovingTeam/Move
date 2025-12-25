package io.github.azakidev.move.ui.components.qr

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import io.github.azakidev.move.R
import io.github.azakidev.move.data.items.Capabilities
import io.github.azakidev.move.data.items.ProviderItem
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors

class BarcodeAnalyser(
    val callback: (String) -> Unit
) : ImageAnalysis.Analyzer {
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val options =
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()

        val scanner = BarcodeScanning.getClient(options)
        val mediaImage = imageProxy.image
        mediaImage?.let {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image).addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    barcodes.forEach { barcode ->
                        callback(barcode.rawValue ?: "")
                    }
                }
            }.addOnFailureListener {
                // Task failed with an exception
                // ...
            }
        }
        imageProxy.close()
    }
}

class CameraPreviewViewModel(analyzerCallBack: (String) -> Unit) : ViewModel() {

    private val executor = Executors.newSingleThreadExecutor()

    // Used to set up a link between the Camera and your UI.
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    private val imageCaptureUseCase = ImageCapture.Builder().build()

    val imageAnalyzerUseCase = ImageAnalysis.Builder().build().also {
        it.setAnalyzer(executor, BarcodeAnalyser { url ->
            analyzerCallBack(url)
        })
    }


    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        processCameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            cameraPreviewUseCase,
            imageCaptureUseCase,
            imageAnalyzerUseCase
        )

        // Cancellation signals we're done with the camera
        try { awaitCancellation() } finally { processCameraProvider.unbindAll() }
    }
}

@Composable
fun QrScanner(
    modifier: Modifier = Modifier,
    providers: List<ProviderItem>,
    callback: (Pair<Int, ProviderItem>) -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    val msg = stringResource(R.string.qrNotFound)

    val viewModel = viewModel {
        CameraPreviewViewModel(
            analyzerCallBack = { url ->
                try {
                    val parsed = parseQr(
                        providers = providers, url = url
                    )
                    callback(parsed)
                } catch (e: Exception) {
                    Log.e("WARNING", "URL not recognized", e)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()

    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier
        )
    }
}

fun parseQr(
    providers: List<ProviderItem>, url: String
): Pair<Int, ProviderItem> {
    val qrUrls = providers.filter { it.capabilities.contains(Capabilities.QrScan) }
        .map { it.qrFormat.replace("@stop", "") }
    val filter = qrUrls.filter { url.contains(it) }
    val provider = providers.find { it.qrFormat.contains(filter.first()) } ?: ProviderItem()
    return Pair(
        url.replace(filter.first(), "").toInt(),
        provider
    )
}