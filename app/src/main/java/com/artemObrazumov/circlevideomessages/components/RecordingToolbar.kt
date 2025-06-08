package com.artemObrazumov.circlevideomessages.components

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.artemObrazumov.circlevideomessages.camera.CameraState
import kotlinx.coroutines.launch

@Composable
fun RecordingToolbar(
    state: CameraState,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Row(
        modifier = modifier
    ) {
        RecordingButton(
            isRecording = state.isRecording,
            onStartRecording = {
                scope.launch {
                    state.startRecording(
                        context = context
                    )
                }
            },
            onStopRecording = {
                state.stopRecording()
            }
        )

        IconButton(
            onClick = {
                state.changeCamera(
                    if (state.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                )
            }
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null
            )
        }
    }
}