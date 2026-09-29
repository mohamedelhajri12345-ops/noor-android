package com.elhajri.noor.stories

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import org.json.JSONObject
import com.elhajri.noor.ui.NoorGradients

private data class StoryDetail(
    val id: Int,
    val prophet: String,
    val title: String,
    val icon: String,
    val text: String
)

@Composable
fun StoryDetailScreen(storyId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    var currentId by remember(storyId) { mutableIntStateOf(storyId) }

    val allStories = remember { loadAllStories(context) }
    val story = remember(currentId, allStories) { allStories.find { it.id == currentId } }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = Gold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = story?.prophet ?: "قصة نبي",
                style = MaterialTheme.typography.titleLarge,
                color = Gold,
                fontWeight = FontWeight.Bold
            )
        }

        if (story == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "القصة غير موجودة",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoldSoft
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = story.prophet,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontFamily = AmiriFamily,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = story.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldSoft,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(color = Gold.copy(alpha = 0.2f), thickness = 1.dp)

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = story.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = AmiriFamily,
                                    fontSize = 19.sp,
                                    lineHeight = 34.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Next / Prev Story Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val prevExists = allStories.any { it.id == currentId - 1 }
                    val nextExists = allStories.any { it.id == currentId + 1 }

                    if (prevExists) {
                        OutlinedButton(
                            onClick = { currentId -= 1 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold)
                        ) {
                            Text("← القصة السابقة", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (nextExists) {
                        Button(
                            onClick = { currentId += 1 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                        ) {
                            Text("القصة التالية →", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun loadAllStories(context: Context): List<StoryDetail> {
    val list = mutableListOf<StoryDetail>()
    try {
        val jsonStr = context.assets.open("data/stories.json").bufferedReader().use { it.readText() }
        val arr = JSONObject(jsonStr).getJSONArray("stories")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                StoryDetail(
                    id = obj.getInt("id"),
                    prophet = obj.optString("prophet", ""),
                    title = obj.getString("title"),
                    icon = obj.optString("icon", ""),
                    text = obj.optString("text", "")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}
