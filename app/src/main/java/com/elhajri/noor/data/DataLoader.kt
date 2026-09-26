package com.elhajri.noor.data

import org.json.JSONObject

data class Surah(val number: Int, val name: String, val englishName: String, val type: String, val ayahs: Int)
data class Reciter(val id: String, val name: String, val servers: List<String>)
data class AthkarCategory(val id: String, val name: String, val subtitle: String, val icon: String, val count: Int)
data class Dhikr(val text: String, val count: Int, val ref: String)
data class TasbihPreset(val text: String, val target: Int)
data class City(val name: String, val lat: Double, val lng: Double, val country: String)
data class QuizQuestion(val q: String, val options: List<String>, val correct: Int, val ref: String)
data class Story(val id: Int, val prophet: String, val title: String, val icon: String)
data class NameOfAllah(val name: String, val meaning: String)

/**
 * Loads NOOR's bundled dataset from assets/data/*.json.
 * All features work offline; no WebView anywhere in this app.
 */
object DataLoader {

    private fun read(context: android.content.Context, file: String): JSONObject =
        JSONObject(context.assets.open("data/$file").bufferedReader().use { it.readText() })

    fun surahs(context: android.content.Context): List<Surah> {
        val arr = read(context, "surahs.json").getJSONArray("surahs")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Surah(o.getInt("number"), o.getString("name"), o.optString("englishName"), o.getString("type"), o.getInt("ayahs"))
        }
    }

    fun reciters(context: android.content.Context): List<Reciter> {
        val arr = read(context, "surahs.json").getJSONArray("reciters")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Reciter(o.getString("id"), o.getString("name"), List(o.getJSONArray("servers").length()) { j -> o.getJSONArray("servers").getString(j) })
        }
    }

    fun athkarCategories(context: android.content.Context): List<AthkarCategory> {
        val arr = read(context, "athkar.json").getJSONArray("athkarCategories")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            AthkarCategory(o.getString("id"), o.getString("name"), o.getString("subtitle"), o.optString("icon"), o.getInt("count"))
        }
    }

    fun athkar(context: android.content.Context, category: String): List<Dhikr> {
        val arr = read(context, "athkar.json").getJSONObject("athkar").getJSONArray(category)
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Dhikr(o.getString("text"), o.optInt("count", 1), o.optString("ref"))
        }
    }

    fun tasbihPresets(context: android.content.Context): List<TasbihPreset> {
        val arr = read(context, "athkar.json").getJSONArray("tasbihPresets")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            TasbihPreset(o.getString("text"), o.getInt("target"))
        }
    }

    fun cities(context: android.content.Context): List<City> {
        val arr = read(context, "cities.json").getJSONArray("cities")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            City(o.getString("name"), o.getDouble("lat"), o.getDouble("lng"), o.optString("country"))
        }
    }

    fun quizQuestions(context: android.content.Context): List<QuizQuestion> {
        val arr = read(context, "quizQuestions.json").getJSONArray("quizQuestions")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            QuizQuestion(
                o.getString("q"),
                List(o.getJSONArray("options").length()) { j -> o.getJSONArray("options").getString(j) },
                o.getInt("correct"),
                o.optString("ref")
            )
        }
    }

    fun stories(context: android.content.Context): List<Story> {
        val arr = read(context, "stories.json").getJSONArray("stories")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Story(o.getInt("id"), o.optString("prophet"), o.getString("title"), o.optString("icon"))
        }
    }

    fun namesOfAllah(context: android.content.Context): List<NameOfAllah> {
        val arr = read(context, "namesOfAllah.json").getJSONArray("namesOfAllah")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            NameOfAllah(o.getString("name"), o.optString("meaning"))
        }
    }
}
