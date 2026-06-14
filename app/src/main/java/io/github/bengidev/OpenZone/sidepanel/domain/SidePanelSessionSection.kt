package io.github.bengidev.openzone.sidepanel.domain

import io.github.bengidev.openzone.chat.domain.ChatConversation
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Recency-bucketed grouping for the session sidebar. Pinned conversations collapse into a single
 * leading "Pinned" section; named group folders follow; the rest fall into recency buckets keyed
 * off [ChatConversation.updatedAt]. Mirrors iOS `SidePanelSessionSection`.
 */
data class SidePanelSessionSection(
        val id: String,
        val title: String,
        val conversations: List<ChatConversation>,
        val isGroupSection: Boolean = false,
        val isGroupExpanded: Boolean = true
) {
 companion object {
  fun grouped(
          conversations: List<ChatConversation>,
          now: Long = System.currentTimeMillis(),
          calendar: Calendar = Calendar.getInstance(),
          expandedGroups: Set<String> = emptySet(),
          forceExpandGroups: Boolean = false
  ): List<SidePanelSessionSection> {
   val sections = mutableListOf<SidePanelSessionSection>()

   val pinned = conversations.filter { it.isPinned }
   if (pinned.isNotEmpty()) {
    sections.add(SidePanelSessionSection(id = "pinned", title = "Pinned", conversations = pinned))
   }

   val groupBuckets = linkedMapOf<String, MutableList<ChatConversation>>()
   for (conversation in conversations) {
    if (conversation.isPinned) continue
    val groupName = conversation.groupName ?: continue
    groupBuckets.getOrPut(groupName) { mutableListOf() }.add(conversation)
   }
   for (groupName in groupBuckets.keys.sorted()) {
    val groupConversations = groupBuckets[groupName].orEmpty()
    val isExpanded = forceExpandGroups || expandedGroups.contains(groupName)
    sections.add(
            SidePanelSessionSection(
                    id = "group:$groupName",
                    title = groupName,
                    conversations = if (isExpanded) groupConversations else emptyList(),
                    isGroupSection = true,
                    isGroupExpanded = isExpanded
            )
    )
   }

   val buckets = linkedMapOf<RecencyBucket, MutableList<ChatConversation>>()
   for (conversation in conversations) {
    if (conversation.isPinned || conversation.groupName != null) continue
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

  fun deduplicatedPinnedFirst(conversations: List<ChatConversation>): List<ChatConversation> {
   val sorted =
           conversations.sortedWith(
                   compareByDescending<ChatConversation> { it.isPinned }.thenByDescending {
                    it.updatedAt
                   }
           )
   val seen = mutableSetOf<String>()
   return sorted.filter { seen.add(it.id) }
  }
 }
}

private enum class RecencyBucket(val id: String, val title: String) {
 TODAY("today", "Today"),
 YESTERDAY("yesterday", "Yesterday"),
 PREVIOUS_7_DAYS("previous7Days", "Previous 7 Days"),
 PREVIOUS_30_DAYS("previous30Days", "Previous 30 Days"),
 OLDER("older", "Older");

 companion object {
  fun classify(dateMillis: Long, nowMillis: Long, calendar: Calendar): RecencyBucket {
   calendar.timeInMillis = nowMillis
   val startOfToday =
           calendar
                   .apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                   }
                   .timeInMillis

   calendar.timeInMillis = dateMillis
   val startOfDate =
           calendar
                   .apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                   }
                   .timeInMillis

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
