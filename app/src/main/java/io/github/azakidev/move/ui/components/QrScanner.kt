package io.github.azakidev.move.ui.components

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import io.github.azakidev.move.R
import io.github.azakidev.move.data.Capabilities
import io.github.azakidev.move.data.ProviderItem
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


@ExperimentalGetImage
@Composable
fun QrScanner(
    modifier: Modifier = Modifier, providers: List<ProviderItem>, callback: (Int) -> Unit
) {
    val msg = stringResource(R.string.qrNotFound)

    AndroidView(
        { context ->
            val cameraExecutor = Executors.newSingleThreadExecutor()
            val previewView = PreviewView(context).also {
                it.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val imageCapture = ImageCapture.Builder().build()

                val imageAnalyzer = ImageAnalysis.Builder().build().also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyser { url ->
                            try {
                                val id = parseQr(
                                    providers = providers, url = url
                                )
                                callback(id)
                            } catch (e: Exception) {
                                Log.e("WARNING", "URL not recognized", e)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        })
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        context as ComponentActivity,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalyzer
                    )

                } catch (exc: Exception) {
                    Log.e("DEBUG", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
            previewView
        }, modifier = modifier
    )
}

fun parseQr(
    providers: List<ProviderItem>, url: String
): Int {
    val qrUrls = providers.filter { it.capabilities.contains(Capabilities.QrScan) }
        .map { it.qrFormat.replace("@stop", "") }
    val filter = qrUrls.filter { url.contains(it) }
    return url.replace(filter.first(), "").toInt()
}