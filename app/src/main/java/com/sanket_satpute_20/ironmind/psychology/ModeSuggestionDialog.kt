package com.sanket_satpute_20.ironmind.psychology

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sanket_satpute_20.ironmind.data.PrefManager

@Composable
fun ModeSuggestionDialog(
    onDecline : () -> Unit,
    onWrongRecommendation: (AppMode) -> Unit,
    onAccept  : (AppMode) -> Unit
) {
    val context = LocalContext.current
    val prefs = PrefManager.getInstance(context)
    var suggestion by remember { mutableStateOf<ModeSuggestion?>(null) }

    LaunchedEffect(Unit) {
        suggestion = BehaviourWatcher.analyseAndSuggest(context)
    }

    if (suggestion != null) {
        val s = suggestion!!
        val modeColor = Color(s.suggestedMode.color)

        Dialog(onDismissRequest = onDecline) {
            Surface(
                color  = modeColor.copy(alpha = 0.1f),
                shape  = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, modeColor.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(s.suggestedMode.emoji, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "SUGGESTION: SWITCH TO ${s.suggestedMode.label.uppercase()}",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Black,
                        color      = modeColor,
                        textAlign  = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        s.reason,
                        fontSize  = 14.sp,
                        color     = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            AdaptiveEngine.switchMode(
                                prefs = prefs,
                                newMode = s.suggestedMode,
                                context = context,
                                previousMode = prefs.appMode,
                                source = "MODE_SUGGESTION_DIALOG"
                            )
                            onAccept(s.suggestedMode)
                            suggestion = null 
                        },
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = modeColor
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Accept: Switch to ${s.suggestedMode.label}",
                            fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { 
                        onDecline()
                        suggestion = null
                    }) {
                        Text("Not right now", color = Color.Gray)
                    }
                    TextButton(onClick = { 
                        onWrongRecommendation(s.suggestedMode)
                        suggestion = null
                    }) {
                        Text("This recommendation is wrong", color = Color.Red.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@androidx.compose.runtime.Composable
fun ModeSuggestionDialogPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        ModeSuggestionDialog(
            onDecline = {},
            onWrongRecommendation = {},
            onAccept = {}
        )
    }
}