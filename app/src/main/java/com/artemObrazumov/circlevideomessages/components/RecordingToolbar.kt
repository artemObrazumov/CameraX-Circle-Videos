package com.artemObrazumov.circlevideomessages.components

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                Icons.Default.Cameraswitch,
                contentDescription = null
            )
        }

        IconButton(
            onClick = {
            }
        ) {
            Icon(
                Icons.Default.Camera,
                contentDescription = null
            )
        }
    }
}