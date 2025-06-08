package com.artemObrazumov.circlevideomessages.postprocessing

import android.net.Uri
import java.io.File

interface PostProcessing {

    suspend fun processImage(
        uri: Uri,
        outputFile: File,
        compression: Int = 100
    )

    suspend fun processVideo(
        uri: Uri,
        outputFile: File
    )
}