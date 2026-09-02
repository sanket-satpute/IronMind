package com.sanket_satpute_20.ironmind.settings

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanket_satpute_20.ironmind.apps.AppClassificationRepository
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.home.TaskViewModel
import com.sanket_satpute_20.ironmind.psychology.*
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.IronRed
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.theme.TextPrimary

@Composable
fun PsychologySettingsScreen(
    onChangeModeClick    : () -> Unit,
    onRetakeDiagnostic   : () -> Unit,
    onChangeTypeManually : () -> Unit,
    onLinkGoogleClick    : () -> Unit,
    onOpenPremiumUpgrade : () -> Unit,
    onManageDistractions : () -> Unit,
    navController        : androidx.navigation.NavController? = null,
    taskViewModel        : TaskViewModel? = if (androidx.compose.ui.platform.LocalInspectionMode.current) null else viewModel()
) {
    val context     = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    val classificationRepository = remember { AppClassificationRepository.getInstance(context) }
    val currentMode = AdaptiveEngine.getCurrentMode(prefs)
    val currentType = AdaptiveEngine.getCurrentType(prefs)
    val blockedLikeCount by produceState(initialValue = prefs.blockedApps.size) {
        value = classificationRepository.getBlockedLikePackagesSnapshot().size
    }

    val profileDate  = prefs.profileDate
    val energyScore  = prefs.lastEnergyScore
    val stressScore  = prefs.lastStressScore
    val moodWord     = prefs.lastMoodWord
    val lastCheckIn  = prefs.lastCheckinDate

    val auth = Firebase.auth
    val userSnapshot by (taskViewModel?.userSnapshot ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
    val isAnonymous = userSnapshot?.isAnonymous ?: true

    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        Text(
            "Settings & Profile",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Black,
            color      = Color.White
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (!prefs.isPremium) {
            Surface(
                color = DeepBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                onClick = onOpenPremiumUpgrade,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(WarningAmber.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = WarningAmber)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("IRONMIND PRO", color = WarningAmber, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Unlock long-term analytics & templates", color = Color.Gray, fontSize = 12.sp)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        SettingsHeroCard(
            currentMode = currentMode,
            currentTypeLabel = currentType.label,
            isAnonymous = isAnonymous,
            blockedLikeCount = blockedLikeCount
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(16.dp))
        
        com.sanket_satpute_20.ironmind.settings.RecoveryDaySettingsCard()
        
        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(
            title = "Account",
            subtitle = "Backup, identity, and profile ownership."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = DeepBackground,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, if (isAnonymous) WarningAmber.copy(alpha = 0.3f) else NeonCyan.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Image or Placeholder
                    if (userSnapshot?.photoUrl != null) {
                        AsyncImage(
                            model = userSnapshot!!.photoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.size(56.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.05f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isAnonymous) "UNSECURED IDENTITY" else (userSnapshot?.displayName ?: "Elite Operator"),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            if (isAnonymous) "Guest Account · Data is fragile" else (userSnapshot?.email ?: "Linked Account"),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    if (!isAnonymous) {
                        Row {
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.Gray)
                            }
                            IconButton(onClick = { 
                                auth.signOut()
                            }) {
                                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Logout", tint = Color.Gray)
                            }
                        }
                    }
                }

                if (isAnonymous) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        onClick = { if (prefs.isPremium) onLinkGoogleClick() else onOpenPremiumUpgrade() },
                        color = WarningAmber.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (!prefs.isPremium) {
                                Icon(Icons.Rounded.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (prefs.isPremium) "🛡️ SECURE WITH GOOGLE" else "🛡️ PRO: SECURE WITH GOOGLE", color = if (prefs.isPremium) WarningAmber else WarningAmber, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                        }
                    }
                    Text(
                        "Backup your rank, artifacts, and history permanently.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        SettingsSectionHeader(
            title = "Core Tools",
            subtitle = "The deeper systems that shape discipline outside the daily board."
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        Surface(
            color = DeepBackground,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                VaultItem("🌙", "Phone Detox Mode", "Blackout apps during sleep hours", SuccessGreen) { navController?.navigate("detox_scheduler") }
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                VaultItem("🏺", "Legendary Artifacts", "View your unlocked identity relics", GoldXP) { navController?.navigate("artifacts_collection") }
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                VaultItem("🔬", "System Autopsy", "Analyze where your time is being stolen", NeonCyan) { navController?.navigate("autopsy") }
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                VaultItem("🛰️", "Iron Circle", "Private accountability network and privacy controls", SuccessGreen) { navController?.navigate("iron_circle") }
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                VaultItem("📜", "Commitment Contract", "Sign a formal pledge to your goals", ElectricViolet) { navController?.navigate("commitment_contract") }
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                VaultItem("🤝", "Accountability Setup", "Set up an accountability partner", NeonCyan) { navController?.navigate("accountability_setup") }
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                VaultItem("🧬", "Identity Setup", "Define your core identity values", ErrorRed) { navController?.navigate("identity_setup") }
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                VaultItem("🔒", "Privacy & Data", "Export data, view policy, or delete account", ErrorRed) { navController?.navigate("privacy_data") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(
            title = "Accessibility & Sensory",
            subtitle = "Tune the app's sensory feedback to your needs."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = DeepBackground,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            var reducedMotion by remember { mutableStateOf(prefs.reducedMotion) }
            var muteAudio by remember { mutableStateOf(prefs.muteAudio) }
            var reducedHaptics by remember { mutableStateOf(prefs.reducedHaptics) }
            var largeText by remember { mutableStateOf(prefs.largeText) }

            Column {
                AccessibilityToggleRow(
                    emoji = "🏃",
                    title = "Reduced Motion",
                    subtitle = "Minimize animations and transitions",
                    checked = reducedMotion,
                    onCheckedChange = { 
                        reducedMotion = it
                        prefs.reducedMotion = it
                    }
                )
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                AccessibilityToggleRow(
                    emoji = "🔇",
                    title = "Mute Audio",
                    subtitle = "Disable all shield sounds and chimes",
                    checked = muteAudio,
                    onCheckedChange = { 
                        muteAudio = it
                        prefs.muteAudio = it
                    }
                )
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                AccessibilityToggleRow(
                    emoji = "📳",
                    title = "Reduced Haptics",
                    subtitle = "Disable vibration feedback",
                    checked = reducedHaptics,
                    onCheckedChange = { 
                        reducedHaptics = it
                        prefs.reducedHaptics = it
                    }
                )
                HorizontalDivider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))
                AccessibilityToggleRow(
                    emoji = "🔎",
                    title = "Large Text (Coming Soon)",
                    subtitle = "Increase readability across the app",
                    checked = largeText,
                    onCheckedChange = { 
                        largeText = it
                        prefs.largeText = it
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(
            title = "Current Profile",
            subtitle = "Your current type, active mode, and the profile story behind them."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color  = DeepBackground,
            shape  = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SurfaceElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${currentType.emoji} ${currentType.label}",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Black,
                            color      = Color.White
                        )
                        if (profileDate.isNotEmpty()) {
                            Text("Profile set on $profileDate",
                                fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Surface(
                        color  = Color(currentMode.color).copy(alpha = 0.15f),
                        shape  = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(currentMode.color).copy(alpha = 0.4f))
                    ) {
                        Text(
                            "${currentMode.emoji} ${currentMode.label}",
                            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize   = 13.sp,
                            color      = Color(currentMode.color),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = SurfaceDark)
                Spacer(modifier = Modifier.height(14.dp))

                Text(currentType.label,
                    fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(
            title = "Distraction Guard",
            subtitle = "Manage the apps and sites your system treats as dangerous."
        )
        Spacer(modifier = Modifier.height(10.dp))

        SettingsActionRow(
            emoji       = "🛡️",
            title       = "Manage Distractions",
            subtitle    = "$blockedLikeCount protected apps, ${prefs.blockedWebsites.size} sites blocked",
            actionLabel = "Configure →",
            actionColor = ErrorRed,
            onClick     = onManageDistractions
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(
            title = "App Mode",
            subtitle = "Change how hard the system responds when discipline slips."
        )
        Spacer(modifier = Modifier.height(10.dp))

        SettingsActionRow(
            emoji       = currentMode.emoji,
            title       = "Change Mode",
            subtitle    = "Currently: ${currentMode.label}",
            actionLabel = "Change →",
            actionColor = Color(currentMode.color),
            onClick     = onChangeModeClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Quick switch", fontSize = 11.sp, color = Color.White.copy(alpha = 0.46f),
            letterSpacing = 0.4.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppMode.values().forEach { mode ->
                val isActive = mode == currentMode
                Surface(
                    color    = if (isActive) Color(mode.color).copy(alpha = 0.15f)
                               else DeepBackground,
                    shape    = RoundedCornerShape(10.dp),
                    border   = BorderStroke(
                        if (isActive) 2.dp else 1.dp,
                        if (isActive) Color(mode.color) else SurfaceDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .gamifiedClick { 
                            if (!isActive) {
                                AdaptiveEngine.switchMode(
                                    prefs = prefs,
                                    newMode = mode,
                                    isManual = true,
                                    context = context,
                                    previousMode = currentMode.name,
                                    source = "PSYCHOLOGY_SETTINGS"
                                )
                            }
                         }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(mode.emoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            mode.label.replace(" Mode", ""),
                            fontSize  = 10.sp,
                            color     = if (isActive) Color(mode.color)
                                        else SurfaceElevated,
                            fontWeight = if (isActive) FontWeight.Bold
                                         else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(
            title = "Identity Type",
            subtitle = "Retake the diagnostic or choose your type manually when life changes."
        )
        Spacer(modifier = Modifier.height(10.dp))

        SettingsActionRow(
            emoji       = "🧬",
            title       = "Retake Diagnostic",
            subtitle    = "Answer 5 questions again — life changes",
            actionLabel = "Retake",
            actionColor = ElectricViolet,
            onClick     = onRetakeDiagnostic
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsActionRow(
            emoji       = "✏️",
            title       = "Change Type Manually",
            subtitle    = "You know yourself best",
            actionLabel = "Choose →",
            actionColor = ElectricViolet,
            onClick     = onChangeTypeManually
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(
            title = "Weekly Check-In",
            subtitle = "A quick read on energy, stress, and mood."
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (lastCheckIn.isNotEmpty() && energyScore > 0) {
            Surface(
                color  = DeepBackground,
                shape  = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Last check-in: $lastCheckIn",
                        fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniStatChip("⚡ Energy", "$energyScore/5",
                            SuccessGreen)
                        MiniStatChip("🔥 Stress", "$stressScore/5",
                            IronRed)
                        if (moodWord.isNotEmpty()) {
                            MiniStatChip("💭 Mood", moodWord,
                                WarningAmber)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        SettingsActionRow(
            emoji       = "📋",
            title       = "Do Weekly Check-In Now",
            subtitle    = "Energy, stress, one word — 60 seconds",
            actionLabel = "Start →",
            actionColor = WarningAmber,
            onClick     = { navController?.navigate("weekly_checkin") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionHeader(
            title = "Mode Guide",
            subtitle = "What each mode actually changes when a task slips."
        )
        Spacer(modifier = Modifier.height(10.dp))

        AppMode.values().forEach { mode ->
            ModeExplainerCard(mode = mode, isCurrent = mode == currentMode)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        SettingsSectionHeader(
            title = "Debug / Developer Tools",
            subtitle = "Internal tools for testing the behavioral engine."
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        SettingsActionRow(
            emoji       = "⚙️",
            title       = "Internal Event Debugger",
            subtitle    = "Simulate Shield events and day sealing",
            actionLabel = "Open →",
            actionColor = ErrorRed,
            onClick     = { navController?.navigate("debug_event_screen") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = DeepBackground,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👑", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Developer: Force Premium", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Toggle IronMind Pro features instantly", color = Color.Gray, fontSize = 12.sp)
                }
                var devPremium by remember { mutableStateOf(prefs.isPremium) }
                Switch(
                    checked = devPremium,
                    onCheckedChange = {
                        devPremium = it
                        prefs.isPremium = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DeepBackground,
                        checkedTrackColor = WarningAmber,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = DeepBackground
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showEditDialog) {
        EditIdentityDialog(
            currentName = userSnapshot?.displayName ?: "",
            currentPhotoUrl = userSnapshot?.photoUrl ?: "",
            onDismiss = { showEditDialog = false },
            onSave = { name, photo ->
                taskViewModel?.updateProfile(name, photo)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun SettingsHeroCard(
    currentMode: AppMode,
    currentTypeLabel: String,
    isAnonymous: Boolean,
    blockedLikeCount: Int
) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "Settings overview",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Profile, system rules, and deeper control all live here.",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatChip("Mode", currentMode.label, Color(currentMode.color))
                MiniStatChip("Type", currentTypeLabel, WarningAmber)
                MiniStatChip("Account", if (isAnonymous) "Guest" else "Linked", if (isAnonymous) WarningAmber else SuccessGreen)
                MiniStatChip("Guard", "$blockedLikeCount apps", ErrorRed)
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            title,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.82f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.56f),
            lineHeight = 18.sp
        )
    }
}

@Composable
fun EditIdentityDialog(
    currentName: String,
    currentPhotoUrl: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var photoUrl by remember { mutableStateOf(currentPhotoUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepBackground,
        title = { Text("EDIT IDENTITY", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("Profile Photo URL") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, photoUrl) }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                Text("SAVE PROFILE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        }
    )
}

@Composable
fun VaultItem(emoji: String, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .gamifiedClick { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.DarkGray)
    }
}

@Composable
fun SettingsActionRow(
    emoji: String,
    title: String,
    subtitle: String,
    actionLabel: String,
    actionColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SurfaceElevated),
        modifier = Modifier.fillMaxWidth().gamifiedClick { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = actionColor.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Surface(
                color = actionColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    actionLabel,
                    color = actionColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun MiniStatChip(emoji: String, text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 12.sp)
            Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ModeExplainerCard(mode: AppMode, isCurrent: Boolean) {
    Surface(
        color = if (isCurrent) Color(mode.color).copy(alpha = 0.05f) else DeepBackground,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isCurrent) Color(mode.color).copy(alpha = 0.5f) else SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(mode.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(mode.label, color = Color(mode.color), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (isCurrent) {
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = Color(mode.color),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("ACTIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Strike System:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            val desc = when (mode) {
                AppMode.BUILD -> "Steady progress. Compassion after failure. No shame."
                AppMode.IRON -> "Strict enforcement. 5s delays. Fails cost 5 points."
                AppMode.RECOVERY -> "Small steps. Zero pressure. Just showing up."
                AppMode.EXPERIMENT -> "No streaks. Rolling rates. Imperfect is fine."
            }
            Text(desc, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

@Composable
fun AccessibilityToggleRow(
    emoji: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .gamifiedClick { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = SuccessGreen.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen)
        )
    }
}




// =========================================================
// Preview
// =========================================================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun PsychologySettingsPreview() {
    if (!androidx.compose.ui.platform.LocalInspectionMode.current) return
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        PsychologySettingsPreviewContent()
    }
}

@androidx.compose.runtime.Composable
private fun PsychologySettingsPreviewContent() {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(20.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.material3.Text(
            "Psychology Settings",
            color = androidx.compose.ui.graphics.Color.White,
            fontSize = 22.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        listOf("🧠 Current Mode: IRON", "👤 User Type: Achiever", "🔄 Retake Diagnostic", "⚡ Change Mode").forEach { item ->
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = SurfaceElevated,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text(
                    item,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = androidx.compose.ui.Modifier.padding(16.dp)
                )
            }
        }
}
}

