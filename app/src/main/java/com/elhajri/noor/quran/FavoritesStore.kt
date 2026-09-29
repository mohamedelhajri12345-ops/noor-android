package com.elhajri.noor.quran

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

object FavoritesStore {
    private const val PREFS_NAME = "nur_quran_favorites_prefs"
    private const val KEY_FAVORITES = "favorite_surahs"

    private val _favorites = MutableStateFlow<List<Int>>(emptyList())
    val favorites: StateFlow<List<Int>> = _favorites.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            _favorites.value = loadFavorites(context)
            isInitialized = true
        }
    }

    private fun loadFavorites(context: Context): List<Int> {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = sp.getString(KEY_FAVORITES, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(raw)
            List(jsonArray.length()) { i -> jsonArray.getInt(i) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isFavorite(context: Context, surahNumber: Int): Boolean {
        init(context)
        return _favorites.value.contains(surahNumber)
    }

    fun toggleFavorite(context: Context, surahNumber: Int) {
        init(context)
        val current = _favorites.value.toMutableList()
        if (current.contains(surahNumber)) {
            current.remove(surahNumber)
        } else {
            current.add(surahNumber)
        }
        _favorites.value = current

        val jsonArray = JSONArray()
        current.forEach { jsonArray.put(it) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FAVORITES, jsonArray.toString())
            .apply()
    }
}
