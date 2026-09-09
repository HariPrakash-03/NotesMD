package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.NoteRepository
import javax.inject.Inject

/** Debounce target — updates title + content + updatedAt on a single note. */
class SaveNoteContentUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Long, title: String, content: String): Result<Unit> =
        runCatching { noteRepository.updateNoteContent(noteId, title, content) }
}
