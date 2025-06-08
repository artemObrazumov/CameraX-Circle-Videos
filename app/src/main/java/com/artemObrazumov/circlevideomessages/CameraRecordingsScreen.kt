package com.artemObrazumov.circlevideomessages

import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.artemObrazumov.circlevideomessages.camera.CameraPreview
import com.artemObrazumov.circlevideomessages.camera.rememberCameraState
import com.artemObrazumov.circlevideomessages.components.RecordingToolbar

@Composable
fun CameraRecordingsScreen(
    modifier: Modifier = Modifier
) {

    val state = rememberCameraState(
        recorderBuilder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(Quality.LOWEST)
            )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        val animationFraction by animateFloatAsState(
            targetValue = if (state.isVisible) {
                1f
            } else {
                0f
            }
        )

        CameraPreview(
            state = state,
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .alpha(animationFraction)
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        RecordingToolbar(
            state = state
        )
    }
}