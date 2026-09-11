package com.notesmd.core.model

/**
 * Tracks the synchronization state of an entity relative to the cloud backend.
 * V1 uses LOCAL_ONLY exclusively. V2 will use all states for conflict resolution.
 */
enum class SyncState {
    LOCAL_ONLY,
    SYNCED,
    PENDING_UPDATE,
    PENDING_DELETE
}
