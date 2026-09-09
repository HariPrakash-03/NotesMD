package com.notesmd.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.notesmd.core.model.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    // --- TDD Section 6: corrected tag-count query ---
    // Uses COUNT(notes.id) instead of COUNT(ref.note_id) to properly exclude
    // soft-deleted notes — CASCADE never fires on a soft delete.
    @Query("""
        SELECT tags.*, COUNT(notes.id) AS noteCount
        FROM tags
        LEFT JOIN note_tag_cross_ref AS ref ON ref.tag_id = tags.id
        LEFT JOIN notes ON notes.id = ref.note_id AND notes.is_deleted = 0
        WHERE tags.is_deleted = 0
        GROUP BY tags.id
        ORDER BY tags.name ASC
    """)
    fun observeTagsWithCounts(): Flow<List<TagWithCountRelation>>

    // --- Additional CRUD queries for use cases ---

    @Insert
    suspend fun insertTag(tag: TagEntity): Long

    @Query("UPDATE tags SET name = :name, color = :colorHex WHERE id = :tagId")
    suspend fun updateTag(tagId: Long, name: String, colorHex: String)

    @Query("UPDATE tags SET is_deleted = 1, sync_state = :syncState WHERE id = :tagId")
    suspend fun softDeleteTag(
        tagId: Long,
        syncState: SyncState = SyncState.PENDING_DELETE
    )

    @Query("SELECT * FROM tags WHERE id = :tagId AND is_deleted = 0")
    suspend fun getTagById(tagId: Long): TagEntity?

    // --- Tag merge helpers ---

    @Query("SELECT note_id FROM note_tag_cross_ref WHERE tag_id = :tagId")
    suspend fun getNoteIdsForTag(tagId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRefIgnore(ref: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross_ref WHERE tag_id = :tagId")
    suspend fun deleteAllCrossRefsForTag(tagId: Long)

    /**
     * Merges [sourceTagId] into [targetTagId]:
     * 1. Re-tags all notes from source → target (ignoring duplicates)
     * 2. Removes all source cross-refs
     * 3. Soft-deletes the source tag
     */
    @Transaction
    suspend fun mergeTag(sourceTagId: Long, targetTagId: Long) {
        val noteIds = getNoteIdsForTag(sourceTagId)
        noteIds.forEach { noteId ->
            insertNoteTagCrossRefIgnore(NoteTagCrossRef(noteId, targetTagId))
        }
        deleteAllCrossRefsForTag(sourceTagId)
        softDeleteTag(sourceTagId)
    }
}
