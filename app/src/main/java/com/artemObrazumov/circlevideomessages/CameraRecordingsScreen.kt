package com.artemObrazumov.circlevideomessages

import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.artemObrazumov.circlevideomessages.camera_compose.CameraPreview
import com.artemObrazumov.circlevideomessages.camera_compose.rememberCameraState
import com.artemObrazumov.circlevideomessages.components.RecordingToolbar
import com.artemObrazumov.circlevideomessages.postprocessing.CirclePostProcessing
import com.artemObrazumov.circlevideomessages.postprocessing.imagePostProcessing
import com.artemObrazumov.circlevideomessages.postprocessing.temporaryFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CameraRecordingsScreen(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var messages by remember { mutableStateOf(listOf<String>()) }
        var messageCleanupJob: Job? = null
        val circlePostProcessing = remember { CirclePostProcessing(context) }

        LaunchedEffect(messages) {
            messageCleanupJob?.cancel()
            messageCleanupJob = launch {
                if (messages.isNotEmpty()) {
                    delay(3000)
                    messages = emptyList()
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            val state = rememberCameraState(
                recorderBuilder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(Quality.LOWEST)
                    )
            )

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
                state = state,
                onRecordingSuccess = {
                    messages += "Recording finished successfully"
                },
                onRecordingFailure = {
                    messages += "Recording finished with error"
                },
                onPhotoSuccess = { uri ->
                    messages += "Image capture finished successfully"
                    uri?.let { temporaryUri ->
                        val temporaryFile = temporaryUri.temporaryFile(context) ?: return@let
                        val extension = temporaryFile.extension
                        val outputFile =
                            File(temporaryFile.parent, "${temporaryFile.name}_processed.$extension")

                        scope.launch {
                            temporaryUri.imagePostProcessing(
                                circlePostProcessing,
                                outputFile = outputFile,
                            )
                            messages += "Image post processing finished successfully"
                        }
                    }
                },
                onPhotoFailure = {
                    messages += "Image capture finished with error"
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .align(Alignment.BottomCenter)
        ) {
            messages.forEach { message ->
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 2.dp
                        ),
                ) {
                    Text(message)
                }
            }
        }
    }
}