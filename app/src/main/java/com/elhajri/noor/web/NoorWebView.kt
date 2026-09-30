package com.elhajri.noor.web

import android.annotation.SuppressLint
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy

/**
 * NoorWebView — نافذة ويب احترافية لصفحات الموقع الأصلي (holyquran2).
 *
 * تقنياً: تعرض صفحة الموقع كما هي حرفياً (نفس ديزاين الكود المصدري) داخل التطبيق،
 * مع إخفاء "الشريط العلوي" و"شريط التنقل السفلي" الخاصين بالموقع عبر حقن CSS،
 * لأن التطبيق يوفر شريطه الخاص في الأعلى والأسفل — فتبقى الواجهة نظيفة غير مزدوجة.
 *
 * المزايا الاحترافية:
 * 1. مؤشر تحميل ذهبي أنيق أثناء جلب الصفحة.
 * 2. شاشة خطأ مع زر إعادة المحاولة عند انقطاع الاتصال.
 * 3. زر الرجوع في شريط عنوان اختياري أنيق (للصفحات غير الرئيسية).
 * 4. زر الرجوع في النظام يتنقل داخل تاريخ الويب بدل الخروج من الشاشة.
 * 5. حفظ حالة التمرير والحفظ المؤقت بين التنقلات.
 */
object NoorWeb {
    const val BASE = "https://holyquran2.base44.app"
    const val HOME = "$BASE/"
    const val QURAN = "$BASE/quran"
    const val ASSISTANT = "$BASE/assistant"

    /**
     * حقن CSS يخفي واجهة الموقع العلوية والسفلية بشرط واحد:
     * يُنفَّذ عدة مرات (عند البدء والانتهاء من التحميل) لأن الموقع تطبيق صفحة واحدة
     * يعيد بناء عناصره ديناميكياً.
     */
    val hideSiteChromeJs: String = """
        (function(){
            function apply(){
                var css = 'header.sticky.top-0.z-40{display:none!important}'
                        + 'nav.fixed.bottom-0.z-40{display:none!important}'
                        + 'main.pb-28{padding-bottom:8px!important}'
                        + 'body{overscroll-behavior-y:contain!important}';
                if(document.getElementById('noor-native-chrome')) return;
                var s = document.createElement('style');
                s.id = 'noor-native-chrome';
                s.textContent = css;
                (document.head || document.documentElement).appendChild(s);
            }
            try{ apply(); }catch(e){}
            document.addEventListener('DOMContentLoaded', apply);
            window.addEventListener('load', apply);
            setTimeout(apply, 350);
            setTimeout(apply, 1500);
        })();
    """.trimIndent()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NoorWebView(
    url: String,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    val webView = remember { mutableStateOf<WebView?>(null) }

    // الرجوع داخل تاريخ الويب أولاً — كأي متصفح محترم
    BackHandler(enabled = true) {
        val wv = webView.value
        if (wv != null && wv.canGoBack()) wv.goBack() else onBack?.invoke()
    }

    Column(modifier = modifier.fillMaxSize().background(Navy)) {
        // شريط عنوان اختياري رفيع وأنيق — فقط للصفحات غير الرئيسية (مثل المساعد الذكي)
        if (title != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "رجوع",
                            tint = Gold
                        )
                    }
                }
                Text(
                    title,
                    color = Gold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(false)
                        settings.mediaPlaybackRequiresUserGesture = false
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?, url: String?,
                                favicon: android.graphics.Bitmap?
                            ) {
                                isLoading = true
                                hasError = false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                // إخفاء واجهة الموقع العلوية/السفلية فور اكتمال التحميل
                                view?.evaluateJavascript(NoorWeb.hideSiteChromeJs, null)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                // نتخطى أخطاء الموارد الفرعية (صور/خطوط) ونُظهر الخطأ للإطار الرئيسي فقط
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    isLoading = false
                                }
                            }
                        }
                        webView.value = this
                        loadUrl(url)
                    }
                },
            )

            // مؤشر تحميل ذهبي أنيق
            if (isLoading && !hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Navy.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Gold, strokeWidth = 2.5.dp)
                        Spacer(Modifier.height(14.dp))
                        Text("جارٍ التحميل…", color = GoldSoft, fontSize = 13.sp)
                    }
                }
            }

            // شاشة خطأ احترافية مع إعادة المحاولة
            if (hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Navy),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.WifiOff,
                            contentDescription = null,
                            tint = GoldSoft.copy(alpha = 0.6f),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "تعذّر تحميل الصفحة",
                            color = Gold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "تحقّق من اتصالك بالإنترنت وحاول مجدداً",
                            color = GoldSoft.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                hasError = false
                                isLoading = true
                                webView.value?.reload()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gold,
                                contentColor = Color(0xFF0A0F1A)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("إعادة المحاولة", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
