package io.github.bengidev.openzone.onboarding.infrastructure

/**
 * Repository interface for onboarding persistence.
 * Abstracts storage implementation to enable testing and future changes.
 */
interface OnboardingRepository {
    /**
     * Checks if onboarding has been completed.
     * @return true if user completed onboarding, false otherwise
     */
    suspend fun isOnboardingCompleted(): Boolean

    /**
     * Marks onboarding as completed.
     * Persists completion state and timestamp.
     */
    suspend fun completeOnboarding()
}
