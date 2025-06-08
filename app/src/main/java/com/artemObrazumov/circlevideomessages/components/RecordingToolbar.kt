package com.artemObrazumov.circlevideomessages.components

import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.artemObrazumov.circlevideomessages.camera_compose.CameraState
import kotlinx.coroutines.launch

@Composable
fun RecordingToolbar(
    state: CameraState,
    onRecordingSuccess: () -> Unit,
    onRecordingFailure: () -> Unit,
    onPhotoSuccess: (Uri?) -> Unit,
    onPhotoFailure: () -> Unit,
    modifier: Modifier = Modifier,
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
                        context = context,
                        onFinish = { uri, _ ->
                            if (uri == null) {
                                onRecordingFailure()
                            } else {
                                onRecordingSuccess()
                            }
                        }
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
            },
            enabled = state.isVisible
        ) {
            Icon(
                Icons.Default.Cameraswitch,
                contentDescription = null
            )
        }

        IconButton(
            onClick = {
                state.takePhoto(
                    context = context,
                    onSuccess = { uri ->
                        onPhotoSuccess(uri)
                    },
                    onFailure = { _ ->
                        onPhotoFailure()
                    }
                )
            },
            enabled = state.isVisible
        ) {
            Icon(
                Icons.Default.Camera,
                contentDescription = null
            )
        }

        IconButton(
            onClick = {
                if (state.isTorchEnabled) {
                    state.disableTorch()
                } else {
                    state.enableTorch()
                }
            },
            enabled = state.isVisible
        ) {
            Icon(
                if (state.isTorchEnabled) {
                    Icons.Default.FlashlightOn
                } else {
                    Icons.Default.FlashlightOff
                },
                contentDescription = null
            )
        }
    }
}