package com.elhajri.noor.theme

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
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
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.TextMain

/**
 * متجر الثيمات — نظام ثيمات إسلامي متكامل:
 * نقاط، إعلانات مكافأة، معاينة حقيقية، شراء وتفعيل.
 */
@Composable
fun ThemeStoreScreen(
    onOpenEarnPoints: () -> Unit
) {
    val context = LocalContext.current
    var points by remember { mutableStateOf(ThemeStore.getPoints(context)) }
    val animatedPoints by animateIntAsState(
        targetValue = points,
        animationSpec = tween(durationMillis = 600),
        label = "points"
    )

    var purchaseTarget by remember { mutableStateOf<NoorThemeDef?>(null) }
    var insufficientTarget by remember { mutableStateOf<NoorThemeDef?>(null) }
    var successTarget by remember { mutableStateOf<NoorThemeDef?>(null) }

    fun refresh() { points = ThemeStore.getPoints(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        // ───────── رأس الصفحة: العنوان + النقاط + اربح النقاط ─────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "متجر الثيمات",
                color = Gold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AmiriFamily
            )
            Spacer(Modifier.weight(1f))

            // شارة النقاط — عداد متحرك يتحدث فور حصول أو شراء
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavyCard)
                    .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.DarkMode, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("$animatedPoints", color = GoldSoft, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(3.dp))
                Text("نقطة", color = TextMain.copy(alpha = 0.6f), fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        // زر اربح النقاط
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(GoldSoft, Gold, Gold.copy(alpha = 0.85f))
                    )
                )
                .clickable { onOpenEarnPoints() }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.DarkMode, contentDescription = null, tint = Navy, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text("اربح النقاط", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        // ───────── شبكة الثيمات ─────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(NoorThemes.all) { def ->
                val unlocked = remember(points) { ThemeStore.isUnlocked(context, def) }
                val active = NoorThemeState.active.id == def.id
                ThemeCard(
                    def = def,
                    unlocked = unlocked,
                    active = active,
                    onClick = {
                        if (active) return@ThemeCard
                        if (unlocked) {
                            ThemeStore.setCurrentTheme(context, def.id)
                        } else if (ThemeStore.getPoints(context) >= def.price) {
                            purchaseTarget = def
                        } else {
                            insufficientTarget = def
                        }
                    }
                )
            }
        }
    }

    // ═════════ حوار تأكيد الشراء ═════════
    if (purchaseTarget != null) {
        val def = purchaseTarget!!
        NoorDialog(
            title = "فتح هذا الثيم؟",
            body = "${def.name}\n\nالسعر: ${def.price} 🌙",
            confirmText = "فتح الثيم",
            cancelText = "إلغاء",
            onConfirm = {
                when (ThemeStore.purchase(context, def)) {
                    ThemeStore.PurchaseResult.OK -> {
                        purchaseTarget = null
                        successTarget = def
                    }
                    ThemeStore.PurchaseResult.INSUFFICIENT_POINTS -> {
                        purchaseTarget = null
                        insufficientTarget = def
                    }
                    ThemeStore.PurchaseResult.ALREADY_UNLOCKED -> {
                        purchaseTarget = null
                    }
                }
                refresh()
            },
            onDismiss = { purchaseTarget = null }
        )
    }

    // ═════════ حوار نقاط غير كافية ═════════
    if (insufficientTarget != null) {
        val def = insufficientTarget!!
        NoorDialog(
            title = "نقاطك غير كافية",
            body = "تحتاج إلى ${def.price} 🌙 لفتح ثيم «${def.name}».\nرصيدك الحالي: ${ThemeStore.getPoints(context)} 🌙",
            confirmText = "اربح النقاط",
            cancelText = "إلغاء",
            onConfirm = {
                insufficientTarget = null
                onOpenEarnPoints()
            },
            onDismiss = { insufficientTarget = null }
        )
    }

    // ═════════ حوار نجاح الشراء ═════════
    if (successTarget != null) {
        val def = successTarget!!
        NoorDialog(
            title = "تم فتح الثيم بنجاح ✓",
            body = "يمكنك تفعيل «${def.name}» الآن.",
            confirmText = "تفعيل الآن",
            cancelText = "لاحقاً",
            onConfirm = {
                ThemeStore.setCurrentTheme(context, def.id)
                successTarget = null
            },
            onDismiss = { successTarget = null }
        )
    }
}

/**
 * بطاقة ثيم — معاينة حقيقية مصغرة + اسم + وصف + السعر/الحالة.
 */
