package com.sanket_satpute_20.ironmind.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun OperationsScreen(
    navController: NavController? = null,
    onBack: () -> Unit = {},
    onOpenPsychologySettings: () -> Unit = {},
    onOpenModeSwitcher: () -> Unit = {},
    onOpenPermissionCenter: () -> Unit = {},
    onOpenLogin: () -> Unit = {},
    onOpenIdentitySetup: () -> Unit = {},
    onOpenCommitmentContract: () -> Unit = {},
    onOpenDistractionsSetup: () -> Unit = {},
    onOpenSleepLock: () -> Unit = {},
    onOpenAppReviewQueue: () -> Unit = {},
    onOpenEmergencyValve: () -> Unit = {},
    onOpenPremiumUpgrade: () -> Unit = {},
    onOpenSpringControl: () -> Unit = {},
    onOpenChallenge: () -> Unit = {},
    onOpenClubChat: () -> Unit = {},
    onOpenIronCircle: () -> Unit = {},
    onTestMorningBrief: () -> Unit = {},
    onTestConfidenceIgnition: () -> Unit = {},
    onTestMorningLaunch: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "COMMAND CENTER",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "SYSTEM OPERATIONS",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(48.dp)) // Balance the close button
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                OperationsSection(
                    title = "SYSTEM CONFIGURATION",
                    icon = Icons.Rounded.Settings,
                    color = NeonCyan
                ) {
                    OperationRow(icon = Icons.Rounded.Psychology, title = "Psychology Framework", onClick = onOpenPsychologySettings)
                    OperationRow(icon = Icons.Rounded.ToggleOn, title = "Mode Switcher", onClick = onOpenModeSwitcher)
                    OperationRow(icon = Icons.Rounded.Security, title = "Permission Center", onClick = onOpenPermissionCenter)
                    OperationRow(icon = Icons.Rounded.AccountCircle, title = "Account Identity", onClick = onOpenLogin)
                }
            }

            item {
                OperationsSection(
                    title = "PROTOCOL SETUP",
                    icon = Icons.Rounded.ListAlt,
                    color = SuccessGreen
                ) {
                    OperationRow(icon = Icons.Rounded.Fingerprint, title = "Identity Architecture", onClick = onOpenIdentitySetup)
                    OperationRow(icon = Icons.Rounded.Gavel, title = "Commitment Contract", onClick = onOpenCommitmentContract)
                    OperationRow(icon = Icons.Rounded.Block, title = "Distraction Blacklist", onClick = onOpenDistractionsSetup)
                    OperationRow(icon = Icons.Rounded.NightsStay, title = "Sleep Lock Enforcement", onClick = onOpenSleepLock)
                    OperationRow(icon = Icons.Rounded.RateReview, title = "App Review Queue", onClick = onOpenAppReviewQueue)
                }
            }
            
            item {
                OperationsSection(
                    title = "ADVANCED ENFORCEMENT",
                    icon = Icons.Rounded.Warning,
                    color = WarningAmber
                ) {
                    OperationRow(icon = Icons.Rounded.Bolt, title = "Spring Control", onClick = onOpenSpringControl)
                    OperationRow(
                        icon = Icons.Rounded.ReportProblem, 
                        title = "Emergency Valve", 
                        subtitle = "Break glass protocol", 
                        isDestructive = true,
                        onClick = onOpenEmergencyValve
                    )
                }
            }

            item {
                OperationsSection(
                    title = "NETWORK & TRIBE",
                    icon = Icons.Rounded.Hub,
                    color = ElectricViolet
                ) {
                    OperationRow(icon = Icons.Rounded.WbTwilight, title = "5AM Club Challenge", onClick = onOpenChallenge)
                    OperationRow(icon = Icons.Rounded.GroupWork, title = "Iron Circle Network", onClick = onOpenIronCircle)
                }
            }

            item {
                OperationsSection(
                    title = "PREMIUM",
                    icon = Icons.Rounded.WorkspacePremium,
                    color = GoldXP // Gold
                ) {
                    OperationRow(
                        icon = Icons.Rounded.Star, 
                        title = "IronMind PRO", 
                        subtitle = "Unlock all restrictions", 
                        isPremium = true,
                        onClick = onOpenPremiumUpgrade
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OperationsSection(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .background(SurfaceDark)
        ) {
            content()
        }
    }
}

@Composable
private fun OperationRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    isPremium: Boolean = false,
    onClick: () -> Unit
) {
    val mainColor = when {
        isDestructive -> ErrorRed
        isPremium -> GoldXP
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(mainColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = mainColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = mainColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = mainColor.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OperationsScreenPreview() {
    IronMindTheme { OperationsScreen() }
}