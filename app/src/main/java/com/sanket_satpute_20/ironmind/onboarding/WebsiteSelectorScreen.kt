package com.sanket_satpute_20.ironmind.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ElectricViolet
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

data class WebItem(
    val name: String,
    val url: String,
    val usageLevel: Float = 0f // 0 to 1
)

private val POPULAR_DISTRACTIONS = listOf(
    WebItem("YouTube", "youtube.com", 0.85f),
    WebItem("Instagram", "instagram.com", 0.70f),
    WebItem("Twitter / X", "twitter.com", 0.40f),
    WebItem("Reddit", "reddit.com", 0.55f),
    WebItem("Facebook", "facebook.com", 0.30f),
    WebItem("Netflix", "netflix.com", 0.20f),
    WebItem("TikTok", "tiktok.com", 0.90f),
    WebItem("Adult Content", "pornhub.com", 0.15f)
)

private val ErrorRed = ErrorRed
private val WarningAmber = WarningAmber
private val SuccessGreen = SurfaceElevated
private val SurfaceLighter = SurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteSelectorScreen(onBack: () -> Unit = {}, onNext: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    val selected = remember { mutableStateListOf<String>() }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        selected.addAll(prefs.blockedWebsites)
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("WEBSITE BLOCKER",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricViolet,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Website")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("POPULAR DISTRACTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(POPULAR_DISTRACTIONS) { item ->
                    WebUsageCard(
                        item = item,
                        isSelected = selected.contains(item.url),
                        onToggle = {
                            if (selected.contains(item.url)) selected.remove(item.url)
                            else selected.add(item.url)
                            val previousBlockedWebsites = prefs.blockedWebsites
                            prefs.blockedWebsites = selected.toSet()
                            HistoryRecorder.recordConfigChange(context, "BLOCKED_WEBSITES", previousBlockedWebsites, prefs.blockedWebsites, "WEBSITE_SELECTOR")
                        }
                    )
                }

                if (selected.any { url -> POPULAR_DISTRACTIONS.none { it.url == url } }) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("CUSTOM BLOCKED SITES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(selected.filter { url -> POPULAR_DISTRACTIONS.none { it.url == url } }.toList()) { url ->
                        WebUsageCard(
                            item = WebItem(url, url, 0f),
                            isSelected = true,
                            onToggle = {
                                selected.remove(url)
                                val previousBlockedWebsites = prefs.blockedWebsites
                                prefs.blockedWebsites = selected.toSet()
                                HistoryRecorder.recordConfigChange(context, "BLOCKED_WEBSITES", previousBlockedWebsites, prefs.blockedWebsites, "WEBSITE_SELECTOR")
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddWebsiteDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, url ->
                if (url.isNotEmpty()) {
                    val cleanUrl = url.lowercase().removePrefix("http://").removePrefix("https://").removePrefix("www.")
                    if (!selected.contains(cleanUrl)) {
                        selected.add(cleanUrl)
                        val previousBlockedWebsites = prefs.blockedWebsites
                        prefs.blockedWebsites = selected.toSet()
                        HistoryRecorder.recordConfigChange(context, "BLOCKED_WEBSITES", previousBlockedWebsites, prefs.blockedWebsites, "WEBSITE_SELECTOR")
                    }
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WebUsageCard(item: WebItem, isSelected: Boolean, onToggle: () -> Unit) {
    val usageColor = when {
        item.usageLevel > 0.6f -> ErrorRed
        item.usageLevel > 0.3f -> WarningAmber
        else -> SuccessGreen
    }

    Surface(
        onClick = onToggle,
        color = if (isSelected) ElectricViolet.copy(alpha = 0.1f) else SurfaceLighter,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) ElectricViolet.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.03f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).background(usageColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Language, contentDescription = null, tint = usageColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(item.url, fontSize = 11.sp, color = Color.Gray)
            }
            
            if (item.usageLevel > 0) {
                CircularProgressIndicator(
                    progress = { item.usageLevel },
                    modifier = Modifier.size(24.dp),
                    color = usageColor,
                    strokeWidth = 2.dp,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            if (isSelected) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = ElectricViolet)
            }
        }
    }
}

@Composable
fun AddWebsiteDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLighter,
        title = { Text("Add Custom Website", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Site Name (e.g. My Distraction)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL (e.g. distraction.com)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, url) }, colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun WebsiteSelectorScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        WebsiteSelectorScreen(onBack = {}, onNext = {})
    }
}