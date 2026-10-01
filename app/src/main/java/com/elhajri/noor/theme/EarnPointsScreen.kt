package com.elhajri.noor.theme

import android.app.Activity
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ads.RewardedAds
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.delay

/**
 * اربح النقاط — نظام إعلانات المكافأة:
 * 3 إعلانات في الجلسة، كل إعلان مكتمل = +100 🌙 (تمنح فقط عند إكمال SDK).
 */
@Composable
fun EarnPointsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    var points by remember { mutableStateOf(ThemeStore.getPoints(context)) }
    val animatedPoints by animateIntAsState(
        targetValue = points,
        animationSpec = tween(600), label = "earn_points"
    )
    var sessionAds by remember { mutableStateOf(ThemeStore.getAdsCompletedInSession(context)) }
    var adReady by remember { mutableStateOf(RewardedAds.isReady()) }
    var showingAd by remember { mutableStateOf(false) }
    var justEarned by remember { mutableStateOf(false) }
    var adError by remember { mutableStateOf(false) }

    val sessionComplete = sessionAds >= ThemeStore.ADS_PER_SESSION

    // تحميل مسبق للإعلان عند دخول الصفحة + متابعة جهوزيته
    LaunchedEffect(Unit) {
        RewardedAds.preload(context)
        while (true) {
            adReady = RewardedAds.isReady()
            if (!RewardedAds.isReady() && !RewardedAds.isLoading()) RewardedAds.preload(context)
            delay(800)
        }
    }

    fun watchAd() {
        val act = activity ?: return
        if (showingAd || sessionComplete) return
        showingAd = true
        adError = false
        RewardedAds.prepareForShow()
        RewardedAds.show(
            act,
            onReward = {
                // المكافأة: فقط بعد إكمال الإعلان حسب SDK — مصدر واحد للحقيقة
                val completed = ThemeStore.recordAdCompleted(context)
                ThemeStore.addPoints(context, ThemeStore.POINTS_PER_AD)
                sessionAds = completed
                points = ThemeStore.getPoints(context)
                justEarned = true
            },
            onClosed = {
                showingAd = false
                adReady = RewardedAds.isReady()
                RewardedAds.preload(context)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // شريط الرجوع
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null, tint = Gold,
                modifier = Modifier.size(22.dp).clickable { onBack() }
            )
        }

        Spacer(Modifier.height(18.dp))

        // ───────── العنوان + النقاط ─────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.DarkMode,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("اربح النقاط", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily)
            Spacer(Modifier.height(6.dp))

            // شارة الرصيد — تتحدث فور الحصول على نقاط
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavyCard)
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.DarkMode, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("$animatedPoints", color = GoldSoft, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text("نقطة", color = TextMain.copy(alpha = 0.6f), fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))
            if (justEarned && !sessionComplete) {
                Text("+${ThemeStore.POINTS_PER_AD} 🌙", color = GoldSoft, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
            }
            Text("شاهد إعلانًا واحصل على 100 نقطة", color = TextMain.copy(alpha = 0.6f), fontSize = 13.sp)
        }

        Spacer(Modifier.height(20.dp))

        // ───────── تقدم إعلانات الجلسة ─────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NavyCard)
                .border(1.dp, Gold.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text("إعلانات الجلسة", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            for (i in 1..ThemeStore.ADS_PER_SESSION) {
                val done = i <= sessionAds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                if (done) Gold.copy(alpha = 0.15f) else NavyLight.copy(alpha = 0.6f)
                            )
                            .border(
                                1.dp,
                                if (done) Gold else Gold.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
                        } else {
                            Text("○", color = TextMain.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "الإعلان ${
                            when (i) {
                                1 -> "الأول"
                                2 -> "الثاني"
                                else -> "الثالث"
                            }
                        }",
                        color = if (done) TextMain else TextMain.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = if (done) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (done) "✓ +${ThemeStore.POINTS_PER_AD} 🌙" else "+${ThemeStore.POINTS_PER_AD} 🌙",
                        color = if (done) Gold else TextMain.copy(alpha = 0.45f),
                        fontSize = 12.sp,
                        fontWeight = if (done) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (sessionComplete) {
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold.copy(alpha = 0.10f))
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "أحسنت! حصلت على ${ThemeStore.ADS_PER_SESSION * ThemeStore.POINTS_PER_AD} نقطة 🌙",
                        color = GoldSoft,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "جلسة جديدة",
                        color = Gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavyLight.copy(alpha = 0.6f))
                            .clickable {
                                ThemeStore.startNewSession(context)
                                sessionAds = 0
                                justEarned = false
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ───────── زر مشاهدة الإعلان ─────────
        val enabled = !showingAd && !sessionComplete && adReady
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (enabled) Brush.horizontalGradient(listOf(GoldSoft, Gold))
                    else Brush.horizontalGradient(listOf(NavyLight.copy(alpha = 0.6f), NavyLight.copy(alpha = 0.6f)))
                )
                .clickable(enabled = enabled) { watchAd() }
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showingAd) {
                CircularProgressIndicator(
                    color = Gold,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("جارٍ عرض الإعلان...", color = TextMain.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            } else if (sessionComplete) {
                Text("اكتملت جلسة المكافآت ✓", color = TextMain.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            } else if (!adReady) {
                CircularProgressIndicator(
                    color = Gold,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("جارٍ تحميل الإعلان...", color = TextMain.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Navy, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("شاهد الإعلان +${ThemeStore.POINTS_PER_AD} 🌙", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (adError) {
            Spacer(Modifier.height(8.dp))
            Text(
                "تعذّر تحميل الإعلان حالياً — تحقق من الاتصال وحاول مجدداً",
                color = TextMain.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "لا تُمنح النقاط إلا بعد إكمال الإعلان كاملاً — الإغلاق المبكر لا يمنح شيئاً",
            color = TextMain.copy(alpha = 0.35f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))
    }
}
