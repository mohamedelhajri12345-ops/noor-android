package com.elhajri.noor.exams
import com.elhajri.noor.settings.t

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground

@Composable
fun ExamCertificateScreen(
    track: ExamTrack,
    difficulty: ExamDifficulty,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf(ExamStorage.getUserName(context)) }
    var showEditDialog by remember { mutableStateOf(false) }
    var certBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(userName, track, difficulty) {
        certBitmap = ExamCertificateGenerator.createCertificateBitmap(context, userName, track, difficulty)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = t("رجوع", "Back"),
                        tint = Gold
                    )
                }
                Text(
                    text = if (track == ExamTrack.KID) "شهادة الجدارة" else "شهادة التمكن",
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = t("تعديل الاسم", "Edit Name"),
                        tint = Gold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "مبارك! لقد استحققت هذه الشهادة الرسمية لإتمام المستوى",
                color = GoldSoft,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Certificate High-Res Preview Card
            certBitmap?.let { bmp ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 16.dp)
                        .padding(8.dp)
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = t("شهادة التخرج", "Graduation Certificate"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2000f / 1414f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            } ?: run {
                CircularProgressIndicator(color = Gold, modifier = Modifier.padding(32.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Name Box
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("الاسم المطبوع على الشهادة:", color = GoldSoft, fontSize = 12.sp)
                        Text(userName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.2f))
                    ) {
                        Text("تغيير", color = Gold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Save Image
                Button(
                    onClick = {
                        certBitmap?.let { bmp ->
                            val uri = ExamCertificateGenerator.saveCertificateToDevice(
                                context,
                                bmp,
                                "NOOR_Cert_${track.id}_${difficulty.id}"
                            )
                            if (uri != null) {
                                Toast.makeText(context, "تم حفظ الشهادة في معرض الصور!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "حدث خطأ أثناء حفظ الصورة", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("حفظ الصورة", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Download PDF
                Button(
                    onClick = {
                        certBitmap?.let { bmp ->
                            val uri = ExamCertificateGenerator.exportCertificatePdf(
                                context,
                                bmp,
                                "NOOR_Cert_${track.id}_${difficulty.id}"
                            )
                            if (uri != null) {
                                Toast.makeText(context, "تم تصدير الشهادة كملف PDF!", Toast.LENGTH_LONG).show()
                                ExamCertificateGenerator.shareUri(context, uri, "application/pdf", t("عرض شهادة PDF", "View PDF Certificate"))
                            } else {
                                Toast.makeText(context, "حدث خطأ أثناء التصدير", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("تحميل PDF", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Share Button
            Button(
                onClick = {
                    certBitmap?.let { bmp ->
                        val uri = ExamCertificateGenerator.saveCertificateToDevice(
                            context,
                            bmp,
                            "NOOR_Share_Cert"
                        )
                        if (uri != null) {
                            ExamCertificateGenerator.shareUri(context, uri, "image/png", t("مشاركة شهادة نُور", "Share NOOR Certificate"))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold.copy(alpha = 0.25f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("مشاركة الشهادة مع الأهل والأصدقاء", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Edit Name Dialog
        if (showEditDialog) {
            var tempName by remember { mutableStateOf(userName) }
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("تعديل الاسم المطبوع", color = Gold, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("ادخل اسمك الكامل كما تحب أن يظهر على شهادة نُور:", color = TextMain, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = GoldSoft,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempName.isNotBlank()) {
                                userName = tempName.trim()
                                ExamStorage.setUserName(context, userName)
                            }
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) {
                        Text("حفظ", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("إلغاء", color = GoldSoft)
                    }
                },
                containerColor = NavyCard
            )
        }
    }
}