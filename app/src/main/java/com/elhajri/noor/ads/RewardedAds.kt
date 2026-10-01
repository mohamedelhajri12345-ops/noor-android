package com.elhajri.noor.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

/**
 * مدير إعلانات المكافأة — AdMob (المزود الموجود أصلًا في المشروع).
 *
 * أمان المكافأة (متطلب جوهري):
 * 1) تُمنح النقاط فقط داخل رد نداء onUserEarnedReward الذي يطلقه SDK
 *    بعد إكمال الإعلان بشروط المزود — لا عند الضغط ولا عند الإغلاق المبكر.
 * 2) علم consumed لكل نسخة إعلان يمنح المكافأة مرة واحدة فقط مهما
 *    استُدعي رد النداء أكثر من مرة لنفس المشاهدة.
 * 3) أثناء التحميل/العرض يبقى الزر معطلاً فلا ضغط متكرر.
 *
 * الوحدة الحالية تجريبية رسمية من Google (اختبار آمن بلا أرباح).
 * قبل النشر الإنتاجي: استبدل AD_UNIT بمعرف الوحدة من حسابك في AdMob،
 * ومعرف التطقبيق في AndroidManifest.xml.
 */
object RewardedAds {

    // وحدة إعلانات مكافأة تجريبية رسمية — للاختبار فقط
    private const val AD_UNIT = "ca-app-pub-3940256099942544/5224358673"

    @Volatile private var rewardedAd: RewardedAd? = null
    @Volatile private var loading = false

    /** علم يمنح المكافأة مرة واحدة لكل نسخة إعلان محمّلة */
    private val rewardConsumed = AtomicBoolean(true)

    fun isReady(): Boolean = rewardedAd != null
    fun isLoading(): Boolean = loading

    /** تحميل مسبق — آمن للاستدعاء المتكرر (يتجاهل إن كان جاهزاً أو قيد التحميل). */
    fun preload(context: Context) {
        if (rewardedAd != null || loading) return
        loading = true
        RewardedAd.load(
            context,
            AD_UNIT,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    loading = false
                }
            }
        )
    }

    /**
     * عرض الإعلان.
     * onReward: تُستدعى مرة واحدة فقط إذا أكمل المستخدم الإعلان فعلاً حسب SDK.
     * onClosed: تُستدعى عند إغلاق الإعلان (مبكراً أو كاملاً) أو فشل العرض —
     *           بدون منح أي نقاط.
     */
    fun show(
        activity: Activity,
        onReward: () -> Unit,
        onClosed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            preload(activity)
            onClosed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                preload(activity)
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                preload(activity)
                onClosed()
            }
        }

        ad.show(activity) { _ ->
            // رد نداء SDK بعد استحقاق المكافأة — المصدر الوحيد للنقاط
            if (rewardConsumed.compareAndSet(false, true)) {
                onReward()
            }
        }
    }

    /** استهلاك الإعلان المحمّل — يُستدعى قبل العرض لضبط علم المنع المزدوج */
    fun prepareForShow() {
        rewardConsumed.set(false)
    }
}
