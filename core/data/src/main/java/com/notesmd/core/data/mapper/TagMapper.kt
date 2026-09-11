package com.notesmd.core.data.mapper

import com.notesmd.core.database.TagEntity
import com.notesmd.core.model.Tag

/** Maps Room [TagEntity] to domain [Tag]. */
object TagMapper {
    fun toDomain(entity: TagEntity): Tag = Tag(
        id = entity.id,
        name = entity.name,
        colorHex = entity.color
    )
}
