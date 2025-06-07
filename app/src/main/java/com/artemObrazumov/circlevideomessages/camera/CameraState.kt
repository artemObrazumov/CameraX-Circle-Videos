package com.artemObrazumov.circlevideomessages.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.video.ExperimentalPersistentRecording
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

interface CameraState {

    val isVisible: Boolean
    val isRecording: Boolean
    val recording: Recording?
    val cameraSelector: CameraSelector
    val preview: Preview
    val videoCapture: VideoCapture<Recorder>

    fun show()
    fun hide()
    suspend fun startRecording(
        context: Context,
        name: String = System.currentTimeMillis().toString(),
        relativePath: String = "DCIM/CameraX"
    )
    fun stopRecording()
    fun changeCamera(cameraSelector: CameraSelector)
}

class CameraStateImpl(
    recorderBuilder: Recorder.Builder = CameraStateDefaults.recorderBuilder,
    previewBuilder: Preview.Builder = CameraStateDefaults.previewBuilder,
    startingCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
) : CameraState {

    private var _recording: Recording? = null

    private val recorder by lazy {
        recorderBuilder.build()
    }

    private val _videoCapture: VideoCapture<Recorder> by lazy {
        VideoCapture.withOutput(recorder)
    }

    private val _preview: Preview by lazy {
        previewBuilder.build()
    }

    private var _visibilityState = mutableStateOf(false)
    private var _isRecording = mutableStateOf(false)
    private var _cameraSelector = mutableStateOf(startingCameraSelector)

    override val isVisible: Boolean
        get() = _visibilityState.value

    override val isRecording: Boolean
        get() = _isRecording.value

    override val recording: Recording?
        get() = _recording

    override val cameraSelector: CameraSelector
        get() = _cameraSelector.value

    override val preview: Preview
        get() = _preview

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
    override suspend fun startRecording(
        context: Context,
        name: String,
        relativePath: String
    ) {
        Log.i("RECORDING", "starting")
        show()
//        while (!isReadyToRecord()) {
//            delay(100)
//        }
        prepareRecording(context, name, relativePath)
        _isRecording.value = true
    }

    @OptIn(ExperimentalPersistentRecording::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun prepareRecording(
        context: Context,
        name: String,
        relativePath: String
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
            ) { recordEvent ->
                println(recordEvent)
            }
    }

    override fun stopRecording() {
        Log.i("RECORDING", "stopping")
        _recording?.stop()
        _recording = null
        _isRecording.value = false
        hide()
    }

    override fun changeCamera(cameraSelector: CameraSelector) {
        Log.i("RECORDING", "changed camera")
        _cameraSelector.value = cameraSelector
    }

    private fun isReadyToRecord(): Boolean {
        return false
    }
}

@Composable
fun rememberCameraState(): CameraState {
    return remember {
        CameraStateImpl()
    }
}

object CameraStateDefaults {

    val recorderBuilder = Recorder.Builder()
        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))

    val previewBuilder = Preview.Builder()
}