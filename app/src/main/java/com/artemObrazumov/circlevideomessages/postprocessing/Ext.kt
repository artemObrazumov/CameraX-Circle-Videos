package com.artemObrazumov.circlevideomessages.postprocessing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

suspend fun Uri.imagePostProcessing(
    postProcessing: PostProcessing,
    outputFile: File,
    compression: Int = 100
) {
    withContext(Dispatchers.Main) {
        postProcessing.processImage(
            this@imagePostProcessing,
            outputFile,
            compression
        )
    }
}

suspend fun Uri.videoPostProcessing(
    postProcessing: PostProcessing,
    outputFile: File
) {
    withContext(Dispatchers.Main) {
        postProcessing.processVideo(
            this@videoPostProcessing,
            outputFile
        )
    }
}

fun Bitmap.toSoftware(): Bitmap {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || config != Bitmap.Config.HARDWARE) return this
    return copy(Bitmap.Config.ARGB_8888, true)
}

fun Uri.temporaryFile(context: Context): File? {
    val inputStream = context.contentResolver.openInputStream(this) ?: return null
    val temporaryFile = File(context.cacheDir, "${UUID.randomUUID()}.${extension(context)}")
    FileOutputStream(temporaryFile).use { outputStream ->
        inputStream.copyTo(outputStream)
    }
    inputStream.close()
    return temporaryFile
}

fun Uri.videoSize(context: Context): Pair<Int, Int> {
    val mediaMetadataRetriever = MediaMetadataRetriever()
    var width = 0
    var height = 0
    try {
        mediaMetadataRetriever.setDataSource(context, this)
        width = mediaMetadataRetriever
            .extractMetadata(METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
        height = mediaMetadataRetriever
            .extractMetadata(METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        mediaMetadataRetriever.release()
    }
    return Pair(width, height)
}

fun Uri.extension(context: Context): String {
    val mimeType = context.contentResolver.getType(this)
    if (mimeType != null) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).toString()
    }
    return try {
        toFile().extension
    } catch (_: Exception) {
        ""
    }
}

fun Context.loadBitmap(uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
    } else {
        MediaStore.Images.Media.getBitmap(contentResolver, uri)
    }
}