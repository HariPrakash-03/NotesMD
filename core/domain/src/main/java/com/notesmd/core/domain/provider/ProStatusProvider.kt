package com.notesmd.core.domain.provider

import kotlinx.coroutines.flow.Flow

/**
 * Provides the user's Pro subscription status.
 * V1: [NoOpProStatusProvider] returns flowOf(true) — nothing gated.
 * V2: swap in BillingProStatusProvider via Hilt binding.
 */
interface ProStatusProvider {
    val isProEnabled: Flow<Boolean>
}
