package com.sanket_satpute_20.ironmind.crucible

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.sanket_satpute_20.ironmind.R
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP

@Composable
fun CrucibleCompleteScreen(
    crucibleId: String,
    campaignComplete: Boolean,
    completedDays: Int,
    totalDays: Int,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val prefs = if (isPreview) null else remember { PrefManager.getInstance(context) }
    val crucible = remember { CrucibleRepository.getCrucibleById(crucibleId) }
    
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.celebration))
    val progress by animateLottieCompositionAsState(composition)

    if (crucible == null) {
        onContinue()
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(contentAlignment = Alignment.Center) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    if (campaignComplete) "CRUCIBLE CONQUERED" else "DAY SECURED",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = GoldXP
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(4.dp, GoldXP)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            crucible.icon,
                            fontSize = 48.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    if (campaignComplete) "NEW IDENTITY FORGED" else "HELL WEEK STILL LIVES",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    if (campaignComplete) crucible.rewardTitle.uppercase() else "$completedDays / $totalDays DAYS SECURED",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    if (campaignComplete) {
                        "The fire of the ${crucible.title} has burned away your weakness. You are no longer who you were."
                    } else {
                        "Today held. The run is still alive. Return tomorrow and secure the next day without blinking."
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldXP),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        if (campaignComplete) "ENTER THE NEXT LEVEL" else "RETURN TO THE CAMPAIGN",
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CrucibleCompleteScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        CrucibleCompleteScreen(
            crucibleId = "titan_1", // Use a valid mock ID if available or this will just show the screen
            campaignComplete = true,
            completedDays = 7,
            totalDays = 7,
            onContinue = {}
        )
    }
}
