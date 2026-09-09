package com.notesmd.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.notesmd.core.model.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // --- TDD Section 6: specified queries ---

    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE is_deleted = 0
        ORDER BY updated_at DESC
    """)
    fun observeActiveNotes(): Flow<List<NoteWithTagsRelation>>

    @Query("""
        UPDATE notes
        SET title = :title, content = :content, updated_at = :updatedAt, sync_state = :syncState
        WHERE id = :noteId
    """)
    suspend fun upsertNoteContent(
        noteId: Long,
        title: String,
        content: String,
        updatedAt: Long = System.currentTimeMillis(),
        syncState: SyncState = SyncState.PENDING_UPDATE
    )

    @Query("DELETE FROM note_tag_cross_ref WHERE note_id = :noteId")
    suspend fun clearNoteTags(noteId: Long)

    @Insert
    suspend fun insertNoteTags(refs: List<NoteTagCrossRef>)

    @Transaction
    suspend fun syncNoteTags(noteId: Long, tagIds: List<Long>) {
        clearNoteTags(noteId)
        insertNoteTags(tagIds.map { NoteTagCrossRef(noteId, it) })
    }

    // --- Additional CRUD queries for use cases ---

    @Insert
    suspend fun insertNote(note: NoteEntity): Long

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId AND is_deleted = 0")
    suspend fun getNoteWithTags(noteId: Long): NoteWithTagsRelation?

    @Query("""
        UPDATE notes
        SET is_deleted = 1, sync_state = :syncState, updated_at = :updatedAt
        WHERE id = :noteId
    """)
    suspend fun softDeleteNote(
        noteId: Long,
        syncState: SyncState = SyncState.PENDING_DELETE,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun hardDeleteNote(noteId: Long)

    @Transaction
    @Query("SELECT * FROM notes WHERE id IN (:noteIds) AND is_deleted = 0")
    suspend fun getNotesByIds(noteIds: List<Long>): List<NoteWithTagsRelation>
}
