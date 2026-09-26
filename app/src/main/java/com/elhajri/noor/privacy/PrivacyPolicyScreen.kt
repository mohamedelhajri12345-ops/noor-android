package com.elhajri.noor.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain

data class PrivacySection(val num: String, val title: String, val body: String)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit = {}) {
    val sections = listOf(
        PrivacySection(
            num = "١",
            title = "مقدمة",
            body = "تطبيق \"القرآن الكريم\" هو تطبيق إسلامي مجاني يهدف إلى تقديم محتوى ديني موثوق. نلتزم بحماية خصوصيتك."
        ),
        PrivacySection(
            num = "٢",
            title = "المعلومات التي نجمعها",
            body = "حساب المستخدم: بريدك الإلكتروني (لتسجيل الدخول والمصادقة عبر رمز تحقق OTP). ملف المجتمع: الاسم المستعار (المعرّف الفريد) والصورة الشخصية (اختياري). الرسائل: نصوص الرسائل والملفات الصوتية والصور المرسلة في محادثات المجتمع. معلومات الاستخدام: عدد مرات فتح التطبيق والصفحات المزارة (للتحسين فقط). موقعك الجغرافي: فقط عند طلبك حساب مواقيت الصلاة واتجاه القبلة (اختياري). بيانات الإشعارات: لتفعيل تذكيرات الصلاة والأذكار (اختياري)."
        ),
        PrivacySection(
            num = "٣",
            title = "المعلومات التي لا نجمعها",
            body = "لا نطلب رقم هاتفك أو عنوانك البريدي. لا نبيع بياناتك لأي طرف ثالث. لا نتتبع نشاطك خارج التطبيق. رسائلك في المجتمع مرئية فقط لأعضاء المحادثة، ولا يمكن لأي شخص خارجها الاطلاع عليها."
        ),
        PrivacySection(
            num = "٤",
            title = "الإعلانات",
            body = "نستخدم إعلانات من Start.io لتمويل التطبيق. قد تجمع شبكات الإعلانات بعض البيانات لتحسين الإعلانات. يمكنك التحكم في إعدادات الإعلانات من إعدادات هاتفك."
        ),
        PrivacySection(
            num = "٥",
            title = "الإشعارات",
            body = "إشعارات الصلاة والأذكار تعمل عبر OneSignal. لا نشارك بياناتك مع أطراف ثالثة."
        ),
        PrivacySection(
            num = "٦",
            title = "المحتوى",
            body = "القرآن الكريم من مصادر موثوقة (mp3quran.net). الأذكار والأحاديث من مصادر صحيحة. الأناشيد من مصادر مجانية (archive.org)."
        ),
        PrivacySection(
            num = "٧",
            title = "التبرعات",
            body = "التبرعات طوعية وتذهب لصيانة التطبيق. بياناتك البنكية لا تمر عبر التطبيق."
        ),
        PrivacySection(
            num = "٨",
            title = "حقوقك",
            body = "يمكنك حذف التطبيق في أي وقت. يمكنك تعطيل الإشعارات من الإعدادات. يمكنك استخدام التطبيق بدون إنترنت (معظم الميزات)."
        ),
        PrivacySection(
            num = "٩",
            title = "التواصل والتعديلات",
            body = "قد نحدث هذه السياسة من وقت لآخر. آخر تحديث: 2026."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(16.dp)
    ) {
        // Top Back Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCard)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = Gold
                )
            }
        }

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Gold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "سياسة الخصوصية",
                color = Gold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "تطبيق القرآن الكريم — آخر تحديث: 2026",
                color = GoldSoft.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Sections
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sections) { s ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = s.num,
                                    color = Gold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = s.title,
                                color = TextMain,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = s.body,
                            color = GoldSoft.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(start = 38.dp)
                        )
                    }
                }
            }
        }
    }
}
