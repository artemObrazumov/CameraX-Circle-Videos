package com.artemObrazumov.circlevideomessages.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun RecordingButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (!isRecording) {
                            onStartRecording()
                            if (tryAwaitRelease()) {
                                onStopRecording()
                            }
                        } else {
                            onStopRecording()
                        }
                    }
                )
            }
    ) {
        Icon(
            if (isRecording) {
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