package com.artemObrazumov.circlevideomessages.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

interface CameraState {

    val isVisible: Boolean
    val isRecording: Boolean
    val cameraSelector: CameraSelector
    val preview: Preview
    val videoCapture: VideoCapture<Recorder>

    fun show()
    fun hide()
    suspend fun startRecording()
    fun stopRecording()
    fun changeCamera(cameraSelector: CameraSelector)
}

class CameraStateImpl(
    recorderBuilder: Recorder.Builder = CameraStateDefaults.recorderBuilder,
    previewBuilder: Preview.Builder = CameraStateDefaults.previewBuilder,
    startingCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
) : CameraState {

    private val _videoCapture: VideoCapture<Recorder> by lazy {
        val recorder = recorderBuilder.build()
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

    override suspend fun startRecording() {
        show()
        while (!isReadyToRecord()) {
            delay(100)
        }
        _isRecording.value = true
    }

    override fun stopRecording() {
        _isRecording.value = false
        hide()
    }

    override fun changeCamera(cameraSelector: CameraSelector) {
        _cameraSelector.value = cameraSelector
    }

    private fun isReadyToRecord(): Boolean {
        _preview.camera
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