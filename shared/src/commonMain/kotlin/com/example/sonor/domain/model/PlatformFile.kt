package com.example.sonor.domain.model

// Platform-agnostic file representation
expect class PlatformFile {
    val name: String
    val path: String
    val isDirectory: Boolean
    val length: Long
}
