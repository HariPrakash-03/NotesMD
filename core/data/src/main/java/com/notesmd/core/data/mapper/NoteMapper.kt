package com.notesmd.core.data.mapper

import com.notesmd.core.database.NoteWithTagsRelation
import com.notesmd.core.model.Note

/** Maps Room [NoteWithTagsRelation] to domain [Note]. */
object NoteMapper {
    fun NoteWithTagsRelation.toDomain(): Note = Note(
        id = note.id,
        title = note.title,
        content = note.content,
        tags = tags.map { TagMapper.toDomain(it) },
        remoteId = note.remoteId,
        createdAt = note.createdAt,
        updatedAt = note.updatedAt
    )
}
