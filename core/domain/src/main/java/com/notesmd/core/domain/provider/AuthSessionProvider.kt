package com.notesmd.core.domain.provider

import com.notesmd.core.model.AuthSession
import kotlinx.coroutines.flow.Flow

/**
 * Provides the current authentication session.
 * V1: [OfflineAuthSession] returns flowOf(null) — anonymous usage.
 * V2: swap in a real JWT/OAuth provider via Hilt binding.
 */
interface AuthSessionProvider {
    val currentSession: Flow<AuthSession?>
}
