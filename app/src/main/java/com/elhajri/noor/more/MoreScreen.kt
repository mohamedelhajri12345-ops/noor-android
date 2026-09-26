package com.elhajri.noor.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard

private data class MoreEntry(val route: String, val title: String, val icon: ImageVector)
private data class MoreSection(val title: String, val entries: List<MoreEntry>)

private val sections = listOf(
    MoreSection("العبادات اليومية", listOf(
        MoreEntry("prayer", "مواقيت الصلاة", Icons.Filled.Schedule),
        MoreEntry("qibla", "اتجاه القبلة", Icons.Filled.LocationOn),
        MoreEntry("athkar", "الأذكار", Icons.Filled.WbSunny),
        MoreEntry("tasbih", "السبحة الإلكترونية", Icons.Filled.RadioButtonUnchecked),
        MoreEntry("tracker", "ورد القرآن اليومي", Icons.Filled.MenuBook),
        MoreEntry("calendar", "التقويم الهجري", Icons.Filled.Refresh)
    )),
    MoreSection("المعرفة والتزكية", listOf(
        MoreEntry("names", "أسماء الله الحسنى", Icons.Filled.Star),
        MoreEntry("stories", "قصص الأنبياء", Icons.Filled.NightsStay),
        MoreEntry("quiz", "الاختبار الديني", Icons.Filled.EmojiEvents),
        MoreEntry("hajj", "الحج والعمرة", Icons.Filled.AccountBalance),
        MoreEntry("zakat", "حاسبة الزكاة", Icons.Filled.Calculate)
    )),
    MoreSection("الوسائط والترفيه", listOf(
        MoreEntry("library", "مكتبة الأناشيد", Icons.Filled.MusicNote),
        MoreEntry("games", "الألعاب الإسلامية", Icons.Filled.Psychology),
        MoreEntry("ai", "المساعد الذكي", Icons.Filled.AutoAwesome),
        MoreEntry("journal", "المفكرة الروحية", Icons.Filled.EditNote)
    )),
    MoreSection("المجتمع والتطبيق", listOf(
        MoreEntry("community", "المجتمع", Icons.Filled.Person),
        MoreEntry("favorites", "المفضلة", Icons.Filled.Favorite),
        MoreEntry("notifications", "التنبيهات", Icons.Filled.Notifications),
        MoreEntry("donation", "التبرع ودعم المشروع", Icons.Filled.MonetizationOn),
        MoreEntry("login", "تسجيل الدخول", Icons.Filled.Lock),
        MoreEntry("privacy", "سياسة الخصوصية", Icons.Filled.Shield),
        MoreEntry("settings", "الإعدادات", Icons.Filled.Settings)
    ))
)

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Text(
                "المزيد",
                color = Gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 14.dp)
            )
        }
        sections.forEach { section ->
            item(key = "header_" + section.title) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .background(Gold, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(section.title, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            section.entries.forEach { entry ->
                item(key = entry.route) {
                    Surface(
                        color = NavyCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                com.elhajri.noor.audio.SoundEffects.click()
                                onNavigate(entry.route)
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Gold.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    entry.icon,
                                    contentDescription = entry.title,
                                    tint = Gold,
                                    modifier = Modifier.padding(8.dp).size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(entry.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.weight(1f))
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = GoldSoft.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
