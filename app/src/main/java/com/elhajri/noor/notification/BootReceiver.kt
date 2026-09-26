package com.elhajri.noor.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-schedules the next Adhan alarm after the phone reboots, or right
 * after the app is updated — without this, all scheduled prayer-time
 * notifications would be silently lost on restart.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            AdhanScheduler.scheduleNextAdhan(context)
        }
    }
}
