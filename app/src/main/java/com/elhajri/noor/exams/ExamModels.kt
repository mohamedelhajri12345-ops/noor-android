package com.elhajri.noor.exams

import androidx.annotation.Keep

@Keep
enum class ExamTrack(val id: String, val titleAr: String, val titleEn: String) {
    KID("kid", "طفل", "Kid"),
    ADULT("adult", "كبير", "Adult")
}

@Keep
enum class ExamDifficulty(val id: String, val titleAr: String, val titleEn: String, val totalLevels: Int, val certTitleKid: String, val certTitleAdult: String) {
    EASY("easy", "سهل", "Easy", 1000, "شهادة الجدارة (المستوى المبتدئ)", "شهادة التمكن (المستوى المبتدئ)"),
    MEDIUM("medium", "متوسط", "Medium", 1000, "شهادة الجدارة (المستوى المتوسط)", "شهادة التمكن (المستوى المتوسط)"),
    HARD("hard", "صعب", "Hard", 2000, "شهادة الجدارة الفائقة", "شهادة التمكن العالية")
}

@Keep
data class ExamQuestion(
    val id: Int,
    val q: String,
    val options: List<String>,
    val correct: Int,
    val category: String,
    val difficulty: String,
    val explanation: String
)

@Keep
data class ExamQuestionAnswer(
    val question: ExamQuestion,
    val selectedIndex: Int?,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int
)

@Keep
data class ExamResult(
    val track: ExamTrack,
    val difficulty: ExamDifficulty,
    val levelNumber: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val percentage: Int,
    val passed: Boolean,
    val starsEarned: Int,
    val timeTakenSeconds: Int,
    val answers: List<ExamQuestionAnswer>,
    val dateEpochMillis: Long = System.currentTimeMillis()
)

@Keep
data class ExamProgress(
    val unlockedLevel: Int = 1,
    val starsMap: Map<Int, Int> = emptyMap(),
    val highestScoreMap: Map<Int, Int> = emptyMap()
)
