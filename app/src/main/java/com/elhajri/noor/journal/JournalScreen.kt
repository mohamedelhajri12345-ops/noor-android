package com.elhajri.noor.journal

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class JournalEntry(
    val id: Long,
    val title: String,
    val content: String,
    val mood: String,
    val date: String
)

private fun loadEntries(context: Context): List<JournalEntry> {
    val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
    val jsonStr = sp.getString("waha_journal", "[]") ?: "[]"
    val list = mutableListOf<JournalEntry>()
    try {
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                JournalEntry(
                    id = o.optLong("id", System.currentTimeMillis()),
                    title = o.optString("title", "خاطرة"),
                    content = o.optString("content", ""),
                    mood = o.optString("mood", ""),
                    date = o.optString("date", "")
                )
            )
        }
    } catch (_: Exception) {}
    return list
}

private fun saveEntries(context: Context, entries: List<JournalEntry>) {
    val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
    val arr = JSONArray()
    entries.forEach { e ->
        val o = JSONObject()
        o.put("id", e.id)
        o.put("title", e.title)
        o.put("content", e.content)
        o.put("mood", e.mood)
        o.put("date", e.date)
        arr.put(o)
    }
    sp.edit().putString("waha_journal", arr.toString()).apply()
}

@Composable
fun JournalScreen() {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(loadEntries(context)) }

    var isAddingOrEditing by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Long?>(null) }

    var inputTitle by remember { mutableStateOf("") }
    var inputContent by remember { mutableStateOf("") }
    var inputMood by remember { mutableStateOf("") }

    var deletingEntry by remember { mutableStateOf<JournalEntry?>(null) }

    val moodOptions = listOf("🤲 شكر", "🌸 تفاؤل", "💡 تفكّر", "❤️ سكينة", "😊 فرح")

    fun openAdd() {
        editingId = null
        inputTitle = ""
        inputContent = ""
        inputMood = ""
        isAddingOrEditing = true
    }

    fun openEdit(entry: JournalEntry) {
        editingId = entry.id
        inputTitle = entry.title
        inputContent = entry.content
        inputMood = entry.mood
        isAddingOrEditing = true
    }

    fun saveCurrent() {
        if (inputContent.isBlank()) return
        val todayStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())

        val newEntries = entries.toMutableList()
        if (editingId == null) {
            val newEntry = JournalEntry(
                id = System.currentTimeMillis(),
                title = inputTitle.ifBlank { "خاطرة" },
                content = inputContent.trim(),
                mood = inputMood,
                date = todayStr
            )
            newEntries.add(0, newEntry)
        } else {
            val idx = newEntries.indexOfFirst { it.id == editingId }
            if (idx != -1) {
                newEntries[idx] = JournalEntry(
                    id = editingId!!,
                    title = inputTitle.ifBlank { "خاطرة" },
                    content = inputContent.trim(),
                    mood = inputMood,
                    date = newEntries[idx].date.ifBlank { todayStr }
                )
            }
        }

        entries = newEntries
        saveEntries(context, newEntries)
        isAddingOrEditing = false
    }

    fun deleteEntry(entry: JournalEntry) {
        val newEntries = entries.filterNot { it.id == entry.id }
        entries = newEntries
        saveEntries(context, newEntries)
        deletingEntry = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "المفكرة",
                color = Gold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { openAdd() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gold)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة خاطرة",
                    tint = Navy
                )
            }
        }

        Text(
            text = "مساحتك الخاصة لتدوين الخواطر والأدعية",
            color = GoldSoft.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isAddingOrEditing) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (editingId == null) "إضافة خاطرة جديدة" else "تعديل الخاطرة",
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        placeholder = { Text("العنوان", color = GoldSoft.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Gold.copy(alpha = 0.3f),
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = inputContent,
                        onValueChange = { inputContent = it },
                        placeholder = { Text("اكتب خاطرتك هنا...", color = GoldSoft.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Gold.copy(alpha = 0.3f),
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        ),
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    Text(
                        text = "الشعور / الحالة:",
                        color = GoldSoft,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        items(moodOptions) { mood ->
                            val isSelected = mood == inputMood
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Gold else NavyLight)
                                    .clickable { inputMood = if (isSelected) "" else mood }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mood,
                                    color = if (isSelected) Navy else TextMain,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { saveCurrent() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("حفظ", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { isAddingOrEditing = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldSoft),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إلغاء")
                        }
                    }
                }
            }
        }

        if (entries.isEmpty() && !isAddingOrEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = GoldSoft.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد خواطر بعد",
                        color = GoldSoft,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "ابدأ بتدوين أول خاطرة",
                        color = GoldSoft.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(entries, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = entry.title,
                                        color = TextMain,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (entry.mood.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Gold.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = entry.mood,
                                                color = Gold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { openEdit(entry) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "تعديل",
                                            tint = GoldSoft,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { deletingEntry = entry },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (entry.date.isNotBlank()) {
                                Text(
                                    text = entry.date,
                                    color = GoldSoft.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Text(
                                text = entry.content,
                                color = TextMain.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }

    deletingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { deletingEntry = null },
            containerColor = NavyCard,
            title = {
                Text(
                    text = "حذف الخاطرة",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "هل أنت تأكد من حذف هذه الخاطرة؟",
                    color = TextMain,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { deleteEntry(entry) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حذف", color = TextMain)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deletingEntry = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إلغاء", color = GoldSoft)
                }
            }
        )
    }
}
