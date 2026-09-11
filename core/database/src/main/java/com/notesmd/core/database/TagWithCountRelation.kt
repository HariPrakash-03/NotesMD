package com.notesmd.core.database

import androidx.room.Embedded

/**
 * Projection class for the tag-with-note-count query.
 * Maps to: SELECT tags.*, COUNT(notes.id) AS noteCount ...
 */
data class TagWithCountRelation(
    @Embedded val tag: TagEntity,
    val noteCount: Int
)
