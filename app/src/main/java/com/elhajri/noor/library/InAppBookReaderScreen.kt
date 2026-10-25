package com.elhajri.noor.library

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.elhajri.noor.settings.t
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InAppBookReaderScreen(
    bookId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val books = remember { LibraryCatalogLoader.loadCatalog(context) }
    val book = remember(bookId, books) { books.find { it.id == bookId } }

    if (bookId.isBlank() || book == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .themeScreenBackground(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldSoft, modifier = Modifier.size(50.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = t("لم يتم العثور على الكتاب المطلوب", "Book not found"),
                    color = Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text(t("رجوع للمكتبة", "Back to Library"), color = Navy, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    var localFile by remember(book.id) { mutableStateOf<File?>(LibraryStore.getLocalFile(context, book)) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    // Auto-download if missing locally
    LaunchedEffect(book.id) {
        if (localFile == null || !localFile!!.exists() || localFile!!.length() == 0L) {
            isDownloading = true
            downloadError = null
            BookDownloader.downloadBook(context, book) { success, msg ->
                isDownloading = false
                if (success) {
                    localFile = LibraryStore.getLocalFile(context, book)
                } else {
                    downloadError = msg ?: t("فشل تحميل الكتاب", "Failed to load book")
                }
            }
        }
    }

    // EPUB format check
    if (book.format.equals("epub", ignoreCase = true)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .themeScreenBackground()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Gold, modifier = Modifier.size(60.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = book.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = AmiriFamily
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = t("تنسيق EPUB يتطلب قارئ مستندات خارجي. يمكنك فتح الملف عبر القارئ المفضل لديك.", "EPUB format requires an external reader app. Open in your preferred app."),
                    color = GoldSoft,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val file = LibraryStore.getLocalFile(context, book)
                        if (file != null && file.exists()) {
                            try {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/epub+zip")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, t("لم يتم العثور على تطبيق لفتح ملفات EPUB", "No app found to open EPUB files"), Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, t("تعذّر فتح القارئ الخارجي", "Could not launch external reader"), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            scope.launch {
                                BookDownloader.downloadBook(context, book) { success, _ ->
                                    if (success) localFile = LibraryStore.getLocalFile(context, book)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text(if (localFile != null) t("فتح في قارئ خارجي", "Open External Reader") else t("تنزيل الكتاب", "Download Book"), color = Navy, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onBack) {
                    Text(t("رجوع", "Back"), color = Gold)
                }
            }
        }
        return
    }

    if (localFile == null || isDownloading || !localFile!!.exists() || localFile!!.length() == 0L) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .themeScreenBackground(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                CircularProgressIndicator(color = Gold)
                Spacer(Modifier.height(16.dp))
                Text(t("جاري تحضير كتاب: ${book.title}...", "Preparing: ${book.title}..."), color = Gold, fontSize = 14.sp, textAlign = TextAlign.Center)
                if (downloadError != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(downloadError!!, color = Color(0xFFFF6B6B), fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        isDownloading = true
                        downloadError = null
                        scope.launch {
                            BookDownloader.downloadBook(context, book) { success, msg ->
                                isDownloading = false
                                if (success) localFile = LibraryStore.getLocalFile(context, book)
                                else downloadError = msg ?: t("فشل التحميل", "Download failed")
                            }
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                        Text(t("إعادة المحاولة", "Retry"), color = Navy)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onBack) {
                        Text(t("رجوع", "Back"), color = Gold)
                    }
                }
            }
        }
        return
    }

    val pdfFile = localFile!!
    val totalPages = remember(pdfFile) {
        try {
            PdfRendererHelper.getPageCount(pdfFile).coerceAtLeast(1)
        } catch (_: Exception) {
            1
        }
    }

    val savedProgress = remember(book.id) { LibraryStore.getProgress(context, book.id) }
    val initialPage = remember(savedProgress, totalPages) {
        val safeTotal = totalPages.coerceAtLeast(1)
        (savedProgress?.lastPage ?: 0).coerceIn(0, safeTotal - 1)
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { totalPages.coerceAtLeast(1) }
    )

    var showControls by remember { mutableStateOf(true) }
    var bookmarks by remember { mutableStateOf(LibraryStore.getBookmarks(context, book.id)) }
    var showBookmarksSheet by remember { mutableStateOf(false) }

    val isCurrentPageBookmarked = bookmarks.contains(pagerState.currentPage)

    // Auto-save reading progress
    LaunchedEffect(pagerState.currentPage, totalPages) {
        try {
            LibraryStore.saveProgress(context, book.id, pagerState.currentPage, totalPages)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        containerColor = Color(0xFF05080E)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showControls = !showControls }
                    )
                }
        ) {
            // PDF Page Viewer
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIdx ->
                PdfPageView(
                    pdfFile = pdfFile,
                    pageIndex = pageIdx
                )
            }

            // Top Control Bar Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xF0070C15), Color(0x99070C15), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ChevronRight, contentDescription = t("رجوع", "Back"), tint = Gold, modifier = Modifier.size(28.dp))
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = book.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AmiriFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = t("صفحة ", "Page ") + "${pagerState.currentPage + 1}" + t(" من ", " of ") + "$totalPages",
                                color = GoldSoft,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showBookmarksSheet = true }) {
                                Icon(Icons.Default.FormatListNumbered, contentDescription = t("الفواصل", "Bookmarks"), tint = Gold)
                            }
                            IconButton(onClick = {
                                val added = LibraryStore.toggleBookmark(context, book.id, pagerState.currentPage)
                                bookmarks = LibraryStore.getBookmarks(context, book.id)
                                val msg = if (added) t("تم حفظ العلامة المرجعية", "Bookmark saved") else t("تم إزالة العلامة المرجعية", "Bookmark removed")
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = if (isCurrentPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = t("علامة مرجعية", "Bookmark"),
                                    tint = Gold
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Control Bar Overlay (Slider + Navigation)
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC070C15), Color(0xF0070C15))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (totalPages > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("١", color = GoldSoft, fontSize = 12.sp)
                            Slider(
                                value = (pagerState.currentPage + 1).toFloat().coerceIn(1f, totalPages.toFloat()),
                                onValueChange = { newVal ->
                                    scope.launch {
                                        val page = (newVal.toInt() - 1).coerceIn(0, totalPages - 1)
                                        pagerState.scrollToPage(page)
                                    }
                                },
                                valueRange = 1f..totalPages.toFloat(),
                                steps = (totalPages - 2).coerceAtLeast(0),
                                colors = SliderDefaults.colors(
                                    thumbColor = Gold,
                                    activeTrackColor = Gold,
                                    inactiveTrackColor = NavyLight
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            )
                            Text("$totalPages", color = GoldSoft, fontSize = 12.sp)
                        }
                    }

                    // Progress Percentage Bar
                    LinearProgressIndicator(
                        progress = {
                            if (totalPages > 0) ((pagerState.currentPage + 1).toFloat() / totalPages.toFloat()).coerceIn(0f, 1f) else 0f
                        },
                        color = Gold,
                        trackColor = NavyLight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }

    // Bookmarks Sheet
    if (showBookmarksSheet) {
        AlertDialog(
            onDismissRequest = { showBookmarksSheet = false },
            containerColor = Color(0xFF0D1420),
            title = {
                Text(t("العلامات المرجعية المحفوظة", "Saved Bookmarks"), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                if (bookmarks.isEmpty()) {
                    Text(t("لم تقم بإضافة أي علامات مرجعية لهذا الكتاب بعد.", "No bookmarks saved for this book yet."), color = GoldSoft, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        bookmarks.forEach { page ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavyCard)
                                    .clickable {
                                        showBookmarksSheet = false
                                        scope.launch {
                                            if (page in 0 until totalPages) {
                                                pagerState.scrollToPage(page)
                                            }
                                        }
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(t("صفحة ${page + 1}", "Page ${page + 1}"), color = Color.White, fontSize = 14.sp)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookmarksSheet = false }) {
                    Text(t("إغلاق", "Close"), color = Gold)
                }
            }
        )
    }
}

@Composable
private fun PdfPageView(
    pdfFile: File,
    pageIndex: Int
) {
    var pageBitmap by remember(pdfFile, pageIndex) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(pdfFile, pageIndex) { mutableStateOf(true) }

    LaunchedEffect(pdfFile, pageIndex) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val bmp = try {
                PdfRendererHelper.renderPageBitmap(pdfFile, pageIndex, targetWidth = 1080, targetHeight = 1520)
            } catch (e: Exception) {
                null
            }
            withContext(Dispatchers.Main) {
                pageBitmap = bmp
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Gold, modifier = Modifier.size(36.dp))
        } else if (pageBitmap != null) {
            Image(
                bitmap = pageBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = t("تعذّر عرض هذه الصفحة", "Could not render page"),
                color = GoldSoft,
                fontSize = 13.sp
            )
        }
    }
}
