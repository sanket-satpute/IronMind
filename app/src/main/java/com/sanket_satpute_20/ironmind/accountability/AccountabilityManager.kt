package com.sanket_satpute_20.ironmind.accountability

import android.content.Context
import android.telephony.SmsManager
import android.widget.Toast
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine

object AccountabilityManager {

    fun sendSkipAlert(context: Context, taskName: String) {
        val prefs = PrefManager.getInstance(context)
        val currentMode = AdaptiveEngine.getCurrentMode(prefs)
        if (!AdaptiveEngine.shouldSendAccountabilitySMS(currentMode)) return

        val partnerNumber = prefs.partnerPhone
        if (partnerNumber.isEmpty()) return

        val partnerName   = prefs.partnerName.ifEmpty { "your accountability partner" }
        val userName      = prefs.userName.ifEmpty { "Your friend" }

        val message = buildMessage(userName, taskName, partnerName)

        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(
                partnerNumber,
                null,
                message,
                null,
                null
            )

            Toast.makeText(
                context,
                "📱 Accountability SMS sent to $partnerName.",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            // Silent fail — don't let SMS failure block the app flow
        }
    }

    private fun buildMessage(userName: String, taskName: String, partnerName: String): String {
        return "$userName just skipped \"$taskName\".\n\n" +
               "They set up this accountability check themselves.\n" +
               "Please check in with them, $partnerName.\n\n" +
               "— IronMind"
    }

    fun isPartnerSet(context: Context): Boolean {
        val prefs = PrefManager.getInstance(context)
        return prefs.partnerPhone.isNotEmpty()
    }
}
