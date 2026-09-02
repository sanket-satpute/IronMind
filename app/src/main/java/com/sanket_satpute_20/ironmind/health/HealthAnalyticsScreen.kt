package com.sanket_satpute_20.ironmind.health

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber
import com.sanket_satpute_20.ironmind.ui.components.PremiumScreenShimmer
import com.sanket_satpute_20.ironmind.ui.components.AnimatedEntry

// --- Premium Palette ---
private val AccentBlue = NeonCyan
private val DeepBlue = SurfaceElevated
private val FocusOrange = WarningAmber
private val CardBg = DeepBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAnalyticsScreen(
    onBack: () -> Unit,
    onOpenPremiumUpgrade: () -> Unit,
    viewModel: HealthAnalyticsViewModel? = if (androidx.compose.ui.platform.LocalInspectionMode.current) null else viewModel()
) {
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val uiState by if (isPreview) {
        remember { mutableStateOf(HealthUiState.Success(listOf(), "Preview insight.", SleepSource.HEALTH_CONNECT)) }
    } else {
        viewModel!!.uiState.collectAsState()
    }
    val context = LocalContext.current
    val healthManager = if (isPreview) null else remember { HealthConnectManager(context) }
    val isPremium = if (isPreview) true else remember { com.sanket_satpute_20.ironmind.data.PrefManager.getInstance(context).isPremium }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { result ->
        if (result.isNotEmpty()) viewModel?.loadData()
    }

    LaunchedEffect(Unit) {
        if (!isPreview) viewModel?.loadData()
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column {
                // Window Insets / Status Bar Spacing
                Spacer(Modifier.statusBarsPadding())
                
                CenterAlignedTopAppBar(
                    title = { 
                        Text("RECOVERY ANALYSIS", 
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp
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
        }
    ) { padding ->
        AnimatedEntry(modifier = Modifier.fillMaxSize().padding(padding).background(DeepBackground)) {
            when (val state = uiState) {
                is HealthUiState.Loading -> {
                    PremiumScreenShimmer()
                }
                is HealthUiState.NoPermission -> {
                    PermissionRationaleUI(onGrant = { if (!isPreview) permissionLauncher.launch(healthManager!!.permissions) })
                }
                is HealthUiState.NoData -> {
                    NoSleepDataUI(onDownloadFit = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.fitness"))
                        context.startActivity(intent)
                    })
                }
                is HealthUiState.Success -> {
                    AnalyticsContent(state.correlations, state.insight, state.source, isPremium, onOpenPremiumUpgrade)
                }
                is HealthUiState.Error -> {
                    ErrorUI(state.message, onRetry = { viewModel?.loadData() })
                }
                else -> {}
            }
        }
    }
}

@Composable
fun AnalyticsContent(correlations: List<DailyCorrelation>, insight: String, source: SleepSource, isPremium: Boolean, onOpenPremiumUpgrade: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // --- Data Source Badge ---
        Surface(
            color = if (source == SleepSource.HEALTH_CONNECT) DeepBlue.copy(alpha = 0.2f) else SurfaceElevated.copy(alpha = 0.2f),
            shape = CircleShape,
            border = BorderStroke(1.dp, if (source == SleepSource.HEALTH_CONNECT) DeepBlue else SuccessGreen),
            modifier = Modifier.align(Alignment.Start)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (source == SleepSource.HEALTH_CONNECT) Icons.Rounded.Sync else Icons.Rounded.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (source == SleepSource.HEALTH_CONNECT) AccentBlue else SuccessGreen
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (source == SleepSource.HEALTH_CONNECT) "HEALTH CONNECT SYNCED" else "PHONE INACTIVITY ESTIMATE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "RECOVERY vs FOCUS",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        // --- Chart Container ---
        Surface(
            color = CardBg,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth().height(360.dp)
        ) {
            if (!isPremium) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(20.dp).blur(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            CorrelationChart(correlations)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem("Sleep (h)", AccentBlue)
                            Spacer(Modifier.width(24.dp))
                            LegendItem("Focus Score", FocusOrange)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("PRO ANALYSIS", color = WarningAmber, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenPremiumUpgrade,
                                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("UNLOCK PRO", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        CorrelationChart(correlations)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem("Sleep (h)", AccentBlue)
                        Spacer(Modifier.width(24.dp))
                        LegendItem("Focus Score", FocusOrange)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Insight Card ---
        Text(
            "RECOVERY INSIGHT",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Surface(
            color = FocusOrange.copy(alpha = 0.05f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, FocusOrange.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = FocusOrange, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = insight,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun CorrelationChart(correlations: List<DailyCorrelation>) {
    AndroidView(
        factory = { context ->
            CombinedChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setPinchZoom(false)
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = android.graphics.Color.GRAY
                    textSize = 10f
                    yOffset = 10f
                    granularity = 1f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index in correlations.indices) correlations[index].date.takeLast(5) else ""
                        }
                    }
                }

                axisLeft.apply {
                    textColor = android.graphics.Color.GRAY
                    setDrawGridLines(true)
                    gridColor = android.graphics.Color.parseColor("#1AFFFFFF")
                    axisMinimum = 0f
                    axisMaximum = 12f
                    textSize = 10f
                }

                axisRight.apply {
                    textColor = android.graphics.Color.parseColor("#FF9800")
                    setDrawGridLines(false)
                    axisMinimum = 0f
                    axisMaximum = 6f
                    textSize = 10f
                }
            }
        },
        update = { chart ->
            val barEntries = correlations.mapIndexed { i, d -> BarEntry(i.toFloat(), d.sleepHours) }
            val lineEntries = correlations.mapIndexed { i, d -> Entry(i.toFloat(), d.avgFocusScore) }

            val barDataSet = BarDataSet(barEntries, "Sleep").apply {
                color = android.graphics.Color.parseColor("#2196F3")
                setDrawValues(false)
                axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT
            }

            val lineDataSet = LineDataSet(lineEntries, "Focus").apply {
                color = android.graphics.Color.parseColor("#FF9800")
                lineWidth = 3f
                setCircleColor(android.graphics.Color.parseColor("#FF9800"))
                circleRadius = 4f
                setDrawCircleHole(true)
                circleHoleColor = android.graphics.Color.BLACK
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.RIGHT
            }

            chart.data = CombinedData().apply {
                setData(BarData(barDataSet).apply { barWidth = 0.4f })
                setData(LineData(lineDataSet))
            }
            chart.animateY(1000)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun NoSleepDataUI(onDownloadFit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp).background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("NO SLEEP SIGNALS", 
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
            color = Color.White)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "We scanned Health Connect and device inactivity patterns, but found no valid data for the past 7 days.",
            textAlign = TextAlign.Center, color = Color.Gray, fontSize = 15.sp, lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Surface(
            color = CardBg,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("OPTIMIZATION PATH", color = FocusOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                StepItem("1", "Install Google Fit or Fitbit", "Wearables provide the highest accuracy.")
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                StepItem("2", "Enable Health Sync", "In app settings, link to Health Connect.")
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                StepItem("3", "Maintain Bedtime", "Keep your phone idle during sleep hours.")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onDownloadFit,
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("GET GOOGLE FIT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun StepItem(num: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(num, color = FocusOrange, fontWeight = FontWeight.Black, fontSize = 14.sp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PermissionRationaleUI(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Shield, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text("SECURE ACCESS", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "IronMind cross-references your physical recovery with mental output. Allow read access to Health Connect to enable this analysis.",
            textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp, lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("INITIALIZE CONNECTION", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ErrorUI(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("SYSTEM ERROR", color = Color.White, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onRetry) {
            Text("RETRY INITIALIZATION", color = AccentBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun HealthAnalyticsScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        HealthAnalyticsScreen(
            onBack = {},
            onOpenPremiumUpgrade = {}
        )
    }
}
