package com.sanket_satpute_20.ironmind.onboarding

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.sanket_satpute_20.ironmind.gamification.gamifiedClick
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sanket_satpute_20.ironmind.apps.AppCategory
import com.sanket_satpute_20.ironmind.apps.AppClassificationRepository
import com.sanket_satpute_20.ironmind.apps.AppClassificationSource
import com.sanket_satpute_20.ironmind.apps.QuickProtectMode
import com.sanket_satpute_20.ironmind.data.AppClassification
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.components.AppIconImage
import com.sanket_satpute_20.ironmind.utils.PermissionHelper
import java.util.Calendar
import kotlinx.coroutines.launch
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

private val SuccessGreen = SurfaceElevated
private val ErrorRed = ErrorRed
private val WarningAmber = WarningAmber
private val SignalBlue = NeonCyan
private val ContextPurple = ElectricViolet
private val SurfaceLighter = SurfaceElevated

private data class ReviewAppItem(
    val classification: AppClassification,
    val usageTimeMs: Long,
    val usageLevel: Float
)

private data class CategorySection(
    val title: String,
    val subtitle: String,
    val items: List<ReviewAppItem>
)

private enum class SelectorMode {
    QUICK_PROTECT,
    ADVANCED_REVIEW
}

@Composable
fun AppSelectorScreen(onNext: () -> Unit, reviewFilter: String = "") {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        AppSelectorPreviewContent()
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = PrefManager.getInstance(context)
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val repo = remember(context, isPreview) { if (isPreview) null else AppClassificationRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var blockWebsites by remember { mutableStateOf(prefs.websiteBlockingEnabled) }
    var showWebsiteSelector by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(PermissionHelper.hasUsageStatsPermission(context))
    }
    var selectedApp by remember { mutableStateOf<AppClassification?>(null) }
    val reviewOnly = reviewFilter.equals("pending", ignoreCase = true)
    var selectorMode by
        remember(reviewOnly) {
            mutableStateOf(
                if (reviewOnly) SelectorMode.ADVANCED_REVIEW else SelectorMode.QUICK_PROTECT
            )
        }
    val classifications by (repo?.observeAll() ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())
    val usageByPackage by
        produceState<Map<String, Long>>(
            initialValue = emptyMap(),
            hasPermission,
            classifications.size
        ) {
            value = if (hasPermission) loadUsageStats(context) else emptyMap()
        }
    LaunchedEffect(Unit) {
        repo?.syncInstalledApps(context)
        repo?.importLegacyBlockedApps(prefs.blockedApps)
        if (repo != null) syncLegacyBlockedApps(repo, prefs, context, "APP_SELECTOR_BOOTSTRAP", false)
    }
    BackHandler(enabled = showWebsiteSelector) { showWebsiteSelector = false }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME)
                hasPermission = PermissionHelper.hasUsageStatsPermission(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    if (showWebsiteSelector) {
        WebsiteSelectorScreen(
            onBack = {
                showWebsiteSelector = false
                sendGuardReload(context)
            }
        )
        return
    }
    val reviewItems =
        remember(classifications, usageByPackage) {
            val maxUsage = usageByPackage.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
            classifications.map {
                val usage = usageByPackage[it.packageName] ?: 0L
                ReviewAppItem(it, usage, (usage.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f))
            }
        }
    val filteredItems =
        remember(reviewItems, searchQuery) {
            if (searchQuery.isBlank()) reviewItems
            else
                reviewItems.filter {
                    it.classification.appName.contains(searchQuery, true) ||
                        it.classification.packageName.contains(searchQuery, true)
                }
        }
    val pendingItems =
        remember(filteredItems) {
            filteredItems
                .filter {
                    AppClassificationSource.fromWireValue(it.classification.source) ==
                        AppClassificationSource.UNKNOWN_PENDING
                }
                .sortedByDescending { it.usageTimeMs }
        }
    val riskyHighlights =
        remember(filteredItems, searchQuery) {
            if (searchQuery.isNotBlank()) emptyList()
            else
                filteredItems
                    .filter {
                        when (AppCategory.fromWireValue(it.classification.category)) {
                            AppCategory.VOID,
                            AppCategory.SIGNAL,
                            AppCategory.CONTEXT -> true
                            AppCategory.TOOL -> false
                        }
                    }
                    .sortedByDescending { it.usageTimeMs }
                    .take(8)
        }
    val quickProtectMode = selectorMode == SelectorMode.QUICK_PROTECT && !reviewOnly
    val sections =
        remember(filteredItems, pendingItems, reviewOnly, quickProtectMode) {
            buildCategorySections(filteredItems, pendingItems, reviewOnly, quickProtectMode)
        }
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(56.dp))
                Text(
                    if (reviewOnly) "REVIEW QUEUE"
                    else if (quickProtectMode) "QUICK PROTECT" else "MISSION TREATMENT",
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                    color = WarningAmber
                )
                Text(
                    if (reviewOnly) "Resolve the apps IronMind still doesn't trust."
                    else if (quickProtectMode) "Pick the apps that break your focus fast."
                    else "Teach IronMind how to treat your apps.",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (reviewOnly)
                        "This view is focused on apps that were used enough to require an explicit mission-treatment decision."
                    else if (quickProtectMode)
                        "Block distracting apps, mark ambiguous ones to ask per mission, and leave safe tools alone."
                    else
                        "Void gets blocked. Signal gets controlled. Tool stays safe. Context gets declared before a mission.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(20.dp))
                if (!reviewOnly) {
                    SelectorModeRow(
                        selectorMode = selectorMode,
                        onModeSelected = { selectorMode = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (reviewOnly) "Search review queue..."
                            else if (quickProtectMode) "Search distracting apps..."
                            else "Search installed apps...",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray)
                    },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarningAmber,
                            unfocusedBorderColor = SurfaceLighter,
                            focusedContainerColor = SurfaceLighter,
                            unfocusedContainerColor = SurfaceLighter,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding =
                    PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { ClassificationSummaryRow(classifications, quickProtectMode) }
                if (pendingItems.isNotEmpty()) {
                    item {
                        ReviewFocusCard(pendingCount = pendingItems.size, reviewOnly = reviewOnly)
                    }
                }
                item {
                    if (hasPermission) UsageSignalCard()
                    else PermissionGuardCard { PermissionHelper.openUsageStatsSettings(context) }
                }
                if (!reviewOnly) {
                    item {
                        WebsiteBlockerCard(
                            isEnabled = blockWebsites,
                            onToggle = { enabled ->
                                blockWebsites = enabled
                                val previous = prefs.websiteBlockingEnabled
                                prefs.websiteBlockingEnabled = enabled
                                HistoryRecorder.recordConfigChange(
                                    context,
                                    "WEBSITE_BLOCKING_ENABLED",
                                    previous,
                                    enabled,
                                    "APP_SELECTOR"
                                )
                                sendGuardReload(context)
                                if (enabled) showWebsiteSelector = true
                            },
                            onConfigure = { showWebsiteSelector = true }
                        )
                    }
                }
                if (riskyHighlights.isNotEmpty() && !reviewOnly) {
                    item {
                        SectionLabel(
                            "PRESSURE APPS",
                            "Most-used risky or ambiguous apps from the last day."
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(riskyHighlights, key = { it.classification.packageName }) {
                                SuggestedReviewChip(it) { selectedApp = it.classification }
                            }
                        }
                    }
                }
                sections.forEach { section ->
                    if (section.items.isNotEmpty()) {
                        item(key = "${section.title}_header") {
                            SectionLabel(section.title, section.subtitle)
                        }
                        items(section.items, key = { it.classification.packageName }) { item ->
                            if (quickProtectMode) {
                                QuickProtectAppCard(
                                    item = item,
                                    onBlock = {
                                        scope.launch {
                                            repo?.setQuickProtectMode(
                                                packageName = item.classification.packageName,
                                                mode = QuickProtectMode.BLOCKED,
                                                decisionSource = "QUICK_PROTECT_BLOCK"
                                            )
                                            if (repo != null) syncLegacyBlockedApps(
                                                repo,
                                                prefs,
                                                context,
                                                "QUICK_PROTECT_BLOCK",
                                                true
                                            )
                                        }
                                    },
                                    onAsk = {
                                        scope.launch {
                                            repo?.setQuickProtectMode(
                                                packageName = item.classification.packageName,
                                                mode = QuickProtectMode.ASK,
                                                decisionSource = "QUICK_PROTECT_ASK"
                                            )
                                            if (repo != null) syncLegacyBlockedApps(
                                                repo,
                                                prefs,
                                                context,
                                                "QUICK_PROTECT_ASK",
                                                true
                                            )
                                        }
                                    },
                                    onAllow = {
                                        scope.launch {
                                            repo?.setQuickProtectMode(
                                                packageName = item.classification.packageName,
                                                mode = QuickProtectMode.ALLOWED,
                                                decisionSource = "QUICK_PROTECT_ALLOW"
                                            )
                                            if (repo != null) syncLegacyBlockedApps(
                                                repo,
                                                prefs,
                                                context,
                                                "QUICK_PROTECT_ALLOW",
                                                true
                                            )
                                        }
                                    },
                                    onAdvanced = { selectedApp = item.classification }
                                )
                            } else {
                                ReviewAppCard(item) { selectedApp = item.classification }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = {
                    val previous = prefs.distractionsSetupComplete
                    prefs.distractionsSetupComplete = true
                    HistoryRecorder.recordConfigChange(
                        context,
                        "DISTRACTIONS_SETUP_COMPLETE",
                        previous,
                        prefs.distractionsSetupComplete,
                        "APP_SELECTOR"
                    )
                    onNext()
                },
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .fillMaxWidth()
                        .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (reviewOnly) "SAVE REVIEW"
                    else if (quickProtectMode) "SAVE PROTECTION" else "SAVE CLASSIFICATION",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.Black
                )
            }
        }
    }
    selectedApp?.let { app ->
        CategoryPickerDialog(
            app = app,
            onDismiss = { selectedApp = null },
            onChoose = { category ->
                scope.launch {
                    if (AppCategory.fromWireValue(app.category) == category)
                        repo?.confirmCategory(app.packageName, category)
                    else repo?.overrideCategory(app.packageName, category)
                    if (repo != null) syncLegacyBlockedApps(
                        repo,
                        prefs,
                        context,
                        "APP_SELECTOR_CATEGORY_UPDATE",
                        true
                    )
                    selectedApp = null
                }
            }
        )
    }
}

@Composable
private fun SelectorModeRow(selectorMode: SelectorMode, onModeSelected: (SelectorMode) -> Unit) {

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        SelectorModeChip(
            title = "Quick Protect",
            subtitle = "Simple block / ask / allow",
            selected = selectorMode == SelectorMode.QUICK_PROTECT,
            onClick = { onModeSelected(SelectorMode.QUICK_PROTECT) },
            color = WarningAmber
        )
        SelectorModeChip(
            title = "Advanced Review",
            subtitle = "Full treatment control",
            selected = selectorMode == SelectorMode.ADVANCED_REVIEW,
            onClick = { onModeSelected(SelectorMode.ADVANCED_REVIEW) },
            color = ContextPurple
        )
    }
}

