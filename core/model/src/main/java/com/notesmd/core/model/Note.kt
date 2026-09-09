package com.notesmd.core.model

data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val tags: List<Tag> = emptyList(),
    val remoteId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
