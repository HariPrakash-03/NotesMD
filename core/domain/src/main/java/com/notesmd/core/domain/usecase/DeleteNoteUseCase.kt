package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.NoteRepository
import javax.inject.Inject

/** Soft-deletes a note (sets is_deleted = 1). V2-ready for sync propagation. */
class DeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Long) {
        noteRepository.softDeleteNote(noteId)
    }
}
