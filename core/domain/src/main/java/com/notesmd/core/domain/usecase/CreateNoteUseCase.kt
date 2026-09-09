package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.NoteRepository
import javax.inject.Inject

/** Creates a new blank note, returns its generated ID. */
class CreateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(title: String = "", content: String = ""): Long {
        return noteRepository.createNote(title, content)
    }
}
