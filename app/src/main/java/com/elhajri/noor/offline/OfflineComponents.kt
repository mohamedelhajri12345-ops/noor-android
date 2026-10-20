package com.elhajri.noor.offline

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.settings.t
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.NavyCard

/**
 * Small gold badge shown on cards/items available offline locally.
 */
@Composable
fun OfflineGoldBadge(
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Gold.copy(alpha = 0.18f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.OfflinePin,
                contentDescription = t("متاح بدون إنترنت", "Available Offline"),
                tint = Gold,
                modifier = Modifier.size(13.dp)
            )
            if (showText) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = t("بدون إنترنت", "Offline"),
                    color = Gold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Friendly Arabic dialog shown when content requires internet connection but device is offline.
 */
@Composable
fun OfflineNoticeDialog(
    itemTitle: String? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        titleContentColor = Gold,
        iconContentColor = Gold,
        icon = {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = t("تنبيه الاتصال بالإنترنت", "Offline Notice"),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Gold
            )
        },
        text = {
            val titleText = if (!itemTitle.isNullOrBlank()) " \"$itemTitle\"" else ""
            Text(
                text = t(
                    "أنت غير متصل بالإنترنت حالياً. يتطلب تشغيل$titleText اتصالاً بالشبكة أو تنزيله مسبقاً للاستماع بدون إنترنت.",
                    "You are currently offline. Playing$titleText requires an internet connection or downloading it beforehand."
                ),
                color = GoldSoft,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = t("حسناً، فهمت", "OK, got it"),
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

fun showOfflineNoticeToast(context: Context, itemTitle: String? = null) {
    val titleText = if (!itemTitle.isNullOrBlank()) " \"$itemTitle\"" else ""
    val msg = t(
        "أنت غير متصل بالإنترنت. يلزم الاتصال لتشغيل$titleText أو قم بتنزيله للاستماع بدون إنترنت.",
        "You are offline. Internet connection is required for$titleText or download it for offline playback."
    )
    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}
