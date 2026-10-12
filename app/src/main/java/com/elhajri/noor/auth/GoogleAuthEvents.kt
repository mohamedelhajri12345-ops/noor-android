package com.elhajri.noor.auth

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Simple one-shot signal so auth screens can react when the Google OAuth
 * deep link (noor://auth/callback) lands while they are on screen.
 */
object GoogleAuthEvents {
    var succeeded by mutableStateOf(false)
        private set

    fun notifySuccess() {
        succeeded = true
    }

    fun consume(): Boolean {
        val v = succeeded
        if (v) succeeded = false
        return v
    }
}
