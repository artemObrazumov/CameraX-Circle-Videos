package com.artemObrazumov.circlevideomessages.camera_compose

import java.io.File

fun File.assertSuffix(suffix: String): File {
    val nameWithoutExtension = nameWithoutExtension
    val newFileName = "$nameWithoutExtension.$suffix"
    return File(parentFile, newFileName)
}