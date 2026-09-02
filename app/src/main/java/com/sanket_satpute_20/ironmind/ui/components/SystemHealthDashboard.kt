package com.sanket_satpute_20.ironmind.ui.components

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.utils.PermissionHelper
import com.sanket_satpute_20.ironmind.utils.PermissionRequirementType
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary
import com.sanket_satpute_20.ironmind.ui.theme.TextSecondary
import com.sanket_satpute_20.ironmind.ui.theme.DividerColor

@Composable
fun SystemHealthDashboard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Periodically re-check permissions to ensure accurate dashboard data
    var accessGranted by remember { mutableStateOf(PermissionHelper.isAccessibilityEnabled(context)) }
    var batteryIgnore by remember { mutableStateOf(PermissionHelper.isIgnoringBatteryOptimizations(context)) }
    var overlaysGranted by remember { mutableStateOf(PermissionHelper.canDrawOverlays(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            accessGranted = PermissionHelper.isAccessibilityEnabled(context)
            batteryIgnore = PermissionHelper.isIgnoringBatteryOptimizations(context)
            overlaysGranted = PermissionHelper.canDrawOverlays(context)
            delay(2000L) // check every 2 seconds
        }
    }

    val isAllGood = accessGranted && batteryIgnore && overlaysGranted
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = if (isAllGood) SurfaceElevated else DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isAllGood) SuccessGreen.copy(alpha = 0.3f) else ErrorRed.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .gamifiedClick { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (isAllGood) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isAllGood) Icons.Rounded.Security else Icons.Rounded.Warning,
                            contentDescription = "Security Status",
                            tint = if (isAllGood) SuccessGreen else ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "OS DEFENSE GRID",
                        color = if (isAllGood) SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        if (isAllGood) "All systems optimal" else "Defenses compromised by OS",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            var showAccessibilityConsent by remember { mutableStateOf(false) }

            if (showAccessibilityConsent) {
                com.sanket_satpute_20.ironmind.ui.components.AccessibilityConsentDialog(
                    onAgree = {
                        showAccessibilityConsent = false
                        PermissionHelper.openAccessibilitySettings(context)
                    },
                    onDecline = {
                        showAccessibilityConsent = false
                    }
                )
            }

            AnimatedVisibility(visible = !isAllGood || expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = DividerColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    HealthDiagnosticRow(
                        label = "Accessibility Service",
                        isGranted = accessGranted,
                        onClick = { showAccessibilityConsent = true }
                    )
                    HealthDiagnosticRow(
                        label = "Draw Over Other Apps",
                        isGranted = overlaysGranted,
                        onClick = { PermissionHelper.requestOverlayPermission(context) }
                    )
                    HealthDiagnosticRow(
                        label = "Unrestricted Battery",
                        isGranted = batteryIgnore,
                        onClick = { PermissionHelper.requestIgnoreBatteryOptimizations(context) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthDiagnosticRow(
    label: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .gamifiedClick { if (!isGranted) onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = if (isGranted) SuccessGreen else WarningAmber,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = TextPrimary.copy(alpha = 0.90f),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        if (!isGranted) {
            Surface(
                color = WarningAmber.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "FIX",
                    color = WarningAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun SystemHealthDashboardPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        SystemHealthDashboard()
    }
}
