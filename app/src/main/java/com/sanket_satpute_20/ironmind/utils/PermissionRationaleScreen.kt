package com.sanket_satpute_20.ironmind.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRationaleScreen(
    permissionTypeStr: String,
    onBack: () -> Unit,
    onContinueToSettings: () -> Unit
) {
    val context = LocalContext.current
    val permissionType = runCatching { PermissionRequirementType.valueOf(permissionTypeStr) }.getOrNull()

    val (title, icon, explanation, dataUsage) = when (permissionType) {
        PermissionRequirementType.ACCESSIBILITY -> RationaleData(
            "Accessibility Service",
            Icons.Rounded.AccessibilityNew,
            "IronMind requires Accessibility Service to detect when you open a blocked app, and to draw the blocking screen over it.",
            "This service only observes the package name of the active app. It does not read your messages, capture your screen, or log keystrokes. No data leaves your device."
        )
        PermissionRequirementType.USAGE_ACCESS -> RationaleData(
            "Usage Access",
            Icons.Rounded.DataUsage,
            "IronMind requires Usage Access to know how long you've been using apps, which allows the System Autopsy feature to show where your time went.",
            "Usage statistics are processed locally on your device to generate your daily reports. They are never sent to our servers."
        )
        PermissionRequirementType.DO_NOT_DISTURB -> RationaleData(
            "Do Not Disturb Access",
            Icons.Rounded.DoNotDisturbOn,
            "IronMind requires Do Not Disturb access to automatically silence notifications and calls when you enter a Deep Focus session.",
            "This permission is only used to toggle your phone's ringer state. No notification content is read."
        )
        PermissionRequirementType.EXACT_ALARMS -> RationaleData(
            "Exact Alarms",
            Icons.Rounded.Alarm,
            "IronMind requires exact alarm permissions to trigger your Bedtime approach warnings and Morning Launch precisely on time.",
            "This is strictly used for scheduling local notifications."
        )
        PermissionRequirementType.OVERLAYS -> RationaleData(
            "Display Over Other Apps",
            Icons.Rounded.Layers,
            "IronMind requires this permission to enforce strict focus modes by displaying the lock screen over distracting apps.",
            "Only used when you attempt to open a blocked app."
        )
        PermissionRequirementType.BATTERY_OPTIMIZATION -> RationaleData(
            "Battery Optimization",
            Icons.Rounded.BatteryStd,
            "IronMind needs to ignore battery optimization so that it can run in the background effectively without Android force-stopping it.",
            "This prevents system interference with timers and blocking."
        )
        else -> RationaleData(
            "System Permission",
            Icons.Rounded.Settings,
            "IronMind requires this permission to function correctly.",
            "Data remains local to your device."
        )
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logPermissionFunnelAbandoned(permissionTypeStr)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                color = NeonCyan.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(40.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Why we need",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                title.uppercase(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Surface(
                color = DeepBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SurfaceElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Core Functionality", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(explanation, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text("Data Privacy", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(dataUsage, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logPermissionRequested(permissionTypeStr)
                    onContinueToSettings()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("I UNDERSTAND, CONTINUE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = {
                    com.sanket_satpute_20.ironmind.analytics.AnalyticsManager.logPermissionFunnelAbandoned(permissionTypeStr)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("NOT NOW", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private data class RationaleData(
    val title: String,
    val icon: ImageVector,
    val explanation: String,
    val dataUsage: String
)





@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun PermissionRationaleScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        PermissionRationaleScreen(permissionTypeStr = "Accessibility", onBack = {}, onContinueToSettings = {})
    }
}
