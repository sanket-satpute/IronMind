package com.sanket_satpute_20.ironmind.integrity

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.psychology.AdaptiveEngine
import com.sanket_satpute_20.ironmind.ui.components.ShieldMode
import kotlinx.coroutines.*

class IntegrityShieldWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = PrefManager.getInstance(context)
        val integrity = (prefs.lastKnownIntegrityLevel * 100).toInt()
        val mode = when (AdaptiveEngine.getCurrentMode(prefs).name) {
            ShieldMode.IRON.name -> "IRON MODE"
            ShieldMode.RECOVERY.name -> "RECOVERY MODE"
            ShieldMode.EXPERIMENT.name -> "EXPERIMENT MODE"
            else -> "BUILD MODE"
        }

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_integrity_shield)
            views.setTextViewText(R.id.widget_integrity_text, "$integrity%")
            views.setTextViewText(R.id.widget_mode_text, mode)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_SHIELD) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, IntegrityShieldWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE_SHIELD = "com.ironmind.ACTION_UPDATE_SHIELD_WIDGET"
    }
}
