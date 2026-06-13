package io.github.bengidev.openzone.sidepanel.domain

import io.github.bengidev.openzone.chat.domain.ChatConversation
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Recency-bucketed grouping for the session sidebar. Pinned conversations
 * collapse into a single leading "Pinned" section; the rest fall into
 * recency buckets (Today, Yesterday, Previous 7 Days, Previous 30 Days,
 * Older) keyed off [ChatConversation.updatedAt]. Mirrors iOS
 * [SidePanelSessionSection].
 *
 * Also provides the compact relative-time label shown on each row via
 * [relativeLabel].
 */
data class SidePanelSessionSection(
    val id: String,
    val title: String,
    val conversations: List<ChatConversation>
) {
    companion object {
        /**
         * Group conversations into the pinned section + recency buckets.
         * Input is assumed pinned-first / most-recent-first (the store
         * guarantees this); ordering within each bucket is preserved.
         * Empty buckets are dropped.
         */
        fun grouped(
            conversations: List<ChatConversation>,
            now: Long = System.currentTimeMillis(),
            calendar: Calendar = Calendar.getInstance()
        ): List<SidePanelSessionSection> {
            val sections = mutableListOf<SidePanelSessionSection>()

            val pinned = conversations.filter { it.isPinned }
            if (pinned.isNotEmpty()) {
                sections.add(
                    SidePanelSessionSection(id = "pinned", title = "Pinned", conversations = pinned)
                )
            }

            val buckets = linkedMapOf<RecencyBucket, MutableList<ChatConversation>>()
            for (conversation in conversations) {
                if (conversation.isPinned) continue
                val bucket = RecencyBucket.classify(conversation.updatedAt, now, calendar)
                buckets.getOrPut(bucket) { mutableListOf() }.add(conversation)
            }

            for (bucket in RecencyBucket.entries) {
                val bucketConversations = buckets[bucket] ?: continue
                sections.add(
                    SidePanelSessionSection(
                        id = bucket.id,
                        title = bucket.title,
                        conversations = bucketConversations
                    )
                )
            }

            return sections
        }

        /**
         * Compact relative label: "now", "5m", "3h", "2d", "1w", "4mo", "1y".
         * Mirrors iOS `SidePanelSessionSection.relativeLabel(for:now:)`.
         */
        fun relativeLabel(forDate: Long, now: Long = System.currentTimeMillis()): String {
            val interval = maxOf(0, now - forDate)
            val minute = TimeUnit.MINUTES.toMillis(1)
            val hour = TimeUnit.HOURS.toMillis(1)
            val day = TimeUnit.DAYS.toMillis(1)
            val week = 7 * day
            val month = 30 * day
            val year = 365 * day

            return when {
                interval >= year -> "${interval / year}y"
                interval >= month -> "${interval / month}mo"
                interval >= week -> "${interval / week}w"
                interval >= day -> "${interval / day}d"
                interval >= hour -> "${interval / hour}h"
                interval >= minute -> "${interval / minute}m"
                else -> "now"
            }
        }
    }
}

/**
 * Recency buckets in canonical display order. Mirrors the iOS
 * private `RecencyBucket` enum embedded in `SidePanelSessionSection`.
 */
private enum class RecencyBucket(val id: String, val title: String) {
    TODAY("today", "Today"),
    YESTERDAY("yesterday", "Yesterday"),
    PREVIOUS_7_DAYS("previous7Days", "Previous 7 Days"),
    PREVIOUS_30_DAYS("previous30Days", "Previous 30 Days"),
    OLDER("older", "Older");

    companion object {
        fun classify(dateMillis: Long, nowMillis: Long, calendar: Calendar): RecencyBucket {
            calendar.timeInMillis = nowMillis
            val startOfToday = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            calendar.timeInMillis = dateMillis
            val startOfDate = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val daysAgo = ((startOfToday - startOfDate) / TimeUnit.DAYS.toMillis(1)).toInt()
            return when {
                daysAgo <= 0 -> TODAY
                daysAgo == 1 -> YESTERDAY
                daysAgo <= 7 -> PREVIOUS_7_DAYS
                daysAgo <= 30 -> PREVIOUS_30_DAYS
                else -> OLDER
            }
        }
    }
}
