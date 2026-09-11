package com.notesmd.core.data.provider

import android.content.Context
import android.net.Uri
import com.notesmd.core.domain.exception.FileTooLargeException
import com.notesmd.core.domain.provider.DocumentStorageProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Inject

/**
 * SAF-backed file reader with single file descriptor and 5MB size ceiling.
 * TDD Section 4.
 */
class SafDocumentStorageProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentStorageProvider {

    private companion object {
        const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L // 5MB
    }

    override suspend fun readExternalFile(uri: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val parsedUri = Uri.parse(uri)
                context.contentResolver.openFileDescriptor(parsedUri, "r")?.use { pfd ->
                    if (pfd.statSize > MAX_FILE_SIZE_BYTES) {
                        throw FileTooLargeException(pfd.statSize)
                    }
                    FileInputStream(pfd.fileDescriptor)
                        .bufferedReader(Charsets.UTF_8)
                        .readText()
                } ?: throw IOException("Cannot open file descriptor")
            }
        }
        
    override suspend fun listFiles(directoryUri: String): Result<List<com.notesmd.core.model.DeviceFile>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val parsedUri = Uri.parse(directoryUri)
                val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, parsedUri)
                    ?: throw IOException("Cannot read directory")
                
                docFile.listFiles()
                    .filter { it.isDirectory || it.name?.endsWith(".md") == true }
                    .map {
                        com.notesmd.core.model.DeviceFile(
                            uri = it.uri.toString(),
                            name = it.name ?: "Unknown",
                            sizeBytes = it.length(),
                            lastModified = it.lastModified(),
                            isDirectory = it.isDirectory
                        )
                    }
            }
        }

    override suspend fun createDocumentInTree(directoryUri: String, filename: String, content: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val parsedUri = Uri.parse(directoryUri)
                val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, parsedUri)
                    ?: throw IOException("Cannot read directory")
                
                val newFile = docFile.createFile("text/markdown", filename)
                    ?: throw IOException("Could not create document $filename")
                
                context.contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                } ?: throw IOException("Could not open output stream for document")
                
                newFile.uri.toString()
            }
        }
}
