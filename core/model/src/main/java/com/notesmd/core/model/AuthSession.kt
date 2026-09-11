package com.notesmd.core.model

/**
 * Represents an authenticated user session.
 * V1: always null (anonymous). V2: populated after JWT/OAuth login.
 */
data class AuthSession(
    val userId: String,
    val token: String,
    val displayName: String? = null
)
