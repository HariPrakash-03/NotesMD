package com.notesmd.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates WHERE is_deleted = 0 ORDER BY name ASC")
    fun observeTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE is_deleted = 0 AND is_default = 1 LIMIT 1")
    fun observeDefaultTemplate(): Flow<TemplateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: TemplateEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<TemplateEntity>)

    @Update
    suspend fun update(template: TemplateEntity)

    @Query("UPDATE templates SET is_deleted = 1, sync_state = 'PENDING_DELETE' WHERE id = :id")
    suspend fun delete(id: Long)

    @Transaction
    suspend fun setAsDefault(templateId: Long) {
        clearDefaultFlag()
        setDefaultFlag(templateId)
    }

    @Query("UPDATE templates SET is_default = 0")
    suspend fun clearDefaultFlag()

    @Query("UPDATE templates SET is_default = 1 WHERE id = :templateId")
    suspend fun setDefaultFlag(templateId: Long)
}
