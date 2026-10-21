package com.elhajri.noor.privacy

import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t
import com.elhajri.noor.settings.toArabicDigits


import com.elhajri.noor.ui.themeScreenBackground
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
import com.elhajri.noor.ui.NoorGradients
import androidx.compose.ui.graphics.Color
import com.elhajri.noor.theme.noorGlassCard

data class PrivacySection(val num: String, val title: String, val body: String)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit = {}) {
    val sections = listOf(
        PrivacySection(
            num = t("١", "1"),
            title = t("مقدمة", "Introduction"),
            body = t("تطبيق \"القرآن الكريم\" هو تطبيق إسلامي مجاني يهدف إلى تقديم محتوى ديني موثوق. نلتزم بحماية خصوصيتك.", "NOOR is a free Islamic app providing authentic religious content. We are committed to protecting your privacy.")
        ),
        PrivacySection(
            num = t("٢", "2"),
            title = t("المعلومات التي نجمعها", "Information We Collect"),
            body = t("ميزات التطبيق كلها تعمل بدون حساب إطلاقاً. الحساب اختياري ولأعضاء المجتمع الحاليين فقط: بريدك الإلكتروني عند تسجيل الدخول، الاسم المستعار والصورة الشخصية (اختياري)، ونصوص الرسائل والملفات المرسلة في محادثات المجتمع. (لا يوجد تسجيل دخول بحساب Google، ولا إنشاء حسابات جديدة داخل التطبيق.) معلومات الاستخدام: عدد مرات فتح التطبيق والصفحات المزارة (للتحسين فقط). موقعك الجغرافي: فقط عند طلبك حساب مواقيت الصلاة واتجاه القبلة (اختياري). بيانات الإشعارات: لتفعيل تذكيرات الصلاة والأذكار (اختياري).", "All app features work without an account. Account creation is optional: email address, optional avatar/username, and community chat messages. Usage info is collected anonymously for app improvement. Location data is used optionally for prayer times and Qibla. Notifications data is used for prayer/dhikr alerts.")
        ),
        PrivacySection(
            num = t("٣", "3"),
            title = t("المعلومات التي لا نجمعها", "Information We Do Not Collect"),
            body = t("لا نطلب رقم هاتفك أو عنوانك البريدي. لا نبيع بياناتك لأي طرف ثالث. لا نتتبع نشاطك خارج التطبيق. رسائلك في المجتمع مرئية فقط لأعضاء المحادثة، ولا يمكن لأي شخص خارجها الاطلاع عليها.", "We do not ask for your phone number or physical address. We do not sell your data to third parties or track your activity outside the app. Community messages are visible only to conversation participants.")
        ),
        PrivacySection(
            num = t("٤", "4"),
            title = t("الإعلانات", "Advertising"),
            body = t("نستخدم إعلانات Google AdMob لتمويل التطبيق وصيانته (بانر، بيني بين الأقسام بشكل نادر، وإعلانات بمكافأة اختيارية). تستخدم Google معرّف الإعلانات على جهازك (Advertising ID) لعرض إعلانات ملائمة. يمكنك إيقاف تخصيص الإعلانات من: الإعدادات > Google > الإعلانات، أو إعادة تعيين معرّف الإعلانات في أي وقت. سياسة خصوصية Google متاحة على: policies.google.com/privacy", "We use Google AdMob ads to support app maintenance (banners, occasional interstitials, optional rewarded ads). Google uses your Advertising ID to serve relevant ads. You can disable personalized ads in device settings. Google Privacy Policy is available at policies.google.com/privacy.")
        ),
        PrivacySection(
            num = t("٥", "5"),
            title = t("الربط بيوتيوب وفتح المحتوى الخارجي", "YouTube Integration & External Content"),
            body = t("بعض الفيديوهات قد تتطلب تسجيل دخول يوتيوب. عند الضغط على زر \"افتح في تطبيق يوتيوب\"، نطلب منك إذناً واضحاً داخل نُور قبل أول استخدام، ثم نفتح الفيديو في تطبيق يوتيوب المثبت على جهازك ليعمل بحسابك المسجّل فيه مباشرة. نُور لا يصل إلى حسابك على يوتيوب، ولا يجمع أو يشارك بياناتك معه — استخدام حسابك يبقى بينك وبين تطبيق يوتيوب وفق سياسة خصوصية يوتيوب. يمكنك دائماً اختيار المتصفح أو الفيديوهات المباشرة داخل التطبيق بدلاً من ذلك.", "Some videos open in the official YouTube app. NOOR asks for clear consent before launching external links. NOOR does not access, collect, or share your YouTube account data.")
        ),
        PrivacySection(
            num = t("٦", "6"),
            title = t("الإشعارات", "Notifications"),
            body = t("إشعارات الصلاة والأذكار تعمل عبر OneSignal. لا نشارك بياناتك مع أطراف ثالثة.", "Prayer and Athkar notifications operate via OneSignal. We do not share your data with third parties.")
        ),
        PrivacySection(
            num = t("٧", "7"),
            title = t("المحتوى", "Content"),
            body = t("القرآن الكريم من مصادر موثوقة (mp3quran.net). الأذكار والأحاديث من مصادر صحيحة. الأناشيد من مصادر مجانية (archive.org).", "The Holy Qur'an is sourced from mp3quran.net. Athkar and Hadiths are from verified authentic sources. Nasheeds are from free open repositories (archive.org).")
        ),
        PrivacySection(
            num = t("٨", "8"),
            title = t("التبرعات", "Donations"),
            body = t("التبرعات طوعية وتذهب لصيانة التطبيق. بياناتك البنكية لا تمر عبر التطبيق.", "Donations are voluntary to support app development and maintenance. Financial data is never processed or stored by NOOR.")
        ),
        PrivacySection(
            num = t("٩", "9"),
            title = t("حقوقك", "Your Rights"),
            body = t("يمكنك حذف التطبيق في أي وقت. يمكنك تعطيل الإشعارات من الإعدادات. يمكنك استخدام التطبيق بدون إنترنت (معظم الميزات).", "You can uninstall the app or disable notifications at any time. You can use most features fully offline.")
        ),
        PrivacySection(
            num = t("١٠", "10"),
            title = t("التواصل والتعديلات", "Contact & Updates"),
            body = t("قد نحدث هذه السياسة من وقت لآخر. آخر تحديث: 2026.", "We may update this policy periodically. Last updated: 2026.")
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
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
                    contentDescription = t("رجوع", "Back"),
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
                text = t("سياسة الخصوصية", "Privacy Policy"),
                color = Gold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = t("تطبيق القرآن الكريم — آخر تحديث: 2026", "NOOR App — Last Updated: 2026"),
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
                    modifier = Modifier.fillMaxWidth().noorGlassCard(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
