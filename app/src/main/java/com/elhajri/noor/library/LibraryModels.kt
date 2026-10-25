package com.elhajri.noor.library

enum class LibrarySection(val id: String, val titleAr: String, val titleEn: String, val subtitleAr: String, val subtitleEn: String, val iconEmoji: String) {
    KIDS("kids", "مكتبة الصغار", "Kids Library", "قصص هادفة، تعليم، وأخلاق للنشء الصاعد", "Educational & moral stories for kids", "👶"),
    ADULTS("adults", "مكتبة الكبار", "Adults Library", "فقه، عقيدة، تفسير، سيرة، ومصنفات إسلامية جامعة", "Fiqh, Aqeedah, Tafsir & detailed Islamic works", "📜")
}

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val category: String, // "prophets_kids", "prophets_adults", "kids_stories", "educational", "fiqh", "religious_stories", "tafsir_seerah", "companions_stories"
    val lang: String,
    val pages: Int,
    val format: String, // "pdf", "epub"
    val license: String,
    val url: String,
    val coverUrl: String,
    val isKids: Boolean,
    val hasImages: Boolean,
    val bundledAsset: String? = null
) {
    val section: LibrarySection
        get() = if (isKids || category in listOf("prophets_kids", "kids_stories", "educational")) {
            LibrarySection.KIDS
        } else {
            LibrarySection.ADULTS
        }
}

data class BookCategory(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val iconEmoji: String,
    val section: LibrarySection? = null // null means applicable to both
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
    
    // Kids Categories
    val PROPHETS_KIDS = BookCategory("prophets_kids", "أنبياء الصغار", "Prophets for Kids", "👶", LibrarySection.KIDS)
    val KIDS_STORIES = BookCategory("kids_stories", "قصص أطفال", "Kids Stories", "🎨", LibrarySection.KIDS)
    val EDUCATIONAL = BookCategory("educational", "كتب تعليمية", "Educational", "🎓", LibrarySection.KIDS)
    
    // Adults Categories
    val FIQH = BookCategory("fiqh", "الفقه والأحكام", "Fiqh & Laws", "📖", LibrarySection.ADULTS)
    val TAFSIR_SEERAH = BookCategory("tafsir_seerah", "التفسير والسيرة", "Tafsir & Seerah", "✨", LibrarySection.ADULTS)
    val PROPHETS_ADULTS = BookCategory("prophets_adults", "قصص الأنبياء", "Prophets Stories", "📜", LibrarySection.ADULTS)
    val COMPANIONS_STORIES = BookCategory("companions_stories", "سير الصحابة", "Companions", "🕌", LibrarySection.ADULTS)
    val RELIGIOUS_STORIES = BookCategory("religious_stories", "مؤلفات إسلامية", "Islamic Works", "🌟", LibrarySection.ADULTS)

    val LIST = listOf(
        ALL,
        PROPHETS_KIDS,
        KIDS_STORIES,
        EDUCATIONAL,
        FIQH,
        TAFSIR_SEERAH,
        PROPHETS_ADULTS,
        COMPANIONS_STORIES,
        RELIGIOUS_STORIES
    )

    fun getCategoriesForSection(section: LibrarySection?): List<BookCategory> {
        if (section == null) return LIST
        return listOf(ALL) + LIST.filter { it.id != "all" && (it.section == section || it.section == null) }
    }

    fun getCategoryNameAr(categoryId: String): String {
        return LIST.find { it.id == categoryId }?.nameAr ?: categoryId
    }

    fun getCategoryNameEn(categoryId: String): String {
        return LIST.find { it.id == categoryId }?.nameEn ?: categoryId
    }
}
