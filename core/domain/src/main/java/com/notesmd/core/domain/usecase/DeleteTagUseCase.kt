package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.TagRepository
import javax.inject.Inject

/** Soft-deletes a tag and its cross-ref associations. */
class DeleteTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tagId: Long) {
        tagRepository.deleteTag(tagId)
    }
}
