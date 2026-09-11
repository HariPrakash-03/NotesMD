package com.notesmd.core.domain.provider

/**
 * Drives bidirectional cloud synchronization.
 * V1: [NoOpSyncEngine] — all methods are no-ops.
 * V2: swap in CloudSyncEngine that pushes/pulls via REST/Firestore.
 */
interface SyncEngine {
    suspend fun pushPendingChanges()
    suspend fun pullRemoteChanges()
}
