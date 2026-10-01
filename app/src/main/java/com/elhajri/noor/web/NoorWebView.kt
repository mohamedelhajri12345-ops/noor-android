package com.elhajri.noor.web

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.alpha
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import kotlinx.coroutines.delay

/**
 * NoorWebView — نافذة ويب احترافية لصفحات الموقع الأصلي (holyquran2).
 *
 * تُعرض صفحة الموقع كما هي حرفياً (نفس ديزاين الكود المصدري) داخل التطبيق،
 * لكن بطريقة "جزء من التطبيق" وليس متصفحاً:
 *
 * 1. تُقطع لحظة صفحة الهبوط/السبلاش الخاصة بالموقع كلياً: غطاء تحميل أصلي معتم
 *    يغطي الشاشة حتى يكتمل التحميل ويُطبَّق إخفاء السبلاش، ثم يتلاشى بنعمة.
 * 2. تُخفى واجهة الموقع العلوية والسفلية وأشرطته عبر حقن CSS، لأن التطبيق
 *    يوفر شريطه الخاص — فلا توجد أشرطة مزدوجة.
 * 3. تُخفى عناصر "الموقع-في-المتصفح": إشعار "تفعيل الإشعارات"، إعلان حالة
 *    الاتصال، أشرطة التمرير، وهالة لمس الروابط — فيبدو محتوى الويب أصيلاً.
 * 4. زر الرجوع في النظام يتنقل داخل تاريخ الصفحات كأي تطبيق محترم.
 * 5. شاشة خطأ أنيقة مع زر إعادة المحاولة عند انقطاع الاتصال.
 */
object NoorWeb {
    const val BASE = "https://holyquran2.base44.app"
    const val HOME = "$BASE/"
    const val QURAN = "$BASE/quran"
    const val ASSISTANT = "$BASE/assistant"
    const val LIBRARY = "$BASE/library"

    /**
     * تعليق مشغّل الصوت: يستمع لأحداث عناصر <audio>/<video> داخل صفحة الموقع
     * ويرسلها إلى الجسر الأصيل (NoorAudioBridge) لكي تظهر الإشعارات
     * وتُستكمل التلاوة أصلياً عند إغلاق التطبيق.
     * يعمل مع أي عنصر صوتي جديد يُنشأ ديناميكياً (مشغل القرآن والأناشيد).
     */
    val audioHooksJs: String = """
        (function(){
            if(window.__noorAudioHooked) return;
            window.__noorAudioHooked = true;
            function safe(f){ try{ f(); }catch(e){} }
            function pageAudio(){ return document.querySelector('audio,video'); }
            function hook(el){
                if(!el || el.__noorHooked) return;
                el.__noorHooked = true;
                var last = 0;
                el.addEventListener('play', function(){
                    safe(function(){
                        var a = pageAudio() || el;
                        if(window.NoorAudio) window.NoorAudio.onPlay(
                            a.currentSrc || a.src || '', a.currentTime,
                            (document.title || 'القرآن الكريم').substring(0, 60)
                        );
                    });
                });
                el.addEventListener('timeupdate', function(){
                    var now = Date.now();
                    if(now - last > 800){
                        last = now;
                        safe(function(){
                            var a = pageAudio() || el;
                            if(window.NoorAudio) window.NoorAudio.onTime(a.currentTime);
                        });
                    }
                });
                el.addEventListener('pause', function(){
                    safe(function(){ if(window.NoorAudio) window.NoorAudio.onPause(); });
                });
                el.addEventListener('ended', function(){
                    safe(function(){ if(window.NoorAudio) window.NoorAudio.onEnded(); });
                });
            }
            function scan(){ safe(function(){
                document.querySelectorAll('audio,video').forEach(hook);
            }); }
            scan();
            setInterval(scan, 1500);
        })();
    """.trimIndent()

