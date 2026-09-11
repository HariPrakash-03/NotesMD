package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.model.ExportProgress
import com.notesmd.core.domain.provider.DocumentStorageProvider
import com.notesmd.core.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BulkExportNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val documentStorageProvider: DocumentStorageProvider
) {
    operator fun invoke(noteIds: List<Long>?, targetDirUri: String): Flow<ExportProgress> = flow {
        try {
            val notesToExport = if (noteIds == null) {
                noteRepository.getActiveNotes()
            } else {
                noteRepository.getNotesForExport(noteIds)
            }

            if (notesToExport.isEmpty()) {
                emit(ExportProgress.Completed(targetDirUri))
                return@flow
            }

            val total = notesToExport.size
            var current = 0
            val usedFilenames = mutableSetOf<String>()

            notesToExport.forEach { note ->
                emit(ExportProgress.InProgress(current, total))

                var baseFilename = slugify(note.title).takeIf { it.isNotEmpty() } ?: "untitled"
                var filename = "$baseFilename.md"
                var counter = 1
                while (usedFilenames.contains(filename)) {
                    filename = "$baseFilename-$counter.md"
                    counter++
                }
                usedFilenames.add(filename)

                documentStorageProvider.createDocumentInTree(targetDirUri, filename, note.content)
                current++
            }

            emit(ExportProgress.InProgress(total, total))
            emit(ExportProgress.Completed(targetDirUri))
        } catch (e: Exception) {
            emit(ExportProgress.Failed(e.message ?: "Unknown error occurred during export"))
        }
    }

    private fun slugify(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }
}
