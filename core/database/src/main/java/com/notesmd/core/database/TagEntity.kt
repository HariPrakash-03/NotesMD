package com.notesmd.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.notesmd.core.model.SyncState

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "remote_id") val remoteId: String? = null,
    val name: String,
    val color: String,
    val version: Long = 1L,
    @ColumnInfo(name = "sync_state") val syncState: SyncState = SyncState.LOCAL_ONLY,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
