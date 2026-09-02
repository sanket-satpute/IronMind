package com.sanket_satpute_20.ironmind.detox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sanket_satpute_20.ironmind.data.PrefManager

class DetoxReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("DETOX_ACTION") ?: return
        val prefs = PrefManager.getInstance(context)

        when (action) {
            "START" -> prefs.detoxCurrentlyActive = true
            "END"   -> prefs.detoxCurrentlyActive = false
        }
    }
}