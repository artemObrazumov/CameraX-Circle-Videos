package com.artemObrazumov.circlevideomessages.postprocessing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.opengl.GLES20
import androidx.annotation.OptIn
import androidx.core.graphics.createBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.VideoFrameProcessingException
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.GlUtil.GlException
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram
import androidx.media3.effect.Crop
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resumeWithException
import kotlin.math.min

@OptIn(UnstableApi::class)
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

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun processVideo(
        uri: Uri,
        outputFile: File
    ) {
        val effects = prepareVideoEffects(uri)
        val mediaItem = EditedMediaItem
            .Builder(MediaItem.fromUri(uri))
            .setEffects(effects)
            .build()
        suspendCancellableCoroutine { continuation ->
            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(
                        composition: Composition,
                        exportResult: ExportResult
                    ) {
                        continuation.resume(Unit) {}
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        continuation.resumeWithException(exportException)
                    }
                })
                .build()
            transformer.start(mediaItem, outputFile.absolutePath)
        }
    }

    private fun prepareVideoEffects(
        uri: Uri,
    ): Effects {
        val (width, height) = uri.videoSize(context)
        val minSide = min(width, height).toFloat()
        val widthRatio = minSide / width
        val heightRatio = minSide / height
        val cropEffect = Crop(
            -widthRatio,
            widthRatio,
            -heightRatio,
            heightRatio
        )
        val circleCropEffect = CircleCropEffect()
        return Effects(emptyList(), listOf(cropEffect, circleCropEffect))
    }
}

@OptIn(UnstableApi::class)
class CircleCropEffect : GlEffect {
    override fun toGlShaderProgram(
        context: Context,
        useHdr: Boolean
    ): GlShaderProgram {
        return CircleCropShaderProgram(context)
    }

    override fun isNoOp(inputWidth: Int, inputHeight: Int): Boolean {
        return super.isNoOp(inputWidth, inputHeight)
    }
}

@UnstableApi
class CircleCropShaderProgram(
    context: Context
) : BaseGlShaderProgram(false, 1) {

    val glProgram: GlProgram = GlProgram(context, VERTEX_SHADER, FRAGMENT_SHADER)

    init {
        glProgram.setBufferAttribute(
            "aPosition",
            GlUtil.getNormalizedCoordinateBounds(),
            GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE
        )
    }

    companion object {
        private const val VERTEX_SHADER = "shaders/circle_vertex_shader.glsl"
        private const val FRAGMENT_SHADER = "shaders/circle_fragment_shader.glsl"
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }

    override fun drawFrame(inputTexId: Int, presentationTimeUs: Long) {
        glProgram.use()
        glProgram.setSamplerTexIdUniform("uTexSampler", inputTexId, 0)
        glProgram.bindAttributesAndUniforms()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    @Throws(VideoFrameProcessingException::class)
    override fun release() {
        super.release()
        try {
            glProgram.delete()
        } catch (e: GlException) {
            throw VideoFrameProcessingException(e)
        }
    }
}
