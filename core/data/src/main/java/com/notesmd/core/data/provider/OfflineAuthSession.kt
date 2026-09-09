package com.notesmd.core.data.provider

import com.notesmd.core.domain.provider.AuthSessionProvider
import com.notesmd.core.model.AuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** V1 stub: no authentication — always anonymous. */
class OfflineAuthSession @Inject constructor() : AuthSessionProvider {
    override val currentSession: Flow<AuthSession?> = flowOf(null)
}
