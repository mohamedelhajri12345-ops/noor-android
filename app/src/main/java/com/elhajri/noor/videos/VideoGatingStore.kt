package com.elhajri.noor.videos

import android.app.Activity
import android.content.Context
import com.elhajri.noor.ads.RewardedAds
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.theme.ThemeStore

/**
 * Rewarded Gating Store for Kids Video Library.
 * Handles free daily video limits (3 free videos/day) and integrates with existing
 * RewardedAds and ThemeStore points system for unlocks.
 */
object VideoGatingStore {

    /**
     * FREE TEST MODE: when true, ALL videos are free to watch (no ads, no points).
     * Set back to false to re-enable rewarded-ads/points gating after final testing.
     */
    const val FREE_TEST_MODE = true

    const val FREE_DAILY_LIMIT = 3
    const val POINTS_UNLOCK_COST = 50

    private val sessionUnlockedVideos = mutableSetOf<String>()

    /**
     * Determines whether a video should be gated behind rewarded ads or points.
     * Returns true if user has reached or exceeded the 3 free daily videos limit
     * and the specific video has not been unlocked in the current session.
     */
    fun shouldGateVideo(context: Context, videoId: String? = null): Boolean {
        if (FREE_TEST_MODE) return false
        if (videoId != null && sessionUnlockedVideos.contains(videoId)) {
            return false
        }
        val watchedToday = Prefs.getDailyWatchedVideosCount(context)
        val gated = watchedToday >= FREE_DAILY_LIMIT
        // TODO: Gating hook active. Connected to Prefs daily counter, RewardedAds & ThemeStore points.
        return gated
    }

    /**
     * Records a video playback session, incrementing the daily watched count.
     */
    fun recordVideoWatched(context: Context, videoId: String) {
        Prefs.incrementDailyWatchedVideosCount(context)
        sessionUnlockedVideos.add(videoId)
    }

    /**
     * Unlocks video access by showing a rewarded ad via AdMob (RewardedAds).
     */
    fun unlockWithAd(activity: Activity, videoId: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        RewardedAds.prepareForShow()
        if (RewardedAds.isReady()) {
            RewardedAds.show(
                activity = activity,
                onReward = {
                    sessionUnlockedVideos.add(videoId)
                    onSuccess()
                },
                onClosed = {
                    if (sessionUnlockedVideos.contains(videoId)) {
                        onSuccess()
                    } else {
                        onFailure()
                    }
                }
            )
        } else {
            // Preload for next time if not loaded yet
            RewardedAds.preload(activity)
            // Allow temporary pass or failure based on UX requirement
            sessionUnlockedVideos.add(videoId)
            onSuccess()
        }
    }

    /**
     * Unlocks video access by spending points from ThemeStore.
     */
    fun unlockWithPoints(context: Context, videoId: String, cost: Int = POINTS_UNLOCK_COST): Boolean {
        val success = ThemeStore.spendPoints(context, cost)
        if (success) {
            sessionUnlockedVideos.add(videoId)
        }
        return success
    }

    /**
     * Helper to mark a video as unlocked for the current session.
     */
    fun markUnlocked(videoId: String) {
        sessionUnlockedVideos.add(videoId)
    }
}
