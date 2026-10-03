package com.elhajri.noor.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.audio.player.NasheedPlayerManager
import com.elhajri.noor.audio.player.PlayerState
import com.elhajri.noor.audio.player.QuranPlayerManager
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.NavyCard

private val PlayerBarBg = Color(0xFF0D1330)

fun toArabicDigits(n: Long): String {
    val digits = "٠١٢٣٤٥٦٧٨٩"
    return n.toString().map { if (it.isDigit()) digits[it - '0'] else it }.joinToString("")
}

fun formatPlayerTime(sec: Float): String {
    if (sec.isNaN() || sec <= 0f) return "٠:٠٠"
    val total = sec.toInt()
    val m = total / 60
    val s = total % 60
    return toArabicDigits(m.toLong()) + ":" + toArabicDigits(s.toLong()).padStart(2, '٠')
}

private data class ActivePlayer(
    val state: PlayerState,
    val isQuran: Boolean
)

/**
 * Faithful Compose port of the web AudioPlayerBar.jsx.
 * Shows the active player (Quran player takes priority) — mini bar with a
 * tap-to-expand full player bottom sheet: seek, shuffle, repeat, prev/next,
 * speed and sleep timer. All native, zero WebView.
 */
@Composable
fun NoorPlayerBar(modifier: Modifier = Modifier) {
    val quranState by QuranPlayerManager.state.collectAsState()
    val nasheedState by NasheedPlayerManager.state.collectAsState()

    val active = when {
        quranState.currentId != null -> ActivePlayer(quranState, true)
        nasheedState.currentId != null -> ActivePlayer(nasheedState, false)
        else -> null
    } ?: return

    var expanded by remember { mutableStateOf(false) }
    // إخفاء الشريط من الشاشة (الصوت يستمر بالخلفية) — يُعاد إظهاره عند تشغيل مقطع جديد
    var dismissedId by remember { mutableStateOf<String?>(null) }
    if (dismissedId == active.state.currentId) return
    val context = LocalContext.current

    fun manager(): com.elhajri.noor.audio.player.PlayerFacade = if (active.isQuran) QuranPlayerManager else NasheedPlayerManager

    Column(modifier = modifier.fillMaxWidth()) {
        // MINI BAR — like the web: [play] title/artist + progress + expand
        Surface(
            color = PlayerBarBg,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // play / pause
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Gold, Color(0xFFB8941F)))
                            )
                            .clickable {
                                manager().ensurePlayer(context)
                                manager().toggle()
                            }
                    ) {
                        Icon(
                            if (active.state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (active.state.isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = Color(0xFF0B1B3A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    // title + artist
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { expanded = true }
                    ) {
                        Text(
                            active.state.title.ifEmpty { "قيد التشغيل" },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                active.state.artist,
                                color = Gold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    // expand
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = "توسيع المشغل",
                        tint = Gold,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable { expanded = true }
                            .padding(2.dp)
                    )
                    // إخفاء من الشاشة — يستمر التشغيل بالخلفية
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "إخفاء المشغل (يستمر الصوت في الخلفية)",
                        tint = GoldSoft.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable { dismissedId = active.state.currentId }
                            .padding(3.dp)
                    )
                }
                // thin progress line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0xFF1A2445))
                ) {
                    val pct = if (active.state.duration > 0f)
                        (active.state.currentTime / active.state.duration).coerceIn(0f, 1f)
                    else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(pct)
                            .background(Gold)
                            .align(Alignment.CenterStart)
                    )
                }
            }
        }
    }

    if (expanded) {
        FullPlayerSheet(active, onDismiss = { expanded = false })
    }
}

/**
 * ورقة المشغل الكامل — طبق الأصل عن الموقع: شريحة ثابتة تنزلق من الأسفل بحركة
 * واحدة منتظمة (slide-up) فوق ستار معتم، تُغلق بالنقر على الستار أو زر "إيقاف"،
 * بلا فيزياء سحب عشوائية الاستقرار (كانت سبب شعور المشغل بأنه "يتحرك عشوائياً").
 * تُحسب حشوة شريط تنقّل النظام أسفلها كي لا تُغطّى أزرار الهاتف صفوف التحكم السفلية.
 */
