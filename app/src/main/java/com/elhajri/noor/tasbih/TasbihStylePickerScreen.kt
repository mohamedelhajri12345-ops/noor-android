package com.elhajri.noor.tasbih

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TouchApp
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
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.theme.noorGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihStylePickerScreen(
    onSelectStyle: (Int) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val dailyCount = remember { TasbihPrefs.getDailyCount(context) }
    val totalAllTime = remember { TasbihPrefs.getTotalAllTime(context) }
    val styles = TasbihStyle.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "أنماط المسبحة",
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AmiriFamily,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "رجوع", tint = Gold)
                        }
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Statistics Card Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noorGlassCard(cornerRadius = 20.dp)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تسبيحات اليوم", color = GoldSoft.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            toArabicNumber(dailyCount),
                            color = Gold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Gold.copy(alpha = 0.25f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("المجموع الكلي", color = GoldSoft.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            toArabicNumber(totalAllTime),
                            color = Gold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "اختر نمط التسبيح المفضل لديك:",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(styles) { index, style ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noorGlassCard(cornerRadius = 18.dp)
                            .clickable { onSelectStyle(index) }
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.15f))
                                    .border(1.dp, Gold.copy(alpha = 0.4f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (index % 2 == 0) Icons.Default.TouchApp else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = style.titleAr,
                                    color = Gold,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = AmiriFamily
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = style.descAr,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = GoldSoft.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
