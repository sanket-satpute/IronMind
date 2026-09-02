package com.sanket_satpute_20.ironmind.billing

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP

@Composable
fun PremiumUpgradeScreen(
    onBack: () -> Unit = {},
    onUpgradeSuccess: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(DeepBackground, DeepBackground))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Glowing Crown icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(GoldXP.copy(alpha = glowAlpha), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("👑", fontSize = 40.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "IRONMIND PRO",
                color = GoldXP,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Unlock the ultimate discipline engine. No excuses. Complete control.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(48.dp))

            // Features box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(20.dp))
                    .border(1.dp, Brush.verticalGradient(listOf(GoldXP.copy(0.5f), Color.Transparent)), RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeatureRow("Boss Mode Unlocked", "Ruthless strictness that prevents app uninstallation.")
                    FeatureRow("Advanced Analytics", "Heatmaps, trend lines, and deep monthly audits.")
                    FeatureRow("Infinite History", "Access your entire graveyard and past performance.")
                    FeatureRow("Custom Challenges", "Build and join private 30-day challenges.")
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onUpgradeSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldXP),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("UPGRADE TO PRO — \$4.99/mo", color = Color.Black, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Cancel anytime. But why would you?",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureRow(title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = GoldXP,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text(desc, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PremiumUpgradeScreenPreview() {
    IronMindTheme { PremiumUpgradeScreen() }
}