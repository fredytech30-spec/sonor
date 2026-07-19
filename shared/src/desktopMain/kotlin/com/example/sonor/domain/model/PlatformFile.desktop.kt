package com.example.sonor.domain.model

import java.io.File

actual class PlatformFile(
    private val file: File
) {
    actual val name: String
        get() = file.name
    actual val path: String
        get() = file.absolutePath
    actual val isDirectory: Boolean
        get() = file.isDirectory
    actual val length: Long
        get() = file.length()
}

fun File.toPlatformFile(): PlatformFile = PlatformFile(this)
fun PlatformFile.toFile(): File = File(path)
