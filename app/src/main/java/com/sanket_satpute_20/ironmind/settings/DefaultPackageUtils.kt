package com.sanket_satpute_20.ironmind.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager

fun getDefaultDialerPackage(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        tm?.defaultDialerPackage.orEmpty()
    } else {
        val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:") }
        context.packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName.orEmpty()
    }
}

fun getDefaultSmsPackage(context: Context): String {
    return android.provider.Telephony.Sms.getDefaultSmsPackage(context).orEmpty()
}
