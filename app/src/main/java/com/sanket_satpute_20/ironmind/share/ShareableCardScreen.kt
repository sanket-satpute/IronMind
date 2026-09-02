package com.sanket_satpute_20.ironmind.share

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.IronRed
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun ShareableCardScreen(
    streakDays: Int = 0,
    userName: String = "Warrior",
    onShare: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "SHARE YOUR VICTORY",
                fontSize = 13.sp,
                color = ErrorRed,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(20.dp))

            // The shareable card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DeepBackground, DeepBackground)
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(ErrorRed, IronRed)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "⚡", fontSize = 40.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "IRONMIND",
                        fontSize = 12.sp,
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "$streakDays",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "DAY STREAK",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 3.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = userName.uppercase(),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Building discipline. One day at a time.",
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onShare,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("SHARE CARD", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onContinue) {
                Text("Skip", color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShareableCardScreenPreview() {
    IronMindTheme { ShareableCardScreen(streakDays = 21, userName = "Sanket") }
}
