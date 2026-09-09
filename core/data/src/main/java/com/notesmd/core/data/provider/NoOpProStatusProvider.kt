package com.notesmd.core.data.provider

import com.notesmd.core.domain.provider.ProStatusProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** V1 stub: nothing gated — always returns true. */
class NoOpProStatusProvider @Inject constructor() : ProStatusProvider {
    override val isProEnabled: Flow<Boolean> = flowOf(true)
}
