package com.elhajri.noor.library

import android.content.Context
import org.json.JSONArray
import java.security.MessageDigest

object LibraryCatalogLoader {

    @Volatile
    private var cachedBooks: List<Book>? = null

    fun loadCatalog(context: Context): List<Book> {
        cachedBooks?.let { return it }

        synchronized(this) {
            cachedBooks?.let { return it }

            val list = mutableListOf<Book>()
            try {
                val jsonStr = context.assets.open("data/library_catalog.json").bufferedReader().use { it.readText() }
                val array = JSONArray(jsonStr)

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)

                    val title = obj.optString("title", "بدون عنوان")
                    val author = obj.optString("author", "غير معروف")
                    val category = obj.optString("category", "religious_stories")
                    val lang = obj.optString("lang", "ar")
                    val pages = obj.optInt("pages", 1)
                    val format = obj.optString("format", "pdf")
                    val license = obj.optString("license", "Public Domain")
                    val url = obj.optString("url", "")
                    val coverUrl = obj.optString("cover_url", "")
                    val kids = obj.optBoolean("kids", false)
                    val images = obj.optBoolean("images", false)
                    val bundledAsset = if (obj.has("bundled_asset")) obj.getString("bundled_asset") else null

                    val id = generateBookId(url, title, i)

                    list.add(
                        Book(
                            id = id,
                            title = title,
                            author = author,
                            category = category,
                            lang = lang,
                            pages = pages,
                            format = format,
                            license = license,
                            url = url,
                            coverUrl = coverUrl,
                            isKids = kids,
                            hasImages = images,
                            bundledAsset = bundledAsset
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            cachedBooks = list
            return list
        }
    }

    private fun generateBookId(url: String, title: String, index: Int): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val hashBytes = md.digest("$url-$title".toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }.take(12)
        } catch (_: Exception) {
            "book_$index"
        }
    }

    fun getCategoryCount(books: List<Book>, categoryId: String, section: LibrarySection? = null): Int {
        val filteredBySection = if (section == null) books else books.filter { it.section == section }
        if (categoryId == "all") return filteredBySection.size
        return filteredBySection.count { it.category == categoryId }
    }
}
