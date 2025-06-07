package com.artemObrazumov.circlevideomessages

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.artemObrazumov.circlevideomessages.camera.CameraPreview
import com.artemObrazumov.circlevideomessages.camera.rememberCameraState
import kotlinx.coroutines.launch

@Composable
fun CameraRecordingsScreen(
    modifier: Modifier = Modifier
) {

    val state = rememberCameraState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CameraPreview(
            state = state,
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                val recordingJob = scope.launch {
                                    state.startRecording()
                                }
                                awaitRelease()
                                state.stopRecording()
                            }
                        )
                    }
            ) {
                Icon(
                    if (state.isRecording) {
                        Icons.Default.ThumbUp
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
    }
}