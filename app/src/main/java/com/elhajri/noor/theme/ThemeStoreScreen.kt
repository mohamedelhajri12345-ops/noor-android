package com.elhajri.noor.theme

import com.elhajri.noor.settings.t

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.themeScreenBackground

/**
 * متجر الثيمات الـ20 المستقبليّة (2026/2027 Futuristic Theme Store).
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

    fun refresh() { points = ThemeStore.getPoints(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
            .padding(horizontal = 14.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        // ───────── رأس الصفحة: العنوان + النقاط + اربح النقاط ─────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                t("متجر الثيمات المستقبليّ", "Futuristic Theme Store"),
                color = Gold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AmiriFamily
            )
            Spacer(Modifier.weight(1f))

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
                Text(t("نقطة", "pts"), color = TextMain.copy(alpha = 0.6f), fontSize = 11.sp)
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
            Icon(Icons.Filled.Palette, contentDescription = null, tint = Navy, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text(t("اربح النقاط وافتح الثيمات", "Earn Points & Unlock Themes"), color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        // ───────── شبكة الثيمات الـ20 الموحدة ─────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(NoorThemes.all, key = { it.id }) { def ->
                val unlocked = remember(points) { ThemeStore.isUnlocked(context, def) }
                val active = NoorThemeState.active.id == def.id

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .noorGlassCard(cornerRadius = 18.dp)
                        .clickable {
                            if (unlocked) {
                                ThemeStore.setCurrentTheme(context, def.id)
                            } else {
                                if (points >= def.price) {
                                    purchaseTarget = def
                                } else {
                                    insufficientTarget = def
                                }
                            }
                        },
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ThemeMotif(def = def, modifier = Modifier.fillMaxSize())

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(def.accent)
                                        .border(1.dp, Color.White, CircleShape)
                                )

                                if (active) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Gold)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(t("نشط", "Active"), color = Navy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (!unlocked) {
                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldSoft, modifier = Modifier.size(16.dp))
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = def.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = def.description,
                                    color = GoldSoft.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))

                                if (unlocked) {
                                    Button(
                                        onClick = { ThemeStore.setCurrentTheme(context, def.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (active) Gold else Gold.copy(alpha = 0.2f),
                                            contentColor = if (active) Navy else Gold
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            if (active) t("مميّز ونشط", "Active") else t("تطبيق الثيم", "Apply Theme"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (points >= def.price) purchaseTarget = def else insufficientTarget = def
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            t("فتح بـ ${def.price} نقطة", "Unlock ${def.price} pts"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Purchase Dialog
    purchaseTarget?.let { def ->
        AlertDialog(
            onDismissRequest = { purchaseTarget = null },
            title = { Text(t("فتح الثيم", "Unlock Theme"), color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    t("هل ترغب في فتح ثيم \"${def.name}\" مقابل ${def.price} نقطة؟", "Do you want to unlock \"${def.name}\" for ${def.price} points?"),
                    color = TextMain
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = ThemeStore.purchase(context, def)
                        if (res == ThemeStore.PurchaseResult.OK) {
                            ThemeStore.setCurrentTheme(context, def.id)
                            refresh()
                        }
                        purchaseTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                ) {
                    Text(t("شراء وتفعيل", "Unlock & Apply"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseTarget = null }) {
                    Text(t("إلغاء", "Cancel"), color = GoldSoft)
                }
            },
            containerColor = NavyCard
        )
    }

    // Insufficient Points Dialog
    insufficientTarget?.let { def ->
        AlertDialog(
            onDismissRequest = { insufficientTarget = null },
            title = { Text(t("نقاط غير كافية", "Insufficient Points"), color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    t("تحتاج إلى ${def.price} نقطة لفتح \"${def.name}\". رصيدك الحالي $points نقطة.", "You need ${def.price} points to unlock \"${def.name}\". Your balance is $points pts."),
                    color = TextMain
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        insufficientTarget = null
                        onOpenEarnPoints()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                ) {
                    Text(t("اربح النقاط الآن", "Earn Points Now"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { insufficientTarget = null }) {
                    Text(t("إإغلاق", "Close"), color = GoldSoft)
                }
            },
            containerColor = NavyCard
        )
    }
}
