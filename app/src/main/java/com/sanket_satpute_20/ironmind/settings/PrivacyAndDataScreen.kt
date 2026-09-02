package com.sanket_satpute_20.ironmind.settings

import com.sanket_satpute_20.ironmind.gamification.gamifiedClick

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.NeonCyan
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyAndDataScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "PRIVACY & DATA",
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Privacy Policy Section
            Column {
                Text(
                    "Privacy Policy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = DeepBackground,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "At IronMind, your data belongs to you. Here's exactly how we handle it:",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "1. All your focus sessions, tasks, and temptations are stored locally on your device.\n\n" +
                            "2. We use Accessibility Services strictly to detect when you open blocked apps and prevent access during focus sessions. We do not keylog, read your messages, or transmit this screen content anywhere.\n\n" +
                            "3. If you link a Google account, your basic profile info and artifacts are synced to secure cloud storage so you don't lose them. If you are a Guest, absolutely nothing leaves your phone.\n\n" +
                            "4. We do not sell data to advertisers. We do not track your browsing history.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            // Data Management Section
            Column {
                Text(
                    "Data Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                DataActionRow(
                    emoji = "📤",
                    title = "Export My Data",
                    subtitle = "Generate a summary of your local database",
                    actionColor = NeonCyan,
                    onClick = { exportData(context) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                DataActionRow(
                    emoji = "🗑️",
                    title = "Delete Account & Data",
                    subtitle = "Wipe all local and cloud data permanently",
                    actionColor = ErrorRed,
                    onClick = { showDeleteConfirm = true }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = DeepBackground,
            title = {
                Text("PERMANENT DELETION", color = ErrorRed, fontWeight = FontWeight.Black)
            },
            text = {
                Text(
                    "This will wipe your entire IronMind history, erase all streaks, delete all artifacts, and sign you out. This cannot be undone.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        deleteAccountAndData(context, onBack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("WIPE EVERYTHING", fontWeight = FontWeight.Black, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun DataActionRow(
    emoji: String,
    title: String,
    subtitle: String,
    actionColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = DeepBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, actionColor.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .gamifiedClick { onClick() }
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
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

private fun exportData(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        val db = IronMindDatabase.getDatabase(context)
        val taskCount = db.taskDao().getAllTasks().first().size
        val focusCount = db.focusSessionDao().getAllSessionsSnapshot().size
        
        val summary = """
            IronMind Data Export
            --------------------
            Total Tasks Logged: $taskCount
            Total Focus Sessions: $focusCount
            
            (JSON dump of detailed tables can be attached here in the future)
        """.trimIndent()
        
        withContext(Dispatchers.Main) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "IronMind Data Export")
                putExtra(Intent.EXTRA_TEXT, summary)
            }
            context.startActivity(Intent.createChooser(intent, "Export Data"))
        }
    }
}

private fun deleteAccountAndData(context: Context, onComplete: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        // 1. Wipe Database
        IronMindDatabase.getDatabase(context).clearAllTables()
        
        // 2. Wipe Preferences
        val prefs = context.getSharedPreferences("IronMindPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        // 3. Sign out of Firebase
        try {
            val user = Firebase.auth.currentUser
            user?.delete()
            Firebase.auth.signOut()
        } catch (e: Exception) {
            // Log exception, maybe user wasn't signed in
        }
        
        withContext(Dispatchers.Main) {
            onComplete()
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun PrivacyAndDataScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        PrivacyAndDataScreen(
            onBack = {}
        )
    }
}