package com.elhajri.noor.ads

/**
 * Central AdMob configuration — the REAL production identifiers
 * provided by the app owner (Mohamed Al-Hajari) from his AdMob account.
 * Keep every ad unit reference going through this file only.
 */
object AdIds {
    /** App-level AdMob identifier (with ~) — declared in AndroidManifest.xml. */
    const val APP_ID = "ca-app-pub-8701459895741919~8785357436"

    /** Banner ad unit — bottom of the main section screens only. */
    const val BANNER = "ca-app-pub-8701459895741919/7200068161"

    /** Interstitial ad unit — shown RARELY (max once every 15 minutes) between sections, never during playback. */
    const val INTERSTITIAL = "ca-app-pub-8701459895741919/1950601753"

    /** Rewarded ad unit — watched by choice to unlock perks/points. */
    const val REWARDED = "ca-app-pub-8701459895741919/3017280573"
}
