package com.sanket_satpute_20.ironmind.psychology

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.PrefManager
import java.time.LocalDate
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun HonestyOverridePanel(
    lockType: String,
    onOverride: () -> Unit,
    onCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    var expanded by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    val isReasonValid = reason.trim().length >= 10

    if (!prefs.honestyModeEnabled) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!expanded) {
            TextButton(onClick = { expanded = true }) {
                Text(
                    "I need to override this",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 12.sp
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically()
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "HONESTY OVERRIDE",
                        fontSize = 11.sp,
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Write why you need to leave. This will be logged " +
                        "and visible in your stats - no judgment, just honesty.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BasicTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(WarningAmber),
                        minLines = 2,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (reason.isNotEmpty()) WarningAmber.copy(alpha = 0.4f)
                                        else SurfaceElevated,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                if (reason.isEmpty()) {
                                    Text(
                                        "Why do you need to leave right now? (min 10 chars)",
                                        color = SurfaceElevated,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { expanded = false; reason = ""; onCancel() }) {
                            Text("Cancel", color = Color.Gray, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                prefs.honestyOverrideCount++
                                prefs.lastHonestyOverrideDate = LocalDate.now().toString()
                                HistoryRecorder.recordConfigChange(
                                    context,
                                    "HONESTY_OVERRIDE",
                                    lockType,
                                    reason.trim(),
                                    "HONESTY_MODE"
                                )
                                onOverride()
                            },
                            enabled = isReasonValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isReasonValid) WarningAmber
                                else SurfaceElevated
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Override",
                                fontWeight = FontWeight.Bold,
                                color = if (isReasonValid) Color.Black else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun HonestyOverridePanelPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        HonestyOverridePanel(lockType = "Doomscrolling", onOverride = {}, onCancel = {})
    }
}