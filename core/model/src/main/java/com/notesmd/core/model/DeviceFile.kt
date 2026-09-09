package com.notesmd.core.model

data class DeviceFile(
    val uri: String,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val isImported: Boolean = false
)
