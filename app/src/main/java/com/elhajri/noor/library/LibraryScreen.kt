package com.elhajri.noor.library

import android.widget.Toast
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
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.noorGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onOpenReader: (String) -> Unit
) {
    val context = LocalContext.current
    val allBooks = remember { LibraryCatalogLoader.loadCatalog(context) }

    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    var selectedBookForDetail by remember { mutableStateOf<Book?>(null) }

    val readingProgressList = remember(selectedTab) { LibraryStore.getAllReadingProgressObjects(context) }
    val downloadedMap = remember(selectedTab) { LibraryStore.getDownloadedBooksMap(context) }

    val filteredBooks = remember(selectedCategory, searchQuery, selectedTab, allBooks) {
        when (selectedTab) {
            1 -> {
                val progressIds = readingProgressList.map { it.bookId }.toSet()
                allBooks.filter { b ->
                    progressIds.contains(b.id) &&
                            (searchQuery.isBlank() || b.title.contains(searchQuery, true) || b.author.contains(searchQuery, true))
                }
            }
            2 -> {
                allBooks.filter { b ->
                    LibraryStore.isBookAvailableLocally(context, b) &&
                            (searchQuery.isBlank() || b.title.contains(searchQuery, true) || b.author.contains(searchQuery, true))
                }
            }
            else -> {
                allBooks.filter { b ->
                    val matchesCat = selectedCategory == "all" || b.category == selectedCategory
                    val matchesQuery = searchQuery.isBlank() ||
                            b.title.contains(searchQuery, ignoreCase = true) ||
                            b.author.contains(searchQuery, ignoreCase = true)
                    matchesCat && matchesQuery
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t("المكتبة الإسلامية", "Islamic Library"), color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = AmiriFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronRight, contentDescription = t("رجوع", "Back"), tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
            )
        },
        containerColor = Color(0xFF070B14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            com.elhajri.noor.ui.NoorSectionTitle(
                title = t("المكتبة", "Library"),
                subtitle = t("٥٥٥ كتاب وقصة إسلامية معتمدة — بدون إعلانات", "555 verified Islamic books & stories — Ad free"),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(t("ابحث باسم الكتاب أو المؤلف...", "Search title or author..."), color = GoldSoft.copy(alpha = 0.5f), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = t("بحث", "Search"), tint = GoldSoft) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NavyCard,
                    unfocusedContainerColor = NavyCard,
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = NavyLight,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavyCard,
                contentColor = Gold,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(t("جميع الكتب (${allBooks.size})", "All Books (${allBooks.size})"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(t("متابعة القراءة", "Continue Reading"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(t("التنزيلات", "Downloads"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(LibraryCategories.LIST) { cat ->
                        val isSelected = cat.id == selectedCategory
                        val count = LibraryCatalogLoader.getCategoryCount(allBooks, cat.id)
                        val catLabel = if (NoorLocale.isEnglish) cat.nameEn else cat.nameAr
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat.id },
                            label = { Text("${cat.iconEmoji} $catLabel ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold,
                                selectedLabelColor = Navy,
                                containerColor = NavyCard,
                                labelColor = GoldSoft
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (selectedTab == 0 && readingProgressList.isNotEmpty() && searchQuery.isBlank()) {
                val inProgressBooks = remember(readingProgressList, allBooks) {
                    readingProgressList.mapNotNull { prog ->
                        allBooks.find { it.id == prog.bookId }?.let { b -> Pair(b, prog) }
                    }.take(5)
                }

                if (inProgressBooks.isNotEmpty()) {
                    Text(t("استكمال القراءة", "Resume Reading"), color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(inProgressBooks) { (book, prog) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavyCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(200.dp)
                                    .clickable { onOpenReader(book.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(45.dp, 60.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NavyLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (book.coverUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = book.coverUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(Icons.Default.Book, contentDescription = null, tint = Gold)
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(book.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(t("صفحة ${prog.lastPage} من ${prog.totalPages}", "Page ${prog.lastPage} of ${prog.totalPages}"), color = GoldSoft, fontSize = 10.sp)
                                        Spacer(Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { prog.progressPercentage / 100f },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                            color = Gold,
                                            trackColor = NavyLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            if (filteredBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedTab) {
                            1 -> t("لا توجد كتب قيد القراءة حالياً", "No books in progress")
                            2 -> t("لم تقم بتنزيل أي كتاب بعد", "No downloaded books yet")
                            else -> t("لا توجد نتائج مطابقة للبحث", "No matching books found")
                        },
                        color = GoldSoft.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
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

    selectedBookForDetail?.let { book ->
        BookDetailSheet(
            book = book,
            onDismiss = { selectedBookForDetail = null },
            onReadClick = { readBook ->
                selectedBookForDetail = null
                onOpenReader(readBook.id)
            }
        )
    }
}

@Composable
private fun BookCard(
    book: Book,
    isDownloaded: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .noorGlassCard(cornerRadius = 14.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyLight),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Gold, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(book.title, color = GoldSoft, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }

                if (isDownloaded) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color(0xCC0D2B1F), CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = t("محمل محلياً", "Downloaded"), tint = Gold, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = book.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(36.dp)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = book.author.ifBlank { t("غير معروف", "Unknown Author") },
                color = GoldSoft.copy(alpha = 0.7f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Gold.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    val catLabel = if (NoorLocale.isEnglish) LibraryCategories.LIST.find { it.id == book.category }?.nameEn ?: book.category else LibraryCategories.getCategoryNameAr(book.category)
                    Text(
                        text = catLabel,
                        color = Gold,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${book.pages} " + t("ص", "p"),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
