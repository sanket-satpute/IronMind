package com.sanket_satpute_20.ironmind.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize as composeFillMaxSize
import androidx.compose.foundation.layout.height as composeHeight
import androidx.compose.foundation.layout.padding as composePadding
import androidx.compose.ui.Modifier as ComposeModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.sanket_satpute_20.ironmind.MainActivity
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen

/**
 * The visual implementation of the Zenith Home Screen Widget.
 */
class ZenithWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = PrefManager.getInstance(context)
            val taskName = prefs.activeTaskName
            val endTime = prefs.activeTaskEndTime
            val openAppAction = actionStartActivity(Intent(context, MainActivity::class.java))
            
            GlanceTheme {
                WidgetContent(taskName, endTime, openAppAction)
            }
        }
    }

    @Composable
    private fun WidgetContent(taskName: String, endTime: String, openAppAction: Action) {
        val hasActiveTask = taskName.isNotEmpty()
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(12.dp)
                .clickable(openAppAction),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            if (hasActiveTask) {
                Text(
                    text = "CURRENT MISSION",
                    style = TextStyle(
                        color = ColorProvider(Color.Gray),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(GlanceModifier.height(8.dp))
                
                Text(
                    text = taskName.uppercase(),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
                
                Spacer(GlanceModifier.height(8.dp))
                
                val remaining = calculateRemainingMinutes(endTime)
                Text(
                    text = if (remaining > 0) "$remaining mins left" else "Securing integrity...",
                    style = TextStyle(
                        color = ColorProvider(SurfaceElevated), // SuccessGreen
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            } else {
                Text(
                    text = "NO ACTIVE MISSION",
                    style = TextStyle(
                        color = ColorProvider(Color.DarkGray),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Tap to build identity",
                    style = TextStyle(
                        color = ColorProvider(Color.Gray),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }

    private fun calculateRemainingMinutes(endTime: String): Long {
        return try {
            val end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"))
            val now = LocalTime.now()
            ChronoUnit.MINUTES.between(now, end)
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Glance content cannot be rendered directly by the Compose preview renderer, so this is a
 * static Compose mirror of the active-mission state for design-time inspection.
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF000000)
@androidx.compose.runtime.Composable
private fun ZenithWidgetPreview() {
    androidx.compose.material3.Surface(color = Color.Black) {
        androidx.compose.foundation.layout.Column(
            modifier = ComposeModifier.composeFillMaxSize().composePadding(12.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            androidx.compose.material3.Text(
                "CURRENT MISSION",
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = ComposeModifier.composeHeight(8.dp)
            )
            androidx.compose.material3.Text(
                "DEEP FOCUS BLOCK",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = ComposeModifier.composeHeight(8.dp)
            )
            androidx.compose.material3.Text("47 mins left", color = SuccessGreen, fontSize = 12.sp)
        }
    }
}
