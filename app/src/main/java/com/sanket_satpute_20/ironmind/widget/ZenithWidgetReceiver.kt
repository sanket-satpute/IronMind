package com.sanket_satpute_20.ironmind.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Entry point for the Zenith Home Screen Widget.
 */
class ZenithWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ZenithWidget()
}