@Composable
private fun FullPlayerSheet(active: ActivePlayer, onDismiss: () -> Unit) {
    val context = LocalContext.current
    fun manager(): com.elhajri.noor.audio.player.PlayerFacade = if (active.isQuran) QuranPlayerManager else NasheedPlayerManager
    var showSleep by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    fun requestClose() { visible = false }

    LaunchedEffect(visible) {
        if (!visible) {
            kotlinx.coroutines.delay(260)
            onDismiss()
        }
    }

    androidx.activity.compose.BackHandler(enabled = true) { requestClose() }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.55f else 0f,
        animationSpec = tween(260),
        label = "scrim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = scrimAlpha))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { requestClose() }
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = androidx.compose.animation.slideInVertically(
                animationSpec = tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            ) { it } + androidx.compose.animation.fadeIn(tween(220)),
            exit = androidx.compose.animation.slideOutVertically(
                animationSpec = tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            ) { it } + androidx.compose.animation.fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PlayerBarBg)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { /* يمتص النقر — لا يُغلق المشغل عند النقر داخل الورقة */ }
                .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.navigationBars)
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 20.dp)
        ) {
            // drag handle
            Box(
                Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
            Spacer(Modifier.height(14.dp))

            // icon disc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.12f))
                    .border(1.dp, Gold.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Gold, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(12.dp))

            Text(
                active.state.title.ifEmpty { "قيد التشغيل" },
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(active.state.artist, color = Gold, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            // seek (RTL: slider value goes right-to-left)
            val s = active.state
            var dragging by remember { mutableStateOf(false) }
            var dragValue by remember { mutableStateOf(0f) }
            Slider(
                value = if (dragging) dragValue else (s.duration - s.currentTime).coerceIn(0f, s.duration),
                onValueChange = { dragging = true; dragValue = it },
                onValueChangeFinished = {
                    manager().ensurePlayer(context)
                    manager().seek((s.duration - dragValue).coerceIn(0f, s.duration))
                    dragging = false
                },
                valueRange = 0f..(if (s.duration > 0f) s.duration else 1f),
                colors = SliderDefaults.colors(
                    thumbColor = Gold,
                    activeTrackColor = Gold,
                    inactiveTrackColor = Color(0xFF22315E)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(formatPlayerTime(s.currentTime), color = GoldSoft.copy(alpha = 0.7f), fontSize = 11.sp)
                Text(formatPlayerTime(s.duration), color = GoldSoft.copy(alpha = 0.7f), fontSize = 11.sp)
            }

            Spacer(Modifier.height(10.dp))

            // main controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Shuffle,
                    contentDescription = "عشوائي",
                    tint = if (s.shuffle) Gold else Color(0xFF6B7594),
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable { manager().toggleShuffle() }
                        .padding(4.dp)
                )
                Spacer(Modifier.width(14.dp))
                Icon(
                    Icons.Filled.SkipNext,
                    contentDescription = "التالي",
                    tint = Color.White,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(NavyCard)
                        .clickable { manager().next() }
                        .padding(13.dp)
                )
                Spacer(Modifier.width(18.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Gold, Color(0xFFB8941F))))
                        .clickable { manager().toggle() }
                ) {
                    Icon(
                        if (s.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (s.isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = Color(0xFF0B1B3A),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.width(18.dp))
                Icon(
                    Icons.Filled.SkipPrevious,
                    contentDescription = "السابق",
                    tint = Color.White,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(NavyCard)
                        .clickable { manager().prev() }
                        .padding(13.dp)
                )
                Spacer(Modifier.width(14.dp))
                Box {
                    Icon(
                        Icons.Filled.Loop,
                        contentDescription = "تكرار",
                        tint = if (s.repeatMode != "none") Gold else Color(0xFF6B7594),
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable { manager().toggleRepeat() }
                            .padding(4.dp)
                    )
                    if (s.repeatMode == "one") {
                        Text(
                            "١",
                            color = Gold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // secondary controls: speed / sleep / stop
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // speed
                val speeds = listOf(0.75f, 1f, 1.25f, 1.5f)
                Surface(
                    color = NavyCard,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        val next = speeds[(speeds.indexOf(s.rate) + 1) % speeds.size]
                        manager().setRate(next)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Filled.Speed, contentDescription = "السرعة", tint = Gold, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            (if (s.rate % 1f == 0f) toArabicDigits(s.rate.toInt().toLong()) else "${s.rate}") + "×",
                            color = Color.White, fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))

                // sleep timer
                Surface(
                    color = if (s.sleepTimerActive) Gold.copy(alpha = 0.18f) else NavyCard,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { showSleep = !showSleep }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Filled.Bedtime, contentDescription = "مؤقت النوم", tint = Gold, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("مؤقت النوم", color = Color.White, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))

                // stop
                Surface(
                    color = Color(0xFF2A1520),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB04050).copy(alpha = 0.5f)),
                    modifier = Modifier.clickable {
                        manager().stop()
                        requestClose()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color(0xFFE07080), modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("إيقاف", color = Color(0xFFE07080), fontSize = 12.sp)
                    }
                }
            }

            if (showSleep) {
                Spacer(Modifier.height(12.dp))
                val options = listOf(
                    "إيقاف" to 0,
                    "نهاية السورة" to -1,
                    "٥ دقائق" to 5,
                    "١٥ دقيقة" to 15,
                    "٣٠ دقيقة" to 30,
                    "٦٠ دقيقة" to 60
                )
                Surface(
                    color = NavyCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        options.forEach { (label, mins) ->
                            Text(
                                label,
                                color = GoldSoft,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        manager().setSleepTimer(mins)
                                        showSleep = false
                                    }
                                    .padding(horizontal = 24.dp, vertical = 7.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
