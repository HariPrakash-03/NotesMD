package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.TagRepository
import javax.inject.Inject

/** Clear-and-replace cross-refs for a given note. Called independently of content saves. */
class SyncNoteTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(noteId: Long, tagIds: List<Long>) {
        tagRepository.syncNoteTags(noteId, tagIds)
    }
}
