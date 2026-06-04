package io.github.bengidev.openzone.onboarding.domain

/**
 * Queue item shown in the Prompt Queue visual demo.
 * Represents a task in the execution queue with its current status.
 */
data class OnboardingQueueItem(
    val title: String,
    val detail: String,
    val status: Status
) {
    val id: String get() = title

    enum class Status {
        RUNNING,
        NEXT,
        QUEUED,
        READY
    }

    companion object {
        val samples: List<OnboardingQueueItem> = listOf(
            OnboardingQueueItem(
                title = "Map onboarding state",
                detail = "Engine already owns current page",
                status = Status.RUNNING
            ),
            OnboardingQueueItem(
                title = "Generate interface cards",
                detail = "No vertical scroll, compact content",
                status = Status.NEXT
            ),
            OnboardingQueueItem(
                title = "Persist completion",
                detail = "Storage writes local progress",
                status = Status.QUEUED
            ),
            OnboardingQueueItem(
                title = "Review model budget",
                detail = "Reasoning slider updates the run",
                status = Status.READY
            )
        )
    }
}
