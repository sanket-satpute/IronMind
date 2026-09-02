package com.sanket_satpute_20.ironmind.ui.components
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.home.HomeMissionState

@Composable
fun PathNodeUI(
    state: HomeMissionState,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeColor = when (state) {
        HomeMissionState.COMPLETED -> SuccessGreen
        HomeMissionState.LIVE_NOW, HomeMissionState.UP_NEXT -> WarningAmber
        HomeMissionState.OVERDUE -> Color.Red
        else -> Color.DarkGray
    }

    val icon: ImageVector = when (state) {
        HomeMissionState.COMPLETED -> Icons.Rounded.CheckCircle
        HomeMissionState.LATER_TODAY -> Icons.Rounded.Lock
        else -> Icons.Rounded.Bolt
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(if (state == HomeMissionState.LIVE_NOW) 12.dp else 4.dp, CircleShape, spotColor = nodeColor)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            nodeColor.copy(alpha = 0.8f),
                            nodeColor.copy(alpha = 0.4f)
                        )
                    )
                )
                .border(
                    width = if (state == HomeMissionState.LIVE_NOW) 3.dp else 1.dp,
                    color = nodeColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title.take(15) + if (title.length > 15) "..." else "",
            color = if (state == HomeMissionState.LATER_TODAY) Color.Gray else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun PathNodeUIPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        PathNodeUI(state = com.sanket_satpute_20.ironmind.home.HomeMissionState.LIVE_NOW, title = "Mission", onClick = {})
    }
}