package com.notesmd.core.domain.repository

import com.notesmd.core.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeTagsWithCounts(): Flow<List<Pair<Tag, Int>>>
    suspend fun createTag(name: String, colorHex: String): Long
    suspend fun deleteTag(tagId: Long)
    suspend fun updateTag(tagId: Long, name: String, colorHex: String)
    suspend fun mergeTag(sourceId: Long, targetId: Long)
    suspend fun syncNoteTags(noteId: Long, tagIds: List<Long>)
}
