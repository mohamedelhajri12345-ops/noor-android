package com.elhajri.noor.quran

import com.elhajri.noor.ui.themeScreenBackground
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import com.elhajri.noor.ui.NoorGradients
import com.elhajri.noor.theme.noorGlassCard

private const val TRACKER_PREFS = "nur_quran_tracker_prefs"
private const val KEY_TRACKER = "tracker_json"
private const val TOTAL_QURAN_PAGES = 604

data class BadgeItem(val icon: String, val label: String, val unlocked: Boolean)

@Composable
fun TrackerScreen() {
    val context = LocalContext.current
    val allSurahs = remember { DataLoader.surahs(context) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tracker, 1: Reports, 2: Surahs

    // Tracker state loaded from SharedPreferences JSON
    val sp = remember { context.getSharedPreferences(TRACKER_PREFS, Context.MODE_PRIVATE) }
    var trackerJsonStr by remember { mutableStateOf(sp.getString(KEY_TRACKER, "{}") ?: "{}") }

    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    // Parse state from JSON
    val trackerObj = remember(trackerJsonStr) {
        try { JSONObject(trackerJsonStr) } catch (e: Exception) { JSONObject() }
    }

    val goal = trackerObj.optInt("goal", 5)
    val streak = trackerObj.optInt("streak", 0)

    val todayObj = trackerObj.optJSONObject(todayKey) ?: JSONObject()
    val todayPages = todayObj.optInt("pages", 0)

    val completedSurahsSet = remember(trackerJsonStr) {
        val set = mutableSetOf<Int>()
        val arr = trackerObj.optJSONArray("completed_surahs") ?: JSONArray()
        for (i in 0 until arr.length()) {
            set.add(arr.getInt(i))
        }
        set
    }

    // Function to update tracker JSON and save
    val saveTracker = { newObj: JSONObject ->
        val s = newObj.toString()
        sp.edit().putString(KEY_TRACKER, s).apply()
        trackerJsonStr = s
    }

    val updatePages = { delta: Int ->
        val obj = JSONObject(trackerJsonStr)
        val tObj = obj.optJSONObject(todayKey) ?: JSONObject()
        val current = tObj.optInt("pages", 0)
        val nextVal = maxOf(0, current + delta)
        tObj.put("pages", nextVal)

        val completedBefore = tObj.optBoolean("completed", false)
        if (delta > 0 && nextVal >= goal && !completedBefore) {
            tObj.put("completed", true)
            val currentStreak = obj.optInt("streak", 0)
            obj.put("streak", currentStreak + 1)
        }
        obj.put(todayKey, tObj)
        saveTracker(obj)
    }

    val setGoal = { newGoal: Int ->
        val obj = JSONObject(trackerJsonStr)
        obj.put("goal", newGoal)
        saveTracker(obj)
    }

    val toggleSurahComplete = { surahNum: Int ->
        val obj = JSONObject(trackerJsonStr)
        val arr = obj.optJSONArray("completed_surahs") ?: JSONArray()
        val list = mutableListOf<Int>()
        for (i in 0 until arr.length()) {
            list.add(arr.getInt(i))
        }
        if (list.contains(surahNum)) {
            list.remove(surahNum)
        } else {
            list.add(surahNum)
        }
        val newArr = JSONArray()
        list.forEach { newArr.put(it) }
        obj.put("completed_surahs", newArr)
        saveTracker(obj)
    }

    // All time pages total
    val allTimeTotalPages = remember(trackerJsonStr) {
        var sum = 0
        val keys = trackerObj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (k != "goal" && k != "streak" && k != "completed_surahs") {
                val day = trackerObj.optJSONObject(k)
                if (day != null) {
                    sum += day.optInt("pages", 0)
                }
            }
        }
        sum
    }

    val badges = listOf(
        BadgeItem("🔥", "٣ أيام", streak >= 3),
        BadgeItem("🌟", "٧ أيام", streak >= 7),
        BadgeItem("🏆", "٣٠ يوم", streak >= 30),
        BadgeItem("📚", "١٠٠ صفحة", allTimeTotalPages >= 100),
        BadgeItem("💎", "٥٠٠ صفحة", allTimeTotalPages >= 500),
        BadgeItem("👑", "٦٠٤ صفحة", allTimeTotalPages >= 604)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
            .padding(16.dp)
    ) {
        // Top Header Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NavyCard)
                .padding(4.dp)
        ) {
            TabButton(
                title = "متابعة اليوم",
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "التقارير",
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "السور المكتملة",
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // Tracker Main View
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    // Ring Progress Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        .noorGlassCard()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ورد اليوم من القرآن",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Ring progress canvas
                                val progressFraction = if (goal > 0) (todayPages.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(150.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val strokeWidth = 14.dp.toPx()
                                        val diameter = size.minDimension - strokeWidth
                                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                                        val arcSize = Size(diameter, diameter)

                                        // Background Track
                                        drawArc(
                                            color = Gold.copy(alpha = 0.15f),
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth)
                                        )

                                        // Progress Arc
                                        drawArc(
                                            color = Gold,
                                            startAngle = -90f,
                                            sweepAngle = progressFraction * 360f,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${(progressFraction * 100).toInt()}%",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Gold
                                        )
                                        Text(
                                            text = "${toArabicNumber(todayPages)}/${toArabicNumber(goal)} صفحة",
                                            fontSize = 12.sp,
                                            color = GoldSoft
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Stats Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Card(modifier = Modifier.noorGlassCard(), 
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(toArabicNumber(streak), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
                                            Text("أيام متتالية", fontSize = 10.sp, color = GoldSoft.copy(alpha = 0.7f))
                                        }
                                    }
                                    Card(modifier = Modifier.noorGlassCard(), 
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(toArabicNumber(allTimeTotalPages), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
                                            Text("إجمالي الصفحات", fontSize = 10.sp, color = GoldSoft.copy(alpha = 0.7f))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Increment / Decrement Buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { updatePages(-1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.2f), contentColor = Gold),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(48.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { updatePages(1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("+١ صفحة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { updatePages(5) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.2f), contentColor = Gold),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("+٥", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (todayPages >= goal) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("بارك الله فيك! أكملت ورد اليوم ✓", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Goal Selector
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        .noorGlassCard()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("الهدف اليومي (صفحات)", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(3, 5, 10, 20).forEach { g ->
                                        val isSelected = goal == g
                                        Button(
                                            onClick = { setGoal(g) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) Gold else NavyLight,
                                                contentColor = if (isSelected) Navy else Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(toArabicNumber(g), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Badges Section
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        .noorGlassCard()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("الإنجازات والشارات", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    badges.take(3).forEach { badge ->
                                        BadgeCard(badge = badge, modifier = Modifier.weight(1f).padding(4.dp))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    badges.drop(3).forEach { badge ->
                                        BadgeCard(badge = badge, modifier = Modifier.weight(1f).padding(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Reports View
                val completedKhatmas = allTimeTotalPages / TOTAL_QURAN_PAGES
                val currentKhatmaPages = allTimeTotalPages % TOTAL_QURAN_PAGES
                val khatmaPct = ((currentKhatmaPages.toFloat() / TOTAL_QURAN_PAGES.toFloat()) * 100).toInt()

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    // Khatma Progress
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        .noorGlassCard()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("التقدم في الختمة الحالية", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(110.dp)
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val strokeWidth = 10.dp.toPx()
                                            val diameter = size.minDimension - strokeWidth
                                            drawArc(
                                                color = Gold.copy(alpha = 0.15f),
                                                startAngle = 0f,
                                                sweepAngle = 360f,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth)
                                            )
                                            drawArc(
                                                color = Gold,
                                                startAngle = -90f,
                                                sweepAngle = (khatmaPct / 100f) * 360f,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("$khatmaPct%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
                                            Text("$currentKhatmaPages/$TOTAL_QURAN_PAGES", fontSize = 10.sp, color = GoldSoft)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ReportItem("الختمات المكتملة", toArabicNumber(completedKhatmas))
                                        ReportItem("إجمالي الصفحات", toArabicNumber(allTimeTotalPages))
                                        ReportItem("المتبقي للختمة", "${toArabicNumber(TOTAL_QURAN_PAGES - currentKhatmaPages)} صفحة")
                                    }
                                }
                            }
                        }
                    }

                    // Surah Completed count
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        .noorGlassCard()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("السور المكتملة قراءةً", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("السور التي ختمت قراءتها كاملة", color = GoldSoft.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                                Text(
                                    text = "${toArabicNumber(completedSurahsSet.size)} / ١١٤",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Surah Completion Grid (114 Surahs)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item {
                        Text(
                            text = "علم على السور التي أتأمت قراءتها (${completedSurahsSet.size}/114)",
                            color = Gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    listItems(allSurahs.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { surah ->
                                val isDone = completedSurahsSet.contains(surah.number)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { toggleSurahComplete(surah.number) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDone) Gold.copy(alpha = 0.2f) else NavyCard
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("سورة ${surah.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("${toArabicNumber(surah.ayahs)} آية", fontSize = 10.sp, color = GoldSoft.copy(alpha = 0.7f))
                                        }
                                        Icon(
                                            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isDone) Gold else GoldSoft.copy(alpha = 0.3f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Gold else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Navy else GoldSoft
        )
    }
}

@Composable
private fun BadgeCard(badge: BadgeItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (badge.unlocked) Gold.copy(alpha = 0.15f) else NavyLight.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(badge.icon, fontSize = 24.sp)
            Text(
                badge.label,
                fontSize = 10.sp,
                color = if (badge.unlocked) Gold else GoldSoft.copy(alpha = 0.4f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReportItem(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    .noorGlassCard()) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(label, fontSize = 10.sp, color = GoldSoft.copy(alpha = 0.7f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gold)
        }
    }
}
