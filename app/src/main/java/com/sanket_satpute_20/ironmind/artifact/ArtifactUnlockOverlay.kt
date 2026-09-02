package com.sanket_satpute_20.ironmind.artifact

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.components.PremiumPillButton

@Composable
fun ArtifactUnlockOverlay(
    artifactId: String,
    onDismiss: () -> Unit
) {
    val artifact = remember { ArtifactRepository.getById(artifactId) } ?: return
    
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fire_streak))
    val progress by animateLottieCompositionAsState(composition)

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.95f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "LEGENDARY ARTIFACT DISCOVERED",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = GoldXP
                )
                
                Spacer(modifier = Modifier.height(48.dp))

                Box(contentAlignment = Alignment.Center) {
                    // Pulsing Glow Background
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(GoldXP.copy(alpha = 0.1f * glowAlpha), CircleShape)
                    )
                    
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(2.dp, GoldXP)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(artifact.icon, fontSize = 56.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    artifact.title.uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    artifact.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                PremiumPillButton(
                    label = "ADD TO COLLECTION",
                    onClick = onDismiss,
                    containerColor = GoldXP,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun ArtifactUnlockOverlayPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ArtifactUnlockOverlay(artifactId = ArtifactRepository.IRON_ANCHOR.id, onDismiss = {})
    }
}
