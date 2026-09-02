package com.sanket_satpute_20.ironmind.psychology

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan

@Composable
fun ProfileRevealScreen(
    profile: UserProfile,
    onContinue: () -> Unit = {}
) {
    var isRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        isRevealed = true
    }

    val glowAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(1500)
    )

    val scale by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .scale(scale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(profile.assignedMode.color).copy(alpha = 0.2f * glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "YOUR PSYCHOLOGICAL PROFILE",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                shape = CircleShape,
                color = DeepBackground,
                border = BorderStroke(2.dp, Color(profile.assignedMode.color).copy(alpha = glowAlpha))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        profile.primaryType.emoji,
                        fontSize = 54.sp,
                        modifier = Modifier.alpha(glowAlpha)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = isRevealed,
                enter = fadeIn(tween(1000)) + expandVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "THE ${profile.primaryType.label.uppercase()}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Surface(
                        color = DeepBackground,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "RECOMMENDED PROTOCOL:",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                profile.assignedMode.label.uppercase(),
                                color = Color(profile.assignedMode.color),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                profile.assignedMode.description,
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(64.dp))

            AnimatedVisibility(
                visible = isRevealed,
                enter = fadeIn(tween(2000, delayMillis = 1000))
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "INITIALIZE PROTOCOL", 
                        color = Color.Black, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileRevealScreenPreview() {
    IronMindTheme {
        ProfileRevealScreen(
            profile = UserProfile(
                primaryType = UserType.DOOMSCROLLER,
                secondaryType = null,
                assignedMode = AppMode.IRON,
                typeScores = emptyMap(),
                profileCreatedDate = LocalDate.now().toString()
            )
        )
    }
}