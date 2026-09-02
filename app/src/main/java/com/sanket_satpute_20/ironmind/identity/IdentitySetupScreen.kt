package com.sanket_satpute_20.ironmind.identity

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.SuccessGreen
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

private val SuccessGreen = SurfaceElevated
private val WarningAmber = WarningAmber
private val SurfaceLighter = SurfaceElevated

@Composable
fun IdentitySetupScreen(onNext: () -> Unit = {}, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    val identities = remember { mutableStateListOf<String>() }
    var currentInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        identities.addAll(prefs.identityStatements)
    }

    val examples = listOf(
        "I am someone who never misses a morning run",
        "I am a developer who codes every single day",
        "I am a person who follows through on their word"
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(56.dp))
                Text(
                    "IDENTITY ANCHOR",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = SuccessGreen
                )
                Text(
                    "Who are you becoming?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Goals are what you do. Identity is who you are. Define yourself with \"I am...\" statements.",
                    fontSize = 14.sp, color = Color.Gray, lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Examples Section ---
                item {
                    Text("INSPIRATION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    examples.forEach { example ->
                        Surface(
                            onClick = { if (!identities.contains(example)) identities.add(example) },
                            color = SurfaceLighter,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
                        ) {
                            Text(
                                "+ \"$example\"",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp, color = Color.Gray, fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }

                // --- Input Section ---
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("YOUR TRUTH", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        placeholder = { Text("I am someone who...", color = Color.DarkGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = { if (currentInput.length >= 5) { identities.add(currentInput.trim()); currentInput = "" } },
                                modifier = Modifier.background(SuccessGreen, CircleShape).size(32.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SuccessGreen,
                            unfocusedBorderColor = SurfaceLighter,
                            focusedContainerColor = SurfaceLighter,
                            unfocusedContainerColor = SurfaceLighter,
                            focusedTextColor = Color.White
                        )
                    )
                }

                // --- Identity List ---
                itemsIndexed(identities) { index, identity ->
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "\"$identity\"",
                                modifier = Modifier.weight(1f),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                lineHeight = 22.sp
                            )
                            IconButton(onClick = { identities.removeAt(index) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        // --- Persistent Footer ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = {
                    val previousIdentities = prefs.identityStatements
                    val previousFoundationComplete = prefs.identitySetupComplete
                    prefs.identityStatements = identities.toSet()
                    prefs.identitySetupComplete = identities.isNotEmpty()
                    HistoryRecorder.recordConfigChange(context, "IDENTITY_STATEMENTS", previousIdentities, prefs.identityStatements, "IDENTITY_SETUP")
                    HistoryRecorder.recordConfigChange(context, "IDENTITY_SETUP_COMPLETE", previousFoundationComplete, prefs.identitySetupComplete, "IDENTITY_SETUP")
                    onNext()
                },
                enabled = identities.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (identities.isNotEmpty()) SuccessGreen else SurfaceLighter
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (identities.isNotEmpty()) "THIS IS MY IDENTITY →" else "DEFINE YOUR IDENTITY",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

fun getRandomIdentity(context: Context): String? {
    return PrefManager.getInstance(context).identityStatements.randomOrNull()
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun IdentitySetupScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        IdentitySetupScreen(onNext = {}, onBack = {})
    }
}