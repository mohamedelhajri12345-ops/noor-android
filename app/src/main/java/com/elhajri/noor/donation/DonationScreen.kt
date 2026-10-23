package com.elhajri.noor.donation
import com.elhajri.noor.settings.t

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.theme.noorGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedAmount by remember { mutableStateOf("100") }
    var customAmount by remember { mutableStateOf("") }

    val copyToClipboard: (String, String) -> Unit = { text, label ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ $label", Toast.LENGTH_SHORT).show()
    }

    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, t("تعذر فتح الرابط", "Could not open the link"), Toast.LENGTH_SHORT).show()
        }
    }

    val bankFields = listOf(
        Triple(t("اسم المستفيد", "Beneficiary Name"), "LAAROUSSI NAJIA", t("اسم المستفيد", "Beneficiary Name")),
        Triple(t("اسم البنك", "Bank Name"), "Attijariwafa Bank", t("اسم البنك", "Bank Name")),
        Triple("RIB", "007341001431470030006084", "RIB"),
        Triple("SWIFT", "BCMAMAMC", "SWIFT"),
        Triple("IBAN", "MA64 0073 4100 1431 4700 3000 6084", "IBAN")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = t("دعم التطبيق", "Support App"),
                        color = Gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = t("رجوع", "Back"),
                            tint = Gold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070B14))
            )
        },
        containerColor = Color(0xFF070B14)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().noorGlassCard(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Gold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = t("صدقتك الجارية", "Your Ongoing Charity (Sadaqah Jariyah)"),
                        color = Gold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = t("كل تبرع منك يُبقي هذا التطبيق حياً لآلاف المسلمين", "Every donation keeps this app alive for thousands of Muslims"),
                        color = TextMain,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().noorGlassCard(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = t("اختر مبلغ التبرع", "Choose donation amount"),
                        color = GoldSoft,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val predefinedAmounts = listOf("50", "100", "200", "500")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        predefinedAmounts.forEach { amt ->
                            val isSelected = selectedAmount == amt && customAmount.isBlank()
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedAmount = amt
                                        customAmount = ""
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Gold else NavyLight
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) null else BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$amt ${t("د.م.", "MAD")}",
                                        color = if (isSelected) Navy else TextMain,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = {
                            customAmount = it
                            if (it.isNotBlank()) selectedAmount = ""
                        },
                        label = { Text("أو أدخل مبلغًا آخر (د.م.)", color = GoldSoft.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Gold.copy(alpha = 0.3f),
                            focusedTextColor = TextMain,
                            unfocusedTextColor = TextMain
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Button(
                        onClick = {
                            openUrl("https://holyquran2.base44.app/donation")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Navy,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = t("تبرع الآن عبر الموقع", "Donate now via Website"),
                                color = Navy,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().noorGlassCard(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = t("بيانات التبرع الحسابي (التحويل البنكي)", "Bank Transfer Details"),
                        color = GoldSoft,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    bankFields.forEach { (label, value, copyLabel) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().noorGlassCard(),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = label,
                                        color = GoldSoft.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = value,
                                        color = TextMain,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { copyToClipboard(value, copyLabel) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Gold.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = t("نسخ", "Copy"),
                                        tint = Gold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().noorGlassCard(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = t("خطوات التبرع", "Donation Steps"),
                        color = GoldSoft,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val steps = listOf(
                        "١" to t("انسخ RIB أعلاه", "Copy RIB above"),
                        "٢" to t("افتح تطبيق بنكك وحوّل المبلغ الذي تريده", "Open your bank app and transfer your desired amount"),
                        "٣" to t("أرسل لنا إشعار التحويل عبر واتساب لنشكرك شخصياً", "Send transfer receipt via WhatsApp so we can thank you")
                    )

                    steps.forEach { (num, text) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = num,
                                    color = Gold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = text,
                                color = TextMain,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val waUrl = "https://wa.me/212726626546?text=" + Uri.encode("السلام عليكم، قمت بالتبرع للتطبيق")
                    openUrl(waUrl)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = t("💬 تأكيد التبرع عبر واتساب", "💬 Confirm Donation via WhatsApp"),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = t("جزاك الله خيراً 🤲", "Jazakum Allahu Khairan 🤲"),
                color = Gold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}