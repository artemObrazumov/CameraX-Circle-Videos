package com.artemObrazumov.circlevideomessages.postprocessing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class CirclePostProcessing(
    private val context: Context
) : PostProcessing {

    override suspend fun processImage(
        uri: Uri,
        outputFile: File,
        compression: Int
    ) {
        val bitmap = context.loadBitmap(uri).toSoftware()

        val outputBitmap = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(outputBitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val radius = min(bitmap.width, bitmap.height) / 2f
        canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val stream = FileOutputStream(outputFile)
        outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
    }

    override suspend fun processVideo(uri: Uri, outputFile: File) {

    }
}