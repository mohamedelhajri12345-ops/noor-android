package com.elhajri.noor.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicLong

/**
 * Interstitial ads manager — AdMob.
 *
 * Anti-annoyance policy (owner requirement): the interstitial appears
 * RARELY and never while audio/video is playing:
 *  - minimum 15 minutes between two interstitials
 *  - never shown during Quran/nasheed playback or video watching
 *  - shown only on natural section transitions
 */
object InterstitialAds {

    private const val AD_UNIT = AdIds.INTERSTITIAL
    private const val MIN_INTERVAL_MS = 15 * 60 * 1000L   // 15 minutes

    @Volatile private var interstitialAd: InterstitialAd? = null
    @Volatile private var loading = false
    private val lastShownAt = AtomicLong(0L)

    fun isReady(): Boolean = interstitialAd != null

    fun preload(context: Context) {
        if (interstitialAd != null || loading) return
        loading = true
        InterstitialAd.load(
            context,
            AD_UNIT,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                        }
                        override fun onAdFailedToShow(adError: AdError) {
                            interstitialAd = null
                        }
                    }
                    loading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    loading = false
                }
            }
        )
    }

    /**
     * Shows the interstitial if enough time has passed since the previous one.
     * Safe to call at every section transition — internally rate-limited.
     * Returns true if an ad was actually displayed.
     */
    fun maybeShow(activity: Activity): Boolean {
        val now = System.currentTimeMillis()
        // Respect the 15-minute politeness window
        if (now - lastShownAt.get() < MIN_INTERVAL_MS) return false
        val ad = interstitialAd ?: return false
        lastShownAt.set(now)
        ad.show(activity)
        // preload the next one for later
        preload(activity)
        return true
    }
}
