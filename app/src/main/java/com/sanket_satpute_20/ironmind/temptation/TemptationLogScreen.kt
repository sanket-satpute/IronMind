package com.sanket_satpute_20.ironmind.temptation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun TemptationLogScreen(
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔥", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "TEMPTATION LOG",
            color = ErrorRed,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Every urge you resisted. Evidence of your iron.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onBack) {
            Text("Back", color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TemptationLogScreenPreview() {
    IronMindTheme { TemptationLogScreen() }
}