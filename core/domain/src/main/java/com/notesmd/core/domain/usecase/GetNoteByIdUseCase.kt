package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.NoteRepository
import com.notesmd.core.model.Note
import javax.inject.Inject

/** Fetches a single note by ID. Returns null if not found or soft-deleted. */
class GetNoteByIdUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Long): Note? {
        return noteRepository.getNoteById(noteId)
    }
}
