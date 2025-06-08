package com.artemObrazumov.circlevideomessages.camera

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun CameraPreview(
    state: CameraState,
    modifier: Modifier = Modifier
) {

    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val view = PreviewView(context)
            scope.launch {
                cameraProvider = async {
                    ProcessCameraProvider.getInstance(context)
                }.await().get()
            }
            state.preview.surfaceProvider = view.surfaceProvider
            return@AndroidView view
        },
        update = {
            state.recording?.pause()

            cameraProvider?.unbindAll()
            if (state.isVisible) {
                try {
                    cameraProvider?.bindToLifecycle(
                        owner,
                        state.cameraSelector,
                        state.preview,
                        state.videoCapture
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            state.recording?.resume()
        }
    )
}