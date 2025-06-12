package com.artemObrazumov.circlevideomessages.camera_compose

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.video.ExperimentalPersistentRecording
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.OutputOptions
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
import java.io.File
import kotlin.properties.Delegates

class CameraState(
    recorderBuilder: Recorder.Builder = CameraStateDefaults.recorderBuilder,
    previewBuilder: Preview.Builder = CameraStateDefaults.previewBuilder,
    startingCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
) {

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

    val isVisible: Boolean
        get() = _visibilityState.value

    val isRecording: Boolean
        get() = _isRecording.value

    val isTorchEnabled: Boolean
        get() = _isTorchEnabled.value

    val recording: Recording?
        get() = _recording

    val cameraSelector: CameraSelector
        get() = _cameraSelector.value

    val preview: Preview
        get() = _preview

    val imageCapture: ImageCapture
        get() = _imageCapture

    val videoCapture: VideoCapture<Recorder>
        get() = _videoCapture

    fun show() {
        if (_visibilityState.value == true) return
        _visibilityState.value = true
    }

    fun hide() {
        if (_visibilityState.value == false) return
        _visibilityState.value = false
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(
        context: Context,
        name: String = System.currentTimeMillis().toString(),
        relativePath: String = "DCIM/CameraX",
        onFinish: (Uri?, Throwable?) -> Unit = { uri, cause -> },
    ) {
        if (recording != null) return
        show()
        startRecordingToMediaStore(context, name, relativePath, onFinish)
    }

    @OptIn(ExperimentalPersistentRecording::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecordingToMediaStore(
        context: Context,
        name: String,
        relativePath: String,
        onFinish: (Uri?, Throwable?) -> Unit,
    ) {

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

        startRecordingInternal(context, outputOptions, onFinish)
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(
        context: Context,
        outputFile: File,
        onFinish: (Uri?, Throwable?) -> Unit = { uri, cause -> },
    ) {
        if (recording != null) return
        show()
        startRecordingToFile(context, outputFile, onFinish)
    }

    @OptIn(ExperimentalPersistentRecording::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecordingToFile(
        context: Context,
        outputFile: File,
        onFinish: (Uri?, Throwable?) -> Unit,
    ) {
        val outputOptions = FileOutputOptions
            .Builder(outputFile.assertSuffix(MP4))
            .build()
        startRecordingInternal(context, outputOptions, onFinish)
    }

    @OptIn(ExperimentalPersistentRecording::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecordingInternal(
        context: Context,
        outputOptions: OutputOptions,
        onFinish: (Uri?, Throwable?) -> Unit,
    ) {
        _recording = when (outputOptions) {
            is FileOutputOptions -> recorder.prepareRecording(context, outputOptions)
            is MediaStoreOutputOptions -> recorder.prepareRecording(context, outputOptions)
            else -> throw IllegalArgumentException("Unsupported output options")
        }
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

    fun stopRecording() {
        _recording?.stop()
        _recording = null
    }

    fun changeCamera(cameraSelector: CameraSelector) {
        _cameraSelector.value = cameraSelector
        if (cameraSelector != CameraSelector.DEFAULT_BACK_CAMERA) {
            disableTorch()
        }
    }

    fun takePhoto(
        context: Context,
        name: String = System.currentTimeMillis().toString(),
        relativePath: String = "DCIM/CameraX",
        onSuccess: (Uri?) -> Unit = {},
        onFailure: (ImageCaptureException) -> Unit = {},
    ) {
        show()
        takePhotoToMediaStore(context, name, relativePath, onSuccess, onFailure)
    }

    private fun takePhotoToMediaStore(
        context: Context,
        name: String = System.currentTimeMillis().toString(),
        relativePath: String = "DCIM/CameraX",
        onSuccess: (Uri?) -> Unit = {},
        onFailure: (ImageCaptureException) -> Unit = {},
    ) {

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

        takePhotoInternal(context, outputOptions, onSuccess, onFailure)
    }

    fun takePhoto(
        context: Context,
        outputFile: File,
        onSuccess: (Uri?) -> Unit = {},
        onFailure: (ImageCaptureException) -> Unit = {},
    ) {
        show()
        takePhotoToFile(context, outputFile, onSuccess, onFailure)
    }

    private fun takePhotoToFile(
        context: Context,
        outputFile: File,
        onSuccess: (Uri?) -> Unit = {},
        onFailure: (ImageCaptureException) -> Unit = {},
    ) {
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(outputFile.assertSuffix(JPG))
            .build()
        takePhotoInternal(context, outputOptions, onSuccess, onFailure)
    }

    private fun takePhotoInternal(
        context: Context,
        outputOptions: ImageCapture.OutputFileOptions,
        onSuccess: (Uri?) -> Unit,
        onFailure: (ImageCaptureException) -> Unit
    ) {
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

    fun enableTorch() {
        if (_cameraSelector.value == CameraSelector.DEFAULT_BACK_CAMERA) {
            _isTorchEnabled.value = true
        }
    }

    fun disableTorch() {
        _isTorchEnabled.value = false
    }

    companion object Suffix {
        const val JPG = "jpg"
        const val MP4 = "mp4"
    }
}

@Composable
fun rememberCameraState(
    recorderBuilder: Recorder.Builder = CameraStateDefaults.recorderBuilder,
    previewBuilder: Preview.Builder = CameraStateDefaults.previewBuilder,
    startingCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
): CameraState {
    return remember {
        CameraState(recorderBuilder, previewBuilder, startingCameraSelector)
    }
}

object CameraStateDefaults {

    val recorderBuilder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))

    val previewBuilder = Preview.Builder()
}
