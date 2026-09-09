package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.TagRepository
import javax.inject.Inject

/** Updates a tag's name and/or color. */
class UpdateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tagId: Long, name: String, colorHex: String) {
        tagRepository.updateTag(tagId, name.trim().lowercase(), colorHex)
    }
}
