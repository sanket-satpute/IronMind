package com.sanket_satpute_20.ironmind.context

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen

@Composable
fun ManualOverrideToggle(
    currentMode: ModeStateEngine.AppMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTravelMode = currentMode == ModeStateEngine.AppMode.TRAVEL_FLEXIBLE || 
                       currentMode == ModeStateEngine.AppMode.MANUAL_OVERRIDE

    val accent = if (isTravelMode) NeonCyan else SuccessGreen
    val label = if (isTravelMode) "TRAVEL MODE" else "DISCIPLINE BASE"
    val icon = if (isTravelMode) Icons.Rounded.Map else Icons.Rounded.Home

    Surface(
        color = accent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
        modifier = if (LocalInspectionMode.current) modifier.clickable(onClick = onToggle) else modifier.gamifiedClick { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = accent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
private fun ManualOverrideTogglePreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ManualOverrideToggle(ModeStateEngine.AppMode.TRAVEL_FLEXIBLE, onToggle = {})
    }
}
