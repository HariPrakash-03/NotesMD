package com.notesmd.core.domain.repository

import com.notesmd.core.model.ViewerTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun observeTemplates(): Flow<List<ViewerTemplate>>
    fun observeDefaultTemplate(): Flow<ViewerTemplate?>
    suspend fun saveTemplate(template: ViewerTemplate): Long
    suspend fun updateTemplate(template: ViewerTemplate)
    suspend fun deleteTemplate(id: Long)
    suspend fun setAsDefault(id: Long)
}
