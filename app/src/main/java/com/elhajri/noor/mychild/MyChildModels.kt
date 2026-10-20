package com.elhajri.noor.mychild

import androidx.annotation.Keep

@Keep
data class ChildProfile(
    val id: String,
    val name: String,
    val age: Int,
    val avatar: String = "🦁", // Emoji or avatar identifier
    val bedtimeHour: Int = 20, // 8:00 PM
    val bedtimeMinute: Int = 0,
    val bedtimeNotificationEnabled: Boolean = true,
    val totalStarsEarned: Int = 0,
    val screenTimeMinutesToday: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Keep
data class ChildMemorization(
    val surahNumber: Int,
    val surahNameAr: String,
    val surahNameEn: String,
    val versesCount: Int,
    val starsCount: Int = 0 // 0 = not started, 1 = listening/reading, 2 = reviewing, 3 = memorized fully
)

@Keep
data class ParentReward(
    val id: String,
    val titleAr: String,
    val starCost: Int,
    val iconEmoji: String = "🎁",
    val isRedeemed: Boolean = false,
    val redeemedAt: Long? = null
)

@Keep
data class BedtimeStoryItem(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: String = "prophets_kids",
    val coverUrl: String? = null,
    val isBundled: Boolean = true
)

@Keep
data class FamilyQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = "",
    val category: String = "عام"
)

@Keep
data class FamilyQuizResult(
    val childId: String,
    val childName: String,
    val parentScore: Int,
    val childScore: Int,
    val winnerName: String,
    val totalRounds: Int = 5,
    val dateMillis: Long = System.currentTimeMillis()
)
