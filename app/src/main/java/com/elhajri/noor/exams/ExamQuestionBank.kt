package com.elhajri.noor.exams

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.Random

object ExamQuestionBank {
    private var isLoaded = false
    private val allQuestions = mutableListOf<ExamQuestion>()
    private val easyQuestions = mutableListOf<ExamQuestion>()
    private val mediumQuestions = mutableListOf<ExamQuestion>()
    private val hardQuestions = mutableListOf<ExamQuestion>()

    @Synchronized
    fun loadBank(context: Context) {
        if (isLoaded) return
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("data/examQuestions.json")
            val reader = InputStreamReader(inputStream, "UTF-8")
            val jsonString = reader.readText()
            reader.close()
            inputStream.close()

            val rootObj = JSONObject(jsonString)
            val array = rootObj.optJSONArray("questions") ?: return

            allQuestions.clear()
            easyQuestions.clear()
            mediumQuestions.clear()
            hardQuestions.clear()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val id = item.optInt("id", i + 1)
                val q = item.optString("q", "").trim()
                val optionsArray = item.optJSONArray("options") ?: continue
                val options = mutableListOf<String>()
                for (j in 0 until optionsArray.length()) {
                    options.add(optionsArray.getString(j).trim())
                }
                val correct = item.optInt("correct", 0)
                val category = item.optString("category", "quran").lowercase()
                val difficulty = item.optString("difficulty", "easy").lowercase()
                val explanation = item.optString("explanation", "معلومة إسلامية نافعة.").trim()

                if (q.isNotEmpty() && options.size == 4 && correct in 0..3) {
                    val question = ExamQuestion(
                        id = id,
                        q = q,
                        options = options,
                        correct = correct,
                        category = category,
                        difficulty = difficulty,
                        explanation = explanation
                    )
                    allQuestions.add(question)
                    when (difficulty) {
                        "easy" -> easyQuestions.add(question)
                        "medium" -> mediumQuestions.add(question)
                        "hard" -> hardQuestions.add(question)
                        else -> easyQuestions.add(question)
                    }
                }
            }
            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTotalCount(): Int = allQuestions.size
    fun getEasyCount(): Int = easyQuestions.size
    fun getMediumCount(): Int = mediumQuestions.size
    fun getHardCount(): Int = hardQuestions.size

    /**
     * Draw procedurally seeded 10-20 questions for a given track, difficulty tier, and level.
     */
    fun generateLevelQuestions(
        context: Context,
        track: ExamTrack,
        difficulty: ExamDifficulty,
        levelNumber: Int
    ): List<ExamQuestion> {
        if (!isLoaded) loadBank(context)

        val questionCount = if (track == ExamTrack.KID) 10 else 15
        val pool = when (difficulty) {
            ExamDifficulty.EASY -> if (easyQuestions.isNotEmpty()) easyQuestions else allQuestions
            ExamDifficulty.MEDIUM -> if (mediumQuestions.isNotEmpty()) mediumQuestions else allQuestions
            ExamDifficulty.HARD -> if (hardQuestions.isNotEmpty()) hardQuestions else allQuestions
        }

        if (pool.isEmpty()) return emptyList()

        // Seeded shuffle ensures fresh but deterministic questions per level
        val seed = (track.id + "_" + difficulty.id + "_level_" + levelNumber).hashCode().toLong()
        val random = Random(seed)

        val shuffledPool = pool.toMutableList()
        shuffledPool.shuffle(random)

        return shuffledPool.take(questionCount)
    }
}