@Composable
private fun RowScope.SelectorModeChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color
) {

    Surface(
        modifier = Modifier.weight(1f),
        onClick = onClick,
        color = if (selected) color.copy(alpha = 0.14f) else SurfaceLighter,
        shape = RoundedCornerShape(18.dp),
        border =
            BorderStroke(
                1.dp,
                if (selected) color.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.08f)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                title,
                color = if (selected) color else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ClassificationSummaryRow(
    classifications: List<AppClassification>,
    quickProtectMode: Boolean
) {
    val quickCounts =
        remember(classifications) {
            QuickProtectMode.entries.associateWith { mode ->
                classifications.count {
                    QuickProtectMode.fromCategory(AppCategory.fromWireValue(it.category)) == mode
                }
            }
        }
    val categoryCounts =
        remember(classifications) {
            AppCategory.entries.associateWith { category ->
                classifications.count { AppCategory.fromWireValue(it.category) == category }
            }
        }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (quickProtectMode) {
            items(QuickProtectMode.entries, key = { it.name }) { mode ->
                Surface(
                    color = quickProtectColor(mode).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, quickProtectColor(mode).copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            mode.label.uppercase(),
                            color = quickProtectColor(mode),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "${quickCounts[mode] ?: 0} apps",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(AppCategory.entries, key = { it.wireValue }) { category ->
                Surface(
                    color = categoryColor(category).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, categoryColor(category).copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            category.wireValue,
                            color = categoryColor(category),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "${categoryCounts[category] ?: 0} apps",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickProtectAppCard(
    item: ReviewAppItem,
    onBlock: () -> Unit,
    onAsk: () -> Unit,
    onAllow: () -> Unit,
    onAdvanced: () -> Unit
) {
    val category = AppCategory.fromWireValue(item.classification.category)
    val currentMode = QuickProtectMode.fromCategory(category)
    val source = AppClassificationSource.fromWireValue(item.classification.source)
    Surface(
        color = SurfaceLighter,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, quickProtectColor(currentMode).copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AppIconImage(
                        packageName = item.classification.packageName,
                        modifier = Modifier.size(48.dp)
                    )
                    if (item.usageTimeMs > 0) {

                        Box(
                            modifier =
                                Modifier.size(12.dp)
                                    .background(usageLevelColor(item.usageLevel), CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.classification.appName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickProtectBadge(currentMode)
                        Text(
                            when (source) {
                                AppClassificationSource.SYSTEM_DEFAULT -> "Suggested by IronMind"
                                AppClassificationSource.USER_OVERRIDE -> "You changed this"
                                AppClassificationSource.USER_CONFIRMED -> "You confirmed this"
                                AppClassificationSource.UNKNOWN_PENDING -> "Needs a real decision"
                            },
                            color =
                                if (source == AppClassificationSource.UNKNOWN_PENDING) WarningAmber
                                else Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    if (item.usageTimeMs > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Last 24h: ${formatUsage(item.usageTimeMs)}",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickProtectActionChip(
                    text = "Block",
                    selected = currentMode == QuickProtectMode.BLOCKED,
                    color = quickProtectColor(QuickProtectMode.BLOCKED),
                    onClick = onBlock,
                    modifier = Modifier.weight(1f)
                )
                QuickProtectActionChip(
                    text = "Ask",
                    selected = currentMode == QuickProtectMode.ASK,
                    color = quickProtectColor(QuickProtectMode.ASK),
                    onClick = onAsk,
                    modifier = Modifier.weight(1f)
                )
                QuickProtectActionChip(
                    text = "Allow",
                    selected = currentMode == QuickProtectMode.ALLOWED,
                    color = quickProtectColor(QuickProtectMode.ALLOWED),
                    onClick = onAllow,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onAdvanced, contentPadding = PaddingValues(0.dp)) {
                Text(
                    "More options",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun QuickProtectActionChip(
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier,
        onClick = onClick,
        color = if (selected) color.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.14f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (selected) color else Color.White.copy(alpha = 0.10f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text.uppercase(),
                color = if (selected) color else Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun QuickProtectBadge(mode: QuickProtectMode) {

    Surface(
        color = quickProtectColor(mode).copy(alpha = 0.14f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            mode.label.uppercase(),
            color = quickProtectColor(mode),
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun UsageSignalCard() {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "USAGE SIGNALS ACTIVE",
                color = SuccessGreen,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "IronMind is reading your last-day usage to surface pressure apps first.",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReviewFocusCard(pendingCount: Int, reviewOnly: Boolean) {

    Surface(
        color = WarningAmber.copy(alpha = 0.10f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (reviewOnly) "FOCUSED REVIEW MODE" else "UNKNOWN APPS NEED A DECISION",
                color = WarningAmber,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.1.sp,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "$pendingCount apps are still unclassified enough to weaken your mission rules.",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Start with REVIEW NEEDED. Once they are classified, the queue clears itself.",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SectionLabel(title: String, subtitle: String) {

    Column {
        Text(
            title,
            color = Color.Gray,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, color = ElectricViolet, fontSize = 12.sp)
    }
}

@Composable
private fun SuggestedReviewChip(app: ReviewAppItem, onOpen: () -> Unit) {

    Surface(
        onClick = onOpen,
        color = SurfaceLighter,
        shape = RoundedCornerShape(24.dp),
        border =
            BorderStroke(
                1.dp,
                categoryColor(AppCategory.fromWireValue(app.classification.category))
                    .copy(alpha = 0.35f)
            ),
        modifier = Modifier.size(width = 128.dp, height = 152.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppIconImage(
                packageName = app.classification.packageName,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                app.classification.appName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            CategoryBadge(AppCategory.fromWireValue(app.classification.category))
            if (app.usageTimeMs > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(formatUsage(app.usageTimeMs), color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ReviewAppCard(item: ReviewAppItem, onOpen: () -> Unit) {
    val category = AppCategory.fromWireValue(item.classification.category)
    val source = AppClassificationSource.fromWireValue(item.classification.source)
    val color = categoryColor(category)
    Surface(
        onClick = onOpen,
        color = SurfaceLighter,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AppIconImage(
                    packageName = item.classification.packageName,
                    modifier = Modifier.size(48.dp)
                )
                if (item.usageTimeMs > 0)
                    Box(
                        modifier =
                            Modifier.size(12.dp)
                                .background(usageLevelColor(item.usageLevel), CircleShape)
                    )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.classification.appName,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(category)
                    Text(
                        when (source) {
                            AppClassificationSource.SYSTEM_DEFAULT -> "Default"
                            AppClassificationSource.USER_OVERRIDE -> "Overridden"
                            AppClassificationSource.USER_CONFIRMED -> "Confirmed"
                            AppClassificationSource.UNKNOWN_PENDING -> "Needs review"
                        },
                        color =
                            if (source == AppClassificationSource.UNKNOWN_PENDING) WarningAmber
                            else Color.Gray,
                        fontSize = 11.sp
                    )
                }
                if (item.usageTimeMs > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Last 24h: ${formatUsage(item.usageTimeMs)}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
            Icon(Icons.Rounded.Edit, contentDescription = null, tint = color)
        }
    }
}

@Composable
private fun CategoryBadge(category: AppCategory) {

    Surface(
        color = categoryColor(category).copy(alpha = 0.14f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            category.wireValue,
            color = categoryColor(category),
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    app: AppClassification,
    onDismiss: () -> Unit,
    onChoose: (AppCategory) -> Unit
) {
    val current = AppCategory.fromWireValue(app.category)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = {
            Column {
                Text(app.appName, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(app.packageName, color = Color.Gray, fontSize = 12.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "How should IronMind treat this app during missions?",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                AppCategory.entries.forEach { category ->
                    val selected = category == current
                    Surface(
                        modifier = Modifier.fillMaxWidth().gamifiedClick { onChoose(category) },
                        color =
                            if (selected) categoryColor(category).copy(alpha = 0.16f)
                            else SurfaceLighter,
                        shape = RoundedCornerShape(16.dp),
                        border =
                            BorderStroke(
                                1.dp,
                                if (selected) categoryColor(category)
                                else Color.White.copy(alpha = 0.08f)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    category.wireValue,
                                    color = categoryColor(category),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    category.missionTreatment,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                            if (selected)
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = categoryColor(category)
                                )
                        }
                    }
                }
            }
        },
        containerColor = DeepBackground
    )
}

@Composable
fun WebsiteBlockerCard(isEnabled: Boolean, onToggle: (Boolean) -> Unit, onConfigure: () -> Unit) {

    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier.size(40.dp)
                        .background(ElectricViolet.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Language, contentDescription = null, tint = ElectricViolet)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Block Distracting Websites",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text("Prevents browser usage during focus", color = Color.Gray, fontSize = 12.sp)
                if (isEnabled) {
                    TextButton(onClick = onConfigure, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            "Configure Sites ->",
                            color = ElectricViolet,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = ElectricViolet)
            )
        }
    }
}

@Composable
private fun PermissionGuardCard(onGrantClick: () -> Unit) {

    Surface(
        color = WarningAmber.copy(alpha = 0.10f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier.size(40.dp).background(WarningAmber.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = WarningAmber)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Usage access will improve suggestions.",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Without it, IronMind can still classify apps, but it cannot highlight the ones draining your day.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = onGrantClick) {
                Text("Grant", color = WarningAmber, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun buildCategorySections(
    items: List<ReviewAppItem>,
    pendingItems: List<ReviewAppItem>,
    reviewOnly: Boolean,
    quickProtectMode: Boolean
): List<CategorySection> {
    val pendingPackages = pendingItems.mapTo(hashSetOf()) { it.classification.packageName }
    val sections = mutableListOf<CategorySection>()
    if (pendingItems.isNotEmpty())
        sections +=
            CategorySection(
                "REVIEW NEEDED",
                "IronMind could not safely classify these yet. Decide them explicitly.",
                pendingItems
            )
    if (reviewOnly) return sections
    val nonPending = items.filterNot { it.classification.packageName in pendingPackages }
    if (quickProtectMode) {
        QuickProtectMode.entries.forEach { mode ->
            val modeItems =
                nonPending
                    .filter {
                        QuickProtectMode.fromCategory(
                            AppCategory.fromWireValue(it.classification.category)
                        ) == mode
                    }
                    .sortedWith(
                        compareByDescending<ReviewAppItem> { it.usageTimeMs }
                            .thenBy { it.classification.appName.lowercase() }
                    )
            if (modeItems.isNotEmpty()) {
                sections +=
                    CategorySection(quickProtectTitle(mode), quickProtectSubtitle(mode), modeItems)
            }
        }
    } else {
        AppCategory.entries.forEach { category ->
            val categoryItems =
                nonPending
                    .filter { AppCategory.fromWireValue(it.classification.category) == category }
                    .sortedWith(
                        compareByDescending<ReviewAppItem> { it.usageTimeMs }
                            .thenBy { it.classification.appName.lowercase() }
                    )
            if (categoryItems.isNotEmpty())
                sections +=
                    CategorySection(category.wireValue, categorySubtitle(category), categoryItems)
        }
    }
    return sections
}

private fun categorySubtitle(category: AppCategory): String =
    when (category) {
        AppCategory.VOID -> "Infinite-retention apps. These should not survive active missions."
        AppCategory.SIGNAL -> "Communication pressure. Legitimate in life, controlled during focus."
        AppCategory.TOOL -> "Clear utility apps. These stay safe unless you override them."
        AppCategory.CONTEXT ->
            "Useful sometimes, distracting other times. Declare them per mission later."
    }

private fun categoryColor(category: AppCategory): Color =
    when (category) {
        AppCategory.VOID -> ErrorRed
        AppCategory.SIGNAL -> SignalBlue
        AppCategory.TOOL -> SuccessGreen
        AppCategory.CONTEXT -> ContextPurple
    }

private fun quickProtectTitle(mode: QuickProtectMode): String =
    when (mode) {
        QuickProtectMode.BLOCKED -> "BLOCKED DURING MISSIONS"
        QuickProtectMode.ASK -> "ASK EACH MISSION"
        QuickProtectMode.ALLOWED -> "ALWAYS ALLOWED"
    }

private fun quickProtectSubtitle(mode: QuickProtectMode): String =
    when (mode) {
        QuickProtectMode.BLOCKED ->
            "These apps are treated as active distractions or communication pressure during focus."
        QuickProtectMode.ASK ->
            "These apps stay off unless you explicitly declare them for a mission."
        QuickProtectMode.ALLOWED -> "These apps are treated as safe tools during missions."
    }

private fun quickProtectColor(mode: QuickProtectMode): Color =
    when (mode) {
        QuickProtectMode.BLOCKED -> ErrorRed
        QuickProtectMode.ASK -> ContextPurple
        QuickProtectMode.ALLOWED -> SuccessGreen
    }

private fun usageLevelColor(level: Float): Color =
    when {
        level > 0.6f -> ErrorRed
        level > 0.3f -> WarningAmber
        else -> SuccessGreen
    }

private suspend fun syncLegacyBlockedApps(
    repo: AppClassificationRepository,
    prefs: PrefManager,
    context: Context,
    source: String,
    recordHistory: Boolean
) {
    val nextBlocked = repo.getLegacyBlockedPackagesSnapshot()
    val previousBlocked = prefs.blockedApps
    if (previousBlocked != nextBlocked) {
        prefs.blockedApps = nextBlocked
        if (recordHistory)
            HistoryRecorder.recordConfigChange(
                context,
                "BLOCKED_APPS",
                previousBlocked,
                nextBlocked,
                source
            )
    }
    sendGuardReload(context)
}

private fun sendGuardReload(context: Context) {
    context.sendBroadcast(
        Intent("com.ironmind.RELOAD_GUARD").apply { setPackage(context.packageName) }
    )
}

private fun loadUsageStats(context: Context): Map<String, Long> {

    if (!PermissionHelper.hasUsageStatsPermission(context)) return emptyMap()
    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return usageStatsManager
        .queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            calendar.timeInMillis,
            System.currentTimeMillis()
        )
        .associate { it.packageName to it.totalTimeInForeground }
}

private fun formatUsage(ms: Long): String {
    val mins = ms / 1000 / 60
    val hours = mins / 60
    return if (hours > 0) "${hours}h ${mins % 60}m" else "${mins}m"
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun AppSelectorScreenPreview() {
    if (!androidx.compose.ui.platform.LocalInspectionMode.current) return
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme { AppSelectorPreviewContent() }
}

@Composable
private fun AppSelectorPreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("QUICK PROTECT", style = MaterialTheme.typography.headlineSmall, color = WarningAmber, fontWeight = FontWeight.Black)
        Text("Pick the apps that break your focus fast.", color = Color.Gray)
        listOf("Instagram" to "VOID · 2h 14m", "YouTube" to "SIGNAL · 1h 06m", "Chrome" to "CONTEXT · 44m").forEach { (name, detail) ->
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(detail, color = Color.Gray, fontSize = 12.sp)
                    }
                    Text("PROTECT", color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
