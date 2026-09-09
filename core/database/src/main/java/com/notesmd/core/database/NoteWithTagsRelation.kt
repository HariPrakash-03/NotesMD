package com.notesmd.core.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Projection class for loading a NoteEntity with its associated TagEntities.
 * Room resolves the many-to-many relationship through [NoteTagCrossRef].
 */
data class NoteWithTagsRelation(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            NoteTagCrossRef::class,
            parentColumn = "note_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>
)
