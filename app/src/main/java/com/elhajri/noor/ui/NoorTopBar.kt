package com.elhajri.noor.ui

import com.elhajri.noor.settings.t

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The NOOR top bar:
 * Crescent logo + app title on one side, gold action buttons on the other.
 * Donation (التبرع) entry removed per owner requirement.
 */
@Composable
fun NoorTopBar(
    onCommunity: () -> Unit,
    onDonate: () -> Unit = {}, // Retained with empty default for backward compatibility
    onAssistant: () -> Unit,
    onSettings: () -> Unit,
    onProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    Surface(
        color = Navy.copy(alpha = 0.85f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo: crescent moon + title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(com.elhajri.noor.R.mipmap.ic_launcher),
                        contentDescription = t("أيقونة التطبيق", "App Icon"),
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = t("القرآن الكريم", "The Holy Qur'an"),
                        color = Gold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AmiriFamily
                    )
                }

                // Action buttons — Donation removed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TopBarAction(icon = { Icon(Icons.Filled.Groups, contentDescription = t("المجتمع", "Community"), tint = Gold, modifier = Modifier.size(18.dp)) }, onClick = onCommunity)
                    TopBarAction(icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = t("واتساب", "WhatsApp"), tint = Gold, modifier = Modifier.size(18.dp)) }) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/212726626546")))
                    }
                    TopBarAction(icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = t("المساعد الذكي", "Smart Assistant"), tint = Gold, modifier = Modifier.size(18.dp)) }, onClick = onAssistant)
                    TopBarAction(icon = { Icon(Icons.Filled.Person, contentDescription = t("ملفي الشخصي", "My Profile"), tint = Gold, modifier = Modifier.size(18.dp)) }, onClick = onProfile)
                    TopBarAction(icon = { Icon(Icons.Filled.Settings, contentDescription = t("الإعدادات", "Settings"), tint = Gold, modifier = Modifier.size(18.dp)) }, onClick = onSettings)
                }
            }
            // hairline gold separator
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Gold.copy(alpha = 0.12f))
            )
        }
    }
}

@Composable
private fun TopBarAction(icon: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
