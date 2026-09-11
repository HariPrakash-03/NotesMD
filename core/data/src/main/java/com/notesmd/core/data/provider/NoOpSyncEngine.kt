package com.notesmd.core.data.provider

import com.notesmd.core.domain.provider.SyncEngine
import javax.inject.Inject

/** V1 stub: no cloud sync — all methods are no-ops. */
class NoOpSyncEngine @Inject constructor() : SyncEngine {
    override suspend fun pushPendingChanges() { /* V2 */ }
    override suspend fun pullRemoteChanges() { /* V2 */ }
}
