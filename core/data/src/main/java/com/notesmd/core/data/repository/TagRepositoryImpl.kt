package com.notesmd.core.data.repository

import com.notesmd.core.data.mapper.TagMapper
import com.notesmd.core.database.NoteDao
import com.notesmd.core.database.TagDao
import com.notesmd.core.database.TagEntity
import com.notesmd.core.domain.repository.TagRepository
import com.notesmd.core.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
    private val noteDao: NoteDao
) : TagRepository {

    override fun observeTagsWithCounts(): Flow<List<Pair<Tag, Int>>> =
        tagDao.observeTagsWithCounts().map { relations ->
            relations.map { TagMapper.toDomain(it.tag) to it.noteCount }
        }

    override suspend fun createTag(name: String, colorHex: String): Long =
        tagDao.insertTag(TagEntity(name = name, color = colorHex))

    override suspend fun deleteTag(tagId: Long) {
        tagDao.deleteAllCrossRefsForTag(tagId)
        tagDao.softDeleteTag(tagId)
    }

    override suspend fun updateTag(tagId: Long, name: String, colorHex: String) =
        tagDao.updateTag(tagId, name, colorHex)

    override suspend fun mergeTag(sourceId: Long, targetId: Long) =
        tagDao.mergeTag(sourceId, targetId)

    override suspend fun syncNoteTags(noteId: Long, tagIds: List<Long>) =
        noteDao.syncNoteTags(noteId, tagIds)
}
