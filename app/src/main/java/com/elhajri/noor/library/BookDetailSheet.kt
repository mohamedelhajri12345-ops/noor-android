package com.elhajri.noor.library

import com.elhajri.noor.settings.t

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.noorGlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailSheet(
    book: Book,
    onDismiss: () -> Unit,
    onReadClick: (Book) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDownloaded by remember { mutableStateOf(LibraryStore.isBookAvailableLocally(context, book)) }
    val downloadStates by BookDownloader.downloadStates.collectAsState()
    val downloadState = downloadStates[book.id] ?: DownloadState.Idle

    var previewBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoadingPreviews by remember { mutableStateOf(false) }

    LaunchedEffect(book.id, isDownloaded) {
        if (LibraryStore.isBookAvailableLocally(context, book)) {
            isLoadingPreviews = true
            withContext(Dispatchers.IO) {
                val list = PdfRendererHelper.renderPreviewPages(context, book, maxPages = 3)
                withContext(Dispatchers.Main) {
                    previewBitmaps = list
                    isLoadingPreviews = false
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF090E18),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Gold.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row with Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t("تفاصيل الكتاب", "Book Details"),
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFamily
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = t("إغلاق", "Close"), tint = GoldSoft)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cover & Hero Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Book Cover Card
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(155.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF14241B), Color(0xFF0B140E))
                            )
                        )
                        .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (book.coverUrl.isNotBlank()) {
                        AsyncImage(
                            model = book.coverUrl,
                            contentDescription = book.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, tint = Gold, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(
                                book.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title, Author, Category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AmiriFamily,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = t("المؤلف: ", "Author: ") + book.author,
                        color = GoldSoft,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Gold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = LibraryCategories.getCategoryNameAr(book.category),
                                color = Gold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = NavyLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${book.pages} " + t("صفحة", "pages"),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = t("التنسيق: ", "Format: ") + book.format.uppercase() + t(" | الرخصة: ", " | License: ") + book.license,
                        color = GoldSoft.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onReadClick(book) },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Navy)
                    Spacer(Modifier.width(6.dp))
                    Text(t("قراءة الآن", "Read Now"), color = Navy, fontWeight = FontWeight.Bold)
                }

                if (!isDownloaded) {
                    Button(
                        onClick = {
                            scope.launch {
                                BookDownloader.downloadBook(context, book) { success, msg ->
                                    if (success) {
                                        isDownloaded = true
                                        Toast.makeText(context, t("تم تنزيل الكتاب بنجاح", "Book downloaded successfully"), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, msg ?: t("فشل تنزيل الكتاب", "Failed to download book"), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Gold)
                        Spacer(Modifier.width(4.dp))
                        Text(t("تنزيل", "Download"), color = Gold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