    /**
     * حقن CSS: يُطبَّق مرات عدة (عند أول ظهور للصفحة وعند اكتمال التحميل)
     * لأن الموقع تطبيق صفحة واحدة يعيد بناء عناصره ديناميكياً.
     */
    val hideSiteChromeJs: String = """
        (function(){
            function apply(){
                if(document.getElementById('noor-native-chrome')) return;
                var css = 'header.sticky.top-0.z-40{display:none!important}'
                        + 'nav.fixed.bottom-0.z-40{display:none!important}'
                        + 'main.pb-28{padding-bottom:8px!important}'
                        // سبلاش الموقع — تُقطع كلياً: التطبيق يعرض شاشة تحميله الخاصة
                        + 'div.fixed.inset-0.z-50.flex.flex-col.items-center.justify-center{display:none!important}'
                        // عناصر "الموقع-في-المتصفح" التي لا معنى لها داخل التطبيق
                        + 'div.fixed.bottom-20.z-50{display:none!important}'
                        + 'div.fixed.top-16.z-50{display:none!important}'
                        // مظهر أصيل: بدون أشرطة تمرير ولا هالة لمس
                        + '::-webkit-scrollbar{display:none!important}'
                        + 'html{overscroll-behavior:none;-webkit-tap-highlight-color:transparent!important}'
                        + 'body{overscroll-behavior:none;-webkit-tap-highlight-color:transparent!important}';
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
    var settling by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    val webView = remember { mutableStateOf<WebView?>(null) }

    // الرجوع داخل تاريخ الويب أولاً — كأي تطبيق محترم
    BackHandler(enabled = true) {
        val wv = webView.value
        if (wv != null && wv.canGoBack()) wv.goBack() else onBack?.invoke()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val urlForCleanup = url
    androidx.compose.runtime.DisposableEffect(urlForCleanup) {
        onDispose {
            // أُغلقت الشاشة: إن كانت التلاوة تعمل داخل الويب فيو فالتقطها أصلياً
            // لكي تستمر حتى بعد إغلاق التطبيق كاملاً — بإذن الله
            webView.value?.let { WebPlayerBus.unbind(it) }
            if (WebPlayerBus.isPlaying && !WebPlayerBus.url.isNullOrBlank()) {
                WebPlayerService.takeover(context)
            }
            // إعادة الصفحة إلى المجمع — فتح لحظي في المرة القادمة
            WebViewPool.release(urlForCleanup, webView.value)
        }
    }

    // بعد اكتمال التحميل: مهلة قصيرة ليرتّب SPA صفحته ويُطبَّق إخفاء السبلاش،
    // ثم يتلاشى غطاء التحميل بنعمة — فلا تظهر لحظة صفحة هبوط أبداً
    LaunchedEffect(settling) {
        if (settling) {
            delay(700)
            settling = false
        }
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isLoading || settling) 1f else 0f,
        animationSpec = tween(350),
        label = "overlayFade"
    )

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
            val loadListener = remember { object : WebViewPool.LoadListener {
                override fun onStarted() { isLoading = true; hasError = false; settling = false }
                override fun onFinished() { isLoading = false; settling = true }
                override fun onError() { hasError = true; isLoading = false; settling = false }
            } }
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    // فتح لحظي: صفحة مجهزة مسبقاً من المجمع إن وُجدت
                    val (wv, complete) = WebViewPool.acquire(ctx, url, loadListener)
                    webView.value = wv
                    if (complete) {
                        isLoading = false; settling = false
                    } else if (!WebViewPool.isOnline(ctx) && WebViewPool.loadOffline(ctx, wv, url)) {
                        // دون إنترنت: تفتح اللقطة المحفوظة للصفحة فوراً
                        isLoading = true
                    } else {
                        isLoading = true
                    }
                    wv
                }
            )

            // غطاء التحميل الأصلي المعتم — يقطع صفحة الهبوط الخاصة بالموقع كلياً
            if (overlayAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(overlayAlpha)
                        .background(Navy),
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
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
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
                                settling = false
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

/**
 * NoorAudioBridge — جسر JS→أصيل: يستقبل أحداث مشغّل الموقع
 * ويحدّث حالة WebPlayerBus ويشغّل خدمة الإشعارات.
 * تُستدعى دواله من صفحة الموقع عبر window.NoorAudio.
 */
class NoorAudioBridge(
    private val context: android.content.Context
) {
    @android.webkit.JavascriptInterface
    fun onPlay(url: String, positionSec: Float, title: String) {
        WebPlayerBus.url = url
        WebPlayerBus.positionSec = positionSec
        WebPlayerBus.title = title
        WebPlayerBus.isPlaying = true
        WebPlayerService.sync(context)
    }

    @android.webkit.JavascriptInterface
    fun onTime(positionSec: Float) {
        WebPlayerBus.positionSec = positionSec
    }

    @android.webkit.JavascriptInterface
    fun onPause() {
        WebPlayerBus.isPlaying = false
        WebPlayerService.sync(context)
    }

    @android.webkit.JavascriptInterface
    fun onEnded() {
        WebPlayerBus.isPlaying = false
        WebPlayerBus.positionSec = 0f
        WebPlayerService.stop(context)
    }
}
