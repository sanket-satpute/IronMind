package com.sanket_satpute_20.ironmind.identity

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.airbnb.lottie.LottieComposition
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.psychology.IdentityLevelEngine
import com.sanket_satpute_20.ironmind.psychology.UserType
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun IdentityLevelUpOverlay(
    newLevel: Int,
    userType: UserType,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val title = IdentityLevelEngine.getIdentityTitle(userType, newLevel)
    
    // We mock lottie and contexts for previews
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val composition by if (isPreview) remember { mutableStateOf<LottieComposition?>(null) } else rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.celebration))
    val progress by if (isPreview) remember { mutableStateOf(1f) } else animateLottieCompositionAsState(composition)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBackground.copy(alpha = 0.92f) // Very dark, slightly transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            
            // Ambient glow
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .scale(pulseScale)
                    .blur(100.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GoldXP.copy(alpha = 0.3f), // Gold glow
                                Color.Transparent
                            )
                        )
                    )
            )

            if (!isPreview) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Diamond,
                    contentDescription = null,
                    tint = GoldXP,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "LEVEL SECURED",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp
                    ),
                    color = GoldXP
                )
                
                Spacer(modifier = Modifier.height(48.dp))

                Box(contentAlignment = Alignment.Center) {
                    
                    // Expanding rings
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .border(1.dp, GoldXP.copy(alpha = 0.2f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale * 0.95f)
                            .border(2.dp, GoldXP.copy(alpha = 0.4f), CircleShape)
                    )

                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = SurfaceDark,
                        border = BorderStroke(4.dp, Brush.linearGradient(
                            listOf(GoldXP, WarningAmber)
                        ))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                newLevel.toString(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    "NEW RANK UNLOCKED",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    title.uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "You are no longer who you were.\nYour standards have risen.",
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldXP),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "ACCEPT IDENTITY",
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        letterSpacing = 2.sp,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IdentityLevelUpOverlayPreview() {
    IronMindTheme {
        IdentityLevelUpOverlay(
            newLevel = 5,
            userType = UserType.ACHIEVER,
            onDismiss = {}
        )
    }
}
