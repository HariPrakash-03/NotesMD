package com.notesmd.core.database

import androidx.room.TypeConverter
import com.notesmd.core.model.SyncState

/** Room TypeConverter for [SyncState] enum ↔ String persistence. */
class SyncStateConverter {
    @TypeConverter
    fun fromSyncState(state: SyncState): String = state.name

    @TypeConverter
    fun toSyncState(value: String): SyncState = SyncState.valueOf(value)
}
