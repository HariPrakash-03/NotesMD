package com.notesmd.core.data.repository

import com.notesmd.core.data.mapper.NoteMapper.toDomain
import com.notesmd.core.database.NoteDao
import com.notesmd.core.database.NoteEntity
import com.notesmd.core.domain.repository.NoteRepository
import com.notesmd.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun observeActiveNotes(): Flow<List<Note>> =
        noteDao.observeActiveNotes().map { relations ->
            relations.map { it.toDomain() }
        }

    override suspend fun getNoteById(id: Long): Note? =
        noteDao.getNoteWithTags(id)?.toDomain()

    override suspend fun createNote(title: String, content: String, remoteId: String?): Long =
        noteDao.insertNote(NoteEntity(title = title, content = content, remoteId = remoteId))

    override suspend fun updateNoteContent(noteId: Long, title: String, content: String) =
        noteDao.upsertNoteContent(noteId, title, content)

    override suspend fun softDeleteNote(noteId: Long) =
        noteDao.softDeleteNote(noteId)

    override suspend fun hardDeleteNote(noteId: Long) =
        noteDao.hardDeleteNote(noteId)

    override suspend fun getNotesForExport(noteIds: List<Long>): List<Note> =
        noteDao.getNotesByIds(noteIds).map { it.toDomain() }

    override suspend fun getActiveNotes(): List<Note> =
        noteDao.getActiveNotes().map { it.toDomain() }
}
