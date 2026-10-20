package com.elhajri.noor.library

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val category: String, // "prophets_kids", "prophets_adults", "kids_stories", "educational", "fiqh", "religious_stories", "tafsir_seerah"
    val lang: String,
    val pages: Int,
    val format: String, // "pdf", "epub"
    val license: String,
    val url: String,
    val coverUrl: String,
    val isKids: Boolean,
    val hasImages: Boolean,
    val bundledAsset: String? = null
)

data class BookCategory(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val iconEmoji: String
)

data class ReadingProgress(
    val bookId: String,
    val lastPage: Int,
    val totalPages: Int,
    val lastReadTimestamp: Long,
    val progressPercentage: Int
)

data class Bookmark(
    val bookId: String,
    val page: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

object LibraryCategories {
    val ALL = BookCategory("all", "الكل", "All", "📚")
    val PROPHETS_KIDS = BookCategory("prophets_kids", "قصص أنبياء صغار", "Prophets for Kids", "👶")
    val PROPHETS_ADULTS = BookCategory("prophets_adults", "قصص أنبياء كبار", "Prophets Stories", "📜")
    val KIDS_STORIES = BookCategory("kids_stories", "قصص أطفال", "Kids Stories", "🎨")
    val EDUCATIONAL = BookCategory("educational", "تعليمية", "Educational", "🎓")
    val FIQH = BookCategory("fiqh", "فقه", "Fiqh Basics", "📖")
    val RELIGIOUS_STORIES = BookCategory("religious_stories", "قصص دينية", "Islamic Stories", "🌟")
    val TAFSIR_SEERAH = BookCategory("tafsir_seerah", "تفسير وسيرة", "Tafsir & Seerah", "✨")

    val LIST = listOf(
        ALL,
        PROPHETS_KIDS,
        PROPHETS_ADULTS,
        KIDS_STORIES,
        EDUCATIONAL,
        FIQH,
        RELIGIOUS_STORIES,
        TAFSIR_SEERAH
    )

    fun getCategoryNameAr(categoryId: String): String {
        return LIST.find { it.id == categoryId }?.nameAr ?: categoryId
    }
}
