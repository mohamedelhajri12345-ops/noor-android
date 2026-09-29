package com.elhajri.noor.ai

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.Navy

/**
 * المساعد الذكي — صفحة المساعد من التطبيق الأصلي المنشور (holyquran2)
 * داخل WebView، بناءً على طلب المالك: نفس مساعد الكود المصدري حرفياً.
 */
private const val ASSISTANT_URL = "https://holyquran2.base44.app/assistant"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AIAssistantScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Navy)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronRight, contentDescription = "رجوع", tint = Gold)
            }
            Text("المساعد الذكي", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(ASSISTANT_URL)
                }
            }
        )
    }
}
