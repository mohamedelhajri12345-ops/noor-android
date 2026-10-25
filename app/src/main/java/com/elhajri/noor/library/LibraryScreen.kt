package com.elhajri.noor.library

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t
import com.elhajri.noor.theme.NoorThemeState
import com.elhajri.noor.theme.noorGlassCard
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.NoorCardSpec
import com.elhajri.noor.ui.SkeletonCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onOpenReader: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeSection by remember { mutableStateOf(LibrarySection.ADULTS) }
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    var selectedBookForDetail by remember { mutableStateOf<Book?>(null) }

    // Catalog state with timeout & caching support
    var catalogState by remember { mutableStateOf<List<Book>?>(null) }
    var isLoadingCatalog by remember { mutableStateOf(true) }
    var catalogError by remember { mutableStateOf(false) }

    fun loadBooks() {
        isLoadingCatalog = true
        catalogError = false
        scope.launch {
            try {
                val list = LibraryCatalogLoader.loadCatalogAsync(context, timeoutMs = 5000L)
                catalogState = list
                isLoadingCatalog = false
                if (list.isEmpty()) catalogError = true
            } catch (e: Exception) {
                catalogError = true
                isLoadingCatalog = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadBooks()
    }

    val allBooks = catalogState ?: emptyList()

    val readingProgressList = remember(selectedTab, catalogState) { LibraryStore.getAllReadingProgressObjects(context) }
    val downloadedMap = remember(selectedTab, catalogState) { LibraryStore.getDownloadedBooksMap(context) }

    // Filter books by activeSection (KIDS vs ADULTS), selectedCategory, search query, and tab
    val sectionBooks = remember(activeSection, allBooks) {
        allBooks.filter { it.section == activeSection }
    }

    val availableCategories = remember(activeSection) {
        LibraryCategories.getCategoriesForSection(activeSection)
    }

    val filteredBooks = remember(activeSection, selectedCategory, searchQuery, selectedTab, sectionBooks) {
        when (selectedTab) {
            1 -> { // Continue reading
                val progressIds = readingProgressList.map { it.bookId }.toSet()
                sectionBooks.filter { b ->
                    progressIds.contains(b.id) &&
                            (searchQuery.isBlank() || b.title.contains(searchQuery, true) || b.author.contains(searchQuery, true))
                }
            }
            2 -> { // Downloads
                sectionBooks.filter { b ->
                    LibraryStore.isBookAvailableLocally(context, b) &&
                            (searchQuery.isBlank() || b.title.contains(searchQuery, true) || b.author.contains(searchQuery, true))
                }
            }
            else -> { // All books
                sectionBooks.filter { b ->
                    val matchesCat = selectedCategory == "all" || b.category == selectedCategory
                    val matchesQuery = searchQuery.isBlank() ||
                            b.title.contains(searchQuery, ignoreCase = true) ||
                            b.author.contains(searchQuery, ignoreCase = true)
                    matchesCat && matchesQuery
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = t("المكتبة الإسلامية", "Islamic Library"),
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                fontFamily = AmiriFamily
                            )
                            Text(
                                text = t("${sectionBooks.size} كتاباً وقصة معتمدة", "${sectionBooks.size} verified books & stories"),
                                color = GoldSoft.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ChevronRight, contentDescription = t("رجوع", "Back"), tint = Gold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp)
            ) {
                // Compact Top-Level Section Hub Selector (Big Clear Buttons for Kids vs Adults)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Adults Library Button
                    val isAdults = activeSection == LibrarySection.ADULTS
                    Surface(
                        onClick = {
                            activeSection = LibrarySection.ADULTS
                            selectedCategory = "all"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .noorGlassCard(cornerRadius = 14.dp, borderWidth = if (isAdults) 1.5.dp else 0.5.dp),
                        color = if (isAdults) Gold.copy(alpha = 0.18f) else NavyCard.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("📜", fontSize = 20.sp)
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = t("مكتبة الكبار", "Adults Library"),
                                    color = if (isAdults) Gold else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = t("فقه، عقيدة، تفسير", "Fiqh, Aqeedah, Tafsir"),
                                    color = GoldSoft.copy(alpha = 0.7f),
                                    fontSize = 9.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Kids Library Button
                    val isKids = activeSection == LibrarySection.KIDS
                    Surface(
                        onClick = {
                            activeSection = LibrarySection.KIDS
                            selectedCategory = "all"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .noorGlassCard(cornerRadius = 14.dp, borderWidth = if (isKids) 1.5.dp else 0.5.dp),
                        color = if (isKids) Gold.copy(alpha = 0.18f) else NavyCard.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("👶", fontSize = 20.sp)
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = t("مكتبة الصغار", "Kids Library"),
                                    color = if (isKids) Gold else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = t("قصص وأخلاق للناشئة", "Stories & morals for kids"),
                                    color = GoldSoft.copy(alpha = 0.7f),
                                    fontSize = 9.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Compact Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(t("ابحث باسم الكتاب أو المؤلف...", "Search title or author..."), color = GoldSoft.copy(alpha = 0.5f), fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = t("بحث", "Search"), tint = GoldSoft) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavyCard.copy(alpha = 0.8f),
                        unfocusedContainerColor = NavyCard.copy(alpha = 0.6f),
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = NavyLight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Section Tabs (All Books, Continue Reading, Downloads)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = NavyCard.copy(alpha = 0.7f),
                    contentColor = Gold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(t("جميع الكتب (${sectionBooks.size})", "All (${sectionBooks.size})"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(t("متابعة القراءة", "Continue Reading"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(t("التنزيلات", "Downloads"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Category Chips (filtered by active section)
                if (selectedTab == 0 && availableCategories.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableCategories) { cat ->
                            val isSelected = cat.id == selectedCategory
                            val count = LibraryCatalogLoader.getCategoryCount(sectionBooks, cat.id)
                            val catLabel = if (NoorLocale.isEnglish) cat.nameEn else cat.nameAr
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat.id },
                                label = { Text("${cat.iconEmoji} $catLabel ($count)", fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Gold,
                                    selectedLabelColor = Navy,
                                    containerColor = NavyCard,
                                    labelColor = GoldSoft
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // In-Progress Books Quick Strip
                if (selectedTab == 0 && readingProgressList.isNotEmpty() && searchQuery.isBlank()) {
                    val inProgressBooks = remember(readingProgressList, sectionBooks) {
                        readingProgressList.mapNotNull { prog ->
                            sectionBooks.find { it.id == prog.bookId }?.let { b -> Pair(b, prog) }
                        }.take(4)
                    }

                    if (inProgressBooks.isNotEmpty()) {
                        Text(
                            text = t("متابعة القراءة المؤخرة", "Resume Recent Reading"),
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AmiriFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(inProgressBooks, key = { it.first.id }) { (book, prog) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .width(180.dp)
                                        .clickable {
                                            if (book.id.isNotBlank()) {
                                                try {
                                                    onOpenReader(book.id)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                        .noorGlassCard(cornerRadius = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp, 54.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NavyLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (book.coverUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = book.coverUrl,
                                                    contentDescription = book.title,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(Icons.Default.Book, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(book.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(t("ص ${prog.lastPage + 1} / ${prog.totalPages}", "P ${prog.lastPage + 1} / ${prog.totalPages}"), color = GoldSoft, fontSize = 9.5.sp)
                                            Spacer(Modifier.height(3.dp))
                                            LinearProgressIndicator(
                                                progress = { (prog.progressPercentage / 100f).coerceIn(0f, 1f) },
                                                modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                                                color = Gold,
                                                trackColor = NavyLight
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }

                // Loading / Error / Empty States
                if (isLoadingCatalog) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        repeat(4) {
                            SkeletonCard(height = 80.dp)
                        }
                    }
                } else if (catalogError || sectionBooks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldSoft, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (catalogError) t("تعذر تحميل قائمة الكتب، يرجى المحاولة مرة أخرى", "Failed to load catalog. Please retry.")
                                else t("لا توجد كتب متاحة في هذا القسم حالياً", "No books available in this section currently"),
                                color = GoldSoft,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = { loadBooks() },
                                colors = ButtonDefaults.buttonColors(containerColor = Gold)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Navy)
                                Spacer(Modifier.width(6.dp))
                                Text(t("إعادة المحاولة", "Retry"), color = Navy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (filteredBooks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (selectedTab) {
                                1 -> t("لا توجد كتب قيد القراءة حالياً في هذا القسم", "No books in progress in this section")
                                2 -> t("لم تقم بتنزيل أي كتاب في هذا القسم بعد", "No downloaded books in this section yet")
                                else -> t("لا توجد نتائج مطابقة للبحث", "No matching books found")
                            },
                            color = GoldSoft.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    // Books Grid with FULL portrait 2:3 aspect covers (ContentScale.Fit)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredBooks, key = { it.id }) { book ->
                            val isDownloaded = downloadedMap.containsKey(book.id) || LibraryStore.isBookAvailableLocally(context, book)
                            BookCard(
                                book = book,
                                isDownloaded = isDownloaded,
                                onClick = { selectedBookForDetail = book }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedBookForDetail?.let { book ->
        BookDetailSheet(
            book = book,
            onDismiss = { selectedBookForDetail = null },
            onReadClick = { readBook ->
                selectedBookForDetail = null
                if (readBook.id.isNotBlank()) {
                    try {
                        onOpenReader(readBook.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }
}

/**
 * Uniform Book Card Composable with 2:3 aspect ratio portrait cover (ContentScale.Fit, NEVER cropped square).
 */
@Composable
private fun BookCard(
    book: Book,
    isDownloaded: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyCard.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .noorGlassCard(cornerRadius = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // FULL portrait 2:3 aspect ratio cover box (ContentScale.Fit, never cropped)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyLight.copy(alpha = 0.6f))
                    .border(0.5.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = book.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Gold, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = book.title,
                            color = GoldSoft,
                            fontSize = 10.5.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isDownloaded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color(0xCC0D2B1F), CircleShape)
                            .padding(3.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = t("محمل محلياً", "Downloaded"), tint = Gold, modifier = Modifier.size(13.dp))
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Fixed Single/Double-line Title with Ellipsis
            Text(
                text = book.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            // Author
            Text(
                text = book.author.ifBlank { t("غير معروف", "Unknown Author") },
                color = GoldSoft.copy(alpha = 0.75f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // Footer Row (Category badge + page count)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Gold.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    val catLabel = if (NoorLocale.isEnglish) {
                        LibraryCategories.getCategoryNameEn(book.category)
                    } else {
                        LibraryCategories.getCategoryNameAr(book.category)
                    }
                    Text(
                        text = catLabel,
                        color = Gold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${book.pages} " + t("ص", "p"),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.5.sp
                )
            }
        }
    }
}
