package com.notesmd.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.notesmd.core.model.SyncState

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "remote_id") val remoteId: String? = null,
    val version: Int = 1,
    @ColumnInfo(name = "sync_state") val syncState: SyncState = SyncState.SYNCED,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "font_family") val fontFamily: String,
    @ColumnInfo(name = "font_scale") val fontScale: Float,
    @ColumnInfo(name = "background_color") val backgroundColor: String,
    @ColumnInfo(name = "heading_color") val headingColor: String,
    @ColumnInfo(name = "code_block_tint") val codeBlockTint: String,
    @ColumnInfo(name = "quote_accent_color") val quoteAccentColor: String,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false
)
