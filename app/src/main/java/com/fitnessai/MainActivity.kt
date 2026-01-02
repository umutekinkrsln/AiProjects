package com.fitnessai

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.fitnessai.ui.theme.FitnessaiTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import android.graphics.PointF
import kotlin.math.acos
import kotlin.math.sqrt
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {

    // UI state değişkenleri
    private val squatAngleState = mutableStateOf(0.0)
    private val squatStatusState = mutableStateOf("Standing")

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val composeView = ComposeView(this)
        setContentView(composeView)
        composeView.setContent {
            FitnessaiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    FeedbackUI()
                }
            }
        }
    }

    @Composable
    fun FeedbackUI() {
        Column {
            CameraPreview()
            Text("Angle: ${squatAngleState.value}")
            Text("Status: ${squatStatusState.value}")

            // Görsel geri bildirim
            when (squatStatusState.value) {
                "Squat detected!" -> Text("✔️ Doğru Squat", color = Color.Green)
                "Half squat" -> Text("⚠️ Yarım Squat", color = Color.Yellow)
                "Standing" -> Text("ℹ️ Ayakta", color = Color.Gray)
                "No pose detected" -> Text("❌ Poz algılanmadı", color = Color.Red)
                "Detection error" -> Text("❌ Algılama hatası", color = Color.Red)
            }
        }
    }

    @Composable
    fun CameraPreview() {
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = androidx.camera.core.Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    val options = PoseDetectorOptions.Builder()
                        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                        .build()
                    val poseDetector = PoseDetection.getClient(options)

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                processImageProxy(poseDetector, imageProxy)
                            }
                        }

                    cameraProvider.bindToLifecycle(
                        this@MainActivity,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        poseDetector: com.google.mlkit.vision.pose.PoseDetector,
        imageProxy: ImageProxy
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    // Hem sol hem sağ bacak kontrolü
                    val hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                        ?: pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
                    val knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                        ?: pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
                    val ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
                        ?: pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

                    if (hip != null && knee != null && ankle != null) {
                        val angle = calculateAngle(hip.position, knee.position, ankle.position)
                        squatAngleState.value = angle

                        squatStatusState.value = when {
                            angle < 100 -> "Squat detected!"
                            angle < 160 -> "Half squat"
                            else -> "Standing"
                        }

                        Log.d("SquatCheck", "${squatStatusState.value} Angle: $angle")
                    } else {
                        // Landmark bulunamadı
                        squatStatusState.value = "No pose detected"
                        squatAngleState.value = 0.0
                        Log.d("SquatCheck", "No pose detected")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("PoseDetection", "Pose detection failed", e)
                    squatStatusState.value = "Detection error"
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun calculateAngle(a: PointF, b: PointF, c: PointF): Double {
        val ab = Pair(a.x.toDouble() - b.x.toDouble(), a.y.toDouble() - b.y.toDouble())
        val cb = Pair(c.x.toDouble() - b.x.toDouble(), c.y.toDouble() - b.y.toDouble())
        val dot = ab.first * cb.first + ab.second * cb.second
        val abLen = sqrt(ab.first * ab.first + ab.second * ab.second)
        val cbLen = sqrt(cb.first * cb.first + cb.second * cb.second)
        return Math.toDegrees(acos(dot / (abLen * cbLen)))
    }
}
