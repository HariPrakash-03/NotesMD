package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.TagRepository
import javax.inject.Inject

/** Creates a tag with auto-trimmed, lowercased name. Returns generated ID. */
class CreateTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(name: String, colorHex: String): Long {
        return tagRepository.createTag(name.trim().lowercase(), colorHex)
    }
}
