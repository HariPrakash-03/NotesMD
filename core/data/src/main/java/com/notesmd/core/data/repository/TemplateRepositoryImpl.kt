package com.notesmd.core.data.repository

import com.notesmd.core.database.TemplateDao
import com.notesmd.core.database.TemplateEntity
import com.notesmd.core.domain.repository.TemplateRepository
import com.notesmd.core.model.FontFamilyOption
import com.notesmd.core.model.ViewerTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao
) : TemplateRepository {

    override fun observeTemplates(): Flow<List<ViewerTemplate>> {
        return templateDao.observeTemplates().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun observeDefaultTemplate(): Flow<ViewerTemplate?> {
        return templateDao.observeDefaultTemplate().map { it?.toModel() }
    }

    override suspend fun saveTemplate(template: ViewerTemplate): Long {
        return templateDao.insert(template.toEntity())
    }

    override suspend fun updateTemplate(template: ViewerTemplate) {
        templateDao.update(template.toEntity())
    }

    override suspend fun deleteTemplate(id: Long) {
        templateDao.delete(id)
    }

    override suspend fun setAsDefault(id: Long) {
        templateDao.setAsDefault(id)
    }

    private fun TemplateEntity.toModel(): ViewerTemplate = ViewerTemplate(
        id = id,
        name = name,
        fontFamily = try { FontFamilyOption.valueOf(fontFamily) } catch (e: Exception) { FontFamilyOption.SANS_SERIF },
        fontScale = fontScale,
        backgroundColor = backgroundColor,
        headingColor = headingColor,
        codeBlockTint = codeBlockTint,
        quoteAccentColor = quoteAccentColor
    )

    private fun ViewerTemplate.toEntity(): TemplateEntity = TemplateEntity(
        id = id,
        name = name,
        fontFamily = fontFamily.name,
        fontScale = fontScale,
        backgroundColor = backgroundColor,
        headingColor = headingColor,
        codeBlockTint = codeBlockTint,
        quoteAccentColor = quoteAccentColor,
        isDefault = false // Or handled separately
    )
}
