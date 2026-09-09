package com.notesmd.core.domain.provider

/**
 * Abstraction over device file access (SAF in V1).
 * URI is passed as a String to keep the domain layer Android-free.
 * The implementation converts to android.net.Uri internally.
 */
interface DocumentStorageProvider {
    suspend fun readExternalFile(uri: String): Result<String>
    suspend fun listFiles(directoryUri: String): Result<List<com.notesmd.core.model.DeviceFile>>
}
