package com.notesmd.core.domain.usecase

import com.notesmd.core.domain.repository.TagRepository
import com.notesmd.core.model.Tag
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Thin wrapper exposing the tags-with-counts flow from the repository. */
class ObserveTagsWithCountsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    operator fun invoke(): Flow<List<Pair<Tag, Int>>> = tagRepository.observeTagsWithCounts()
}
