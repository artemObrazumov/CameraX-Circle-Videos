package com.artemObrazumov.circlevideomessages.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.video.ExperimentalPersistentRecording
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

interface CameraState {

    val isVisible: Boolean
    val isRecording: Boolean
    val isTorchEnabled: Boolean
    val recording: Recording?
    val cameraSelector: CameraSelector
    val preview: Preview
    val imageCapture: ImageCapture
    val videoCapture: VideoCapture<Recorder>

    fun show()
    fun hide()
    fun startRecording(
        context: Context,
        name: String = System.currentTimeMillis().toString(),
        relativePath: String = "DCIM/CameraX",
        onFinish: (Uri?, Throwable?) -> Unit = { uri, cause -> },
    )
    fun stopRecording()
    fun changeCamera(cameraSelector: CameraSelector)
    fun takePhoto(
        context: Context,
        name: String = System.currentTimeMillis().toString(),
        relativePath: String = "DCIM/CameraX",
        onSuccess: (Uri?) -> Unit = {},
        onFailure: (ImageCaptureException) -> Unit = {},
    )
    fun enableTorch()
    fun disableTorch()
}

class CameraStateImpl(
    recorderBuilder: Recorder.Builder = CameraStateDefaults.recorderBuilder,
    previewBuilder: Preview.Builder = CameraStateDefaults.previewBuilder,
    startingCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
) : CameraState {

    private var _recording: Recording? by Delegates.observable(null) { _, _, newRecording ->
        _isRecording.value = (newRecording != null)
    }

    private val recorder by lazy {
        recorderBuilder.build()
    }

    private val _videoCapture: VideoCapture<Recorder> by lazy {
        VideoCapture.withOutput(recorder)
    }

    private val _preview: Preview by lazy {
        previewBuilder.build()
    }

    private val _imageCapture by lazy {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    private var _visibilityState = mutableStateOf(false)
    private var _isRecording = mutableStateOf(false)
    private var _isTorchEnabled = mutableStateOf(false)
    private var _cameraSelector = mutableStateOf(startingCameraSelector)

    override val isVisible: Boolean
        get() = _visibilityState.value

    override val isRecording: Boolean
        get() = _isRecording.value

    override val isTorchEnabled: Boolean
        get() = _isTorchEnabled.value

    override val recording: Recording?
        get() = _recording

    override val cameraSelector: CameraSelector
        get() = _cameraSelector.value

    override val preview: Preview
        get() = _preview

    override val imageCapture: ImageCapture
        get() = _imageCapture

    override val videoCapture: VideoCapture<Recorder>
        get() = _videoCapture

    override fun show() {
        if (_visibilityState.value == true) return
        _visibilityState.value = true
    }

    override fun hide() {
        if (_visibilityState.value == false) return
        _visibilityState.value = false
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(
        context: Context,
        name: String,
        relativePath: String,
        onFinish: (Uri?, Throwable?) -> Unit,
    ) {
        if (recording != null) return
        Log.i("RECORDING", "starting")
        show()
        startRecordingInternal(context, name, relativePath, onFinish)
    }

    @OptIn(ExperimentalPersistentRecording::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecordingInternal(
        context: Context,
        name: String,
        relativePath: String,
        onFinish: (Uri?, Throwable?) -> Unit,
    ) {

        Log.i("RECORDING", "preparing")
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, relativePath)
        }

        val outputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()

        _recording = recorder.prepareRecording(context, outputOptions)
            .asPersistentRecording()
            .withAudioEnabled()
            .start(
                ContextCompat.getMainExecutor(context)
            ) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    onFinish(event.outputResults.outputUri, event.cause)
                }
            }
    }

    override fun stopRecording() {
        Log.i("RECORDING", "stopping")
        _recording?.stop()
        _recording = null
    }

    override fun changeCamera(cameraSelector: CameraSelector) {
        Log.i("RECORDING", "changed camera")
        _cameraSelector.value = cameraSelector
        if (cameraSelector != CameraSelector.DEFAULT_BACK_CAMERA) {
            disableTorch()
        }
    }

    override fun takePhoto(
        context: Context,
        name: String,
        relativePath: String,
        onSuccess: (Uri?) -> Unit,
        onFailure: (ImageCaptureException) -> Unit
    ) {

        Log.i("RECORDING", "taking photo")
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        _imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onSuccess(output.savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    onFailure(exception)
                }
            }
        )
    }

    override fun enableTorch() {
        if (_cameraSelector.value == CameraSelector.DEFAULT_BACK_CAMERA) {
            _isTorchEnabled.value = true
        }
    }

    override fun disableTorch() {
        _isTorchEnabled.value = false
    }
}

@Composable
fun rememberCameraState(
    recorderBuilder: Recorder.Builder = CameraStateDefaults.recorderBuilder,
    previewBuilder: Preview.Builder = CameraStateDefaults.previewBuilder,
    startingCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
): CameraState {
    return remember {
        CameraStateImpl(recorderBuilder, previewBuilder, startingCameraSelector)
    }
}

object CameraStateDefaults {

    val recorderBuilder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))

    val previewBuilder = Preview.Builder()
}