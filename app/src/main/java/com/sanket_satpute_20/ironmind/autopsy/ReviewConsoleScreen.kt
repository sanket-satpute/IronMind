package com.sanket_satpute_20.ironmind.autopsy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun ReviewConsoleScreen(
    taskId: String = "",
    taskName: String = "Morning Run (5km)",
    onSubmit: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var rationalization by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            TextButton(onClick = onBack) { Text("← Escape", color = ErrorRed.copy(alpha = 0.5f)) }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "AUTOPSY CONSOLE",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text("You failed a commitment.", fontSize = 13.sp, color = ErrorRed)
                }
            }

            Spacer(Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(12.dp))
                    .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("FAILED TASK", fontSize = 10.sp, color = ErrorRed, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(taskName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(36.dp))

            Text("WHY DID YOU FAIL?", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Document your rationalization below. Be brutally honest. Excuses look pathetic when written down.",
                fontSize = 13.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = rationalization,
                onValueChange = { rationalization = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("I skipped because...", color = Color.DarkGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ErrorRed,
                    unfocusedBorderColor = DeepBackground,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            Spacer(Modifier.height(48.dp))

            val canSubmit = rationalization.trim().length > 10

            Button(
                onClick = { if (canSubmit) onSubmit(rationalization) },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    disabledContainerColor = SurfaceDark,
                    contentColor = Color.White,
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "LOG FAILURE",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewConsoleScreenPreview() {
    IronMindTheme { ReviewConsoleScreen() }
}