package com.elhajri.noor.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * إعلان بانر AdMob — بمعرف الوحدة التجريبي الرسمي من Google.
 * مكان آمن: أسفل شاشة الأقسام فقط، ضمن التمرير — لا يغطي محتوى
 * القرآن ولا الأزرار ولا يعطل التلاوة.
 *
 * قبل النشر الإنتاجي: استبدل معرّف الوحدة (ومعرّف التطبيق في المانيفست)
 * بمعرّفاتك الحقيقية من حسابك في AdMob.
 */
@Composable
fun NoorBannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth().height(52.dp).padding(top = 4.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                // وحدة إعلانية تجريبية رسمية — بنرات اختبار فقط بلا أرباح
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
