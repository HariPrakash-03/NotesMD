package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.NoteRepository
import com.notesmd.core.model.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Thin wrapper exposing the active notes flow from the repository. */
class ObserveActiveNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = noteRepository.observeActiveNotes()
}
