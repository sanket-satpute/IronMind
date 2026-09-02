package com.sanket_satpute_20.ironmind.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCenterScreen(
    onBack: () -> Unit = {},
    onNavigateToRationale: (PermissionRequirementType) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var requirements by remember { mutableStateOf(PermissionHelper.getPermissionCenterRequirements(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                requirements = PermissionHelper.getPermissionCenterRequirements(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val grantedCount = requirements.count { it.isGranted }
    val criticalMissing = requirements.count { !it.isGranted && it.priority == PermissionPriority.CRITICAL }
    val importantMissing = requirements.count { !it.isGranted && it.priority == PermissionPriority.IMPORTANT }
    val supportiveMissing = requirements.count { !it.isGranted && it.priority == PermissionPriority.SUPPORTIVE }
    val missingRequirements = remember(requirements) { requirements.filterNot { it.isGranted } }
    val nextRequirement = missingRequirements.firstOrNull()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "PERMISSION CENTER",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = DeepBackground,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "SYSTEM READINESS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "$grantedCount/${requirements.size} protections are ready. Core blocking, alarms, and silence controls are managed here.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermissionSummaryChip(
                            label = if (criticalMissing == 0) "CRITICAL READY" else "$criticalMissing CRITICAL",
                            color = if (criticalMissing == 0) SuccessGreen else ErrorRed
                        )
                        PermissionSummaryChip(
                            label = if (importantMissing == 0) "IMPORTANT READY" else "$importantMissing IMPORTANT",
                            color = if (importantMissing == 0) SuccessGreen else WarningAmber
                        )
                        PermissionSummaryChip(
                            label = if (supportiveMissing == 0) "OPTIONAL READY" else "$supportiveMissing OPTIONAL",
                            color = if (supportiveMissing == 0) SuccessGreen else NeonCyan
                        )
                    }
                }
            }

            PermissionWizardCard(
                requirement = nextRequirement,
                step = if (nextRequirement == null) 0 else 1,
                totalSteps = missingRequirements.size,
                onContinue = { nextRequirement?.let { onNavigateToRationale(it.type) } }
            )

            if (nextRequirement != null) {
                Text(
                    "ALL PROTECTIONS",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            requirements.forEach { requirement ->
                PermissionCenterCard(
                    requirement = requirement,
                    onRequirementClicked = { onNavigateToRationale(it.type) }
                )
            }
        }
    }
}

@Composable
private fun PermissionWizardCard(
    requirement: PermissionRequirement?,
    step: Int,
    totalSteps: Int,
    onContinue: () -> Unit
) {
    val isComplete = requirement == null
    val accent = if (isComplete) SuccessGreen else when (requirement.priority) {
        PermissionPriority.CRITICAL -> ErrorRed
        PermissionPriority.IMPORTANT -> WarningAmber
        PermissionPriority.SUPPORTIVE -> NeonCyan
    }
    Surface(
        color = accent.copy(alpha = 0.10f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                if (isComplete) "WIZARD COMPLETE" else "NEXT STEP  $step / $totalSteps",
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                if (isComplete) "Your focus system is fully armed." else requirement.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                if (isComplete) "Every requested protection is ready to support your execution." else requirement.description,
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 13.sp
            )
            if (!isComplete) {
                Surface(
                    onClick = onContinue,
                    color = accent,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("OPEN ${requirement.actionLabel}", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionSummaryChip(
    label: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun PermissionCenterCard(
    requirement: PermissionRequirement,
    onRequirementClicked: (PermissionRequirement) -> Unit = {}
) {
    val context = LocalContext.current
    val accentColor = when (requirement.priority) {
        PermissionPriority.CRITICAL -> ErrorRed
        PermissionPriority.IMPORTANT -> WarningAmber
        PermissionPriority.SUPPORTIVE -> NeonCyan
    }

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = CircleShape,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                    Icon(
                        imageVector = if (requirement.isGranted) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = if (requirement.isGranted) SuccessGreen else accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(requirement.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text(
                        if (requirement.isGranted) "Granted" else requirement.shortStatus,
                        color = if (requirement.isGranted) SuccessGreen else accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        when (requirement.priority) {
                            PermissionPriority.CRITICAL -> "CRITICAL"
                            PermissionPriority.IMPORTANT -> "IMPORTANT"
                            PermissionPriority.SUPPORTIVE -> "OPTIONAL"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                requirement.description,
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (requirement.isGranted) {
                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.2f))
                ) {
                    Text(
                        "READY",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = SuccessGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            } else {
                Surface(
                    onClick = { onRequirementClicked(requirement) },
                    color = accentColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${requirement.actionLabel} ${requirement.title.uppercase()}",
                            color = accentColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}





@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun PermissionCenterScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        PermissionCenterScreen()
    }
}
