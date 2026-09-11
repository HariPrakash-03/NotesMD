package com.notesmd.core.domain.repository

import com.notesmd.core.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeActiveNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun createNote(title: String, content: String, remoteId: String? = null): Long
    suspend fun updateNoteContent(noteId: Long, title: String, content: String)
    suspend fun softDeleteNote(noteId: Long)
    suspend fun hardDeleteNote(noteId: Long)
    suspend fun getNotesForExport(noteIds: List<Long>): List<Note>
    suspend fun getActiveNotes(): List<Note>
}
