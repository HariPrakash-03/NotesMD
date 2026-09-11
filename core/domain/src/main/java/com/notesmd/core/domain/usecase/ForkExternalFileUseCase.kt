package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.provider.DocumentStorageProvider
import com.notesmd.core.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Copy-on-Write: reads an external file via SAF, creates a new local note.
 * Design Doc Section 6.1.
 */
class ForkExternalFileUseCase @Inject constructor(
    private val documentStorageProvider: DocumentStorageProvider,
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(uri: String): Result<Long> = runCatching {
        val content = documentStorageProvider.readExternalFile(uri).getOrThrow()
        val title = extractTitle(content)
        noteRepository.createNote(title, content, remoteId = uri)
    }

    private fun extractTitle(content: String): String {
        val firstLine = content.lineSequence().firstOrNull { it.isNotBlank() } ?: ""
        return if (firstLine.startsWith("#")) {
            firstLine.trimStart('#').trim()
        } else {
            firstLine.take(50)
        }
    }
}
