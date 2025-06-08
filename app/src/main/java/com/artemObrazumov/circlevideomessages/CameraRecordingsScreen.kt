package com.artemObrazumov.circlevideomessages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.artemObrazumov.circlevideomessages.camera.CameraPreview
import com.artemObrazumov.circlevideomessages.camera.rememberCameraState
import com.artemObrazumov.circlevideomessages.components.RecordingToolbar

@Composable
fun CameraRecordingsScreen(
    modifier: Modifier = Modifier
) {

    val state = rememberCameraState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
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

        RecordingToolbar(
            state = state
        )
    }
}