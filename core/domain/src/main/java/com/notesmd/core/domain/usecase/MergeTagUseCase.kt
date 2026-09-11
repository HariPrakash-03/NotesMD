package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.TagRepository
import javax.inject.Inject

/** Merges sourceTag into targetTag: re-tags all notes, deletes source. */
class MergeTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(sourceTagId: Long, targetTagId: Long) {
        tagRepository.mergeTag(sourceTagId, targetTagId)
    }
}