@Composable
private fun ThemeCard(
    def: NoorThemeDef,
    unlocked: Boolean,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(def.cardRadius + 2.dp))
            .background(NavyCard)
            .border(
                1.dp,
                if (active) def.accent else Gold.copy(alpha = 0.10f),
                RoundedCornerShape(def.cardRadius + 2.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        // معاينة مصغرة حقيقية: شريط علوي، بطاقة، زر، شريط تنقل
        ThemePreview(def)

        Spacer(Modifier.height(8.dp))

        Text((com.elhajri.noor.theme.NoorThemes.icons[def.id] ?: "✨") + " " + def.name, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(
            def.description,
            color = TextMain.copy(alpha = 0.5f),
            fontSize = 10.sp,
            lineHeight = 14.sp,
            maxLines = 2
        )

        Spacer(Modifier.height(8.dp))

        // السعر أو الحالة
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when {
                        active -> def.accent.copy(alpha = 0.12f)
                        unlocked -> def.accent.copy(alpha = 0.12f)
                        else -> NavyLight.copy(alpha = 0.6f)
                    }
                )
                .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when {
                    active -> "مفعّل ✓"
                    unlocked -> "تفعيل"
                    else -> "فتح الثيم — ${def.price} 🌙"
                },
                color = if (active) def.accent else if (unlocked) def.accent else TextMain.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        if (def.price == 0 && !active) {
            Spacer(Modifier.height(4.dp))
            Text("مجاني", color = EmeraldSuccessSoft, fontSize = 10.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }
    }
}

private val EmeraldSuccessSoft = Color(0xFF34D399)

/**
 * معاينة مصغرة للتطبيق بلون الثيم — ليست مجرد مربع لون:
 * شريط علوي + بطاقة + زر + نصوص + شريط تنقل سفلي بأيقونات.
 */
@Composable
fun ThemePreview(def: NoorThemeDef) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, def.accent.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(listOf(def.background, def.backgroundEnd))
                )
                .drawBehind { drawThemeMotif(def) }
                .padding(6.dp)
        ) {
        // الشريط العلوي
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(def.surface)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(8.dp).clip(CircleShape).background(def.accent)
            )
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier.width(26.dp).height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(def.textSecondary.copy(alpha = 0.5f))
            )
            Spacer(Modifier.weight(1f))
            repeat(3) {
                Spacer(Modifier.width(3.dp))
                Box(Modifier.size(3.dp).clip(CircleShape).background(def.accent.copy(alpha = 0.5f)))
            }
        }

        Spacer(Modifier.height(5.dp))

        // البطاقة
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(7.dp))
                .background(def.surfaceVariant)
                .border(1.dp, def.accent.copy(alpha = 0.15f), RoundedCornerShape(7.dp))
                .padding(5.dp)
        ) {
            Text("﴾ بسم الله ﴿", color = def.accent, fontSize = 9.sp, fontFamily = AmiriFamily)
            Spacer(Modifier.height(2.dp))
            Box(
                Modifier.width(48.dp).height(2.dp)
                    .background(def.textSecondary.copy(alpha = 0.35f))
            )
            Spacer(Modifier.height(3.dp))
            // زر الثيم
            Box(
                modifier = Modifier
                    .width(52.dp).height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.horizontalGradient(def.buttonGradient)),
                contentAlignment = Alignment.Center
            ) {
                Text("قراءة", color = def.background, fontSize = 6.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(5.dp))

        // شريط التنقل السفلي
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(def.surface)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(4) { i ->
                Box(
                    Modifier.size(7.dp).clip(CircleShape)
                        .background(if (i == 0) def.accent else def.textSecondary.copy(alpha = 0.4f))
                )
            }
        }
        }
    }
}

/**
 * حوار موحّد بتصميم التطبيق — RTL.
 */
@Composable
fun NoorDialog(
    title: String,
    body: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(NavyCard)
                .border(1.dp, Gold.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.DarkMode,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(title, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(body, color = TextMain.copy(alpha = 0.65f), fontSize = 12.sp,
                lineHeight = 18.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // زر الإلغاء على يمين الحوار (RTL: أول عنصر = اليمين)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyLight.copy(alpha = 0.7f))
                        .clickable { onDismiss() }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(cancelText, color = TextMain.copy(alpha = 0.7f), fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(GoldSoft, Gold)))
                        .clickable { onConfirm() }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(confirmText, color = Navy, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
