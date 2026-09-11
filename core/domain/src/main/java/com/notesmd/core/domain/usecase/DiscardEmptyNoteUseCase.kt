package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.NoteRepository
import javax.inject.Inject

/** Hard-deletes a note when both title and content are blank on navigate-away. */
class DiscardEmptyNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Long) {
        noteRepository.hardDeleteNote(noteId)
    }
}
