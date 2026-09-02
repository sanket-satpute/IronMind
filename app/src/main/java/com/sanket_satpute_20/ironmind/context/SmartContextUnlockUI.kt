package com.sanket_satpute_20.ironmind.context

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.GoldXP

@Composable
fun SmartContextUnlockUI(
    onUnlockComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        SmartContextUnlockPreviewContent(onDismiss)
        return
    }
    val context = LocalContext.current
    val pref = remember { PrefManager.getInstance(context) }
    
    var step by remember { mutableStateOf(0) } // 0: Intro, 1: Location, 2: Battery

    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    var isBatteryOptimized by remember { 
        mutableStateOf(!powerManager.isIgnoringBatteryOptimizations(context.packageName)) 
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val activity = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        
        if (fineLocation || activity) {
            step = 2 // Move to battery
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.95f)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (step) {
                0 -> {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "UNLOCK SMART CONTEXT",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "IronMind can automatically detect when you are at Home or Work, dynamically shifting modes without you needing to do anything.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { step = 1 },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("PROCEED", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onDismiss) {
                        Text("NOT NOW", color = Color.Gray)
                    }
                }
                
                1 -> {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "GRANT LOCATION & ACTIVITY",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "We need Location to cluster your frequent coordinates, and Activity Recognition to save battery while you move. This data never leaves your device.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            locationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACTIVITY_RECOGNITION
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("GRANT PERMISSIONS", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
                
                2 -> {
                    if (isBatteryOptimized) {
                        Icon(
                            Icons.Rounded.BatteryAlert,
                            contentDescription = null,
                            tint = GoldXP,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "PROTECT IRONMIND",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your phone will kill IronMind while you sleep to save battery, breaking your alarms and trackers. You MUST disable battery optimization for IronMind.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldXP),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("DISABLE RESTRICTIONS", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = {
                                // Double check if it actually worked (sometimes intents don't update state immediately, 
                                // but we let them proceed if they click 'I DID THIS')
                                pref.hasUnlockedSmartContext = true
                                val serviceIntent = Intent(context, ContextIntelligenceService::class.java)
                                context.startService(serviceIntent)
                                onUnlockComplete()
                            }
                        ) {
                            Text("I'VE DONE THIS / SKIP", color = Color.Gray)
                        }
                    } else {
                        // Already unrestricted
                        LaunchedEffect(Unit) {
                            pref.hasUnlockedSmartContext = true
                            val serviceIntent = Intent(context, ContextIntelligenceService::class.java)
                            context.startService(serviceIntent)
                            onUnlockComplete()
                        }
                    }
                }
            }
        }
    } // End of Surface
    } // End of Dialog
}

@Composable
private fun SmartContextUnlockPreviewContent(onDismiss: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.95f)) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(24.dp))
            Text("UNLOCK SMART CONTEXT", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))
            Text("IronMind can shift modes automatically when you arrive at Home or Work.", color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                Text("PROCEED", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss) { Text("NOT NOW", color = Color.Gray) }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun SmartContextUnlockUIPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        SmartContextUnlockUI(onUnlockComplete = {}, onDismiss = {})
    }
}
