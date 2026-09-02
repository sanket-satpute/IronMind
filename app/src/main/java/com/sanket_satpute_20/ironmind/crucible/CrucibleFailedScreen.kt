package com.sanket_satpute_20.ironmind.crucible

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun CrucibleFailedScreen(
    crucibleTitle: String = "The Deep Work Crucible",
    onContinue: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Close, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(40.dp))
            }
            
            Spacer(Modifier.height(32.dp))

            Text(
                "CRUCIBLE FAILED",
                color = ErrorRed,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "You aborted $crucibleTitle.",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "You survived for 14 minutes before quitting. Your weakness has been logged. Streak penalized.",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(48.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("CONSEQUENCE", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("-2 Days from Streak", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ACCEPT AND CONTINUE", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrucibleFailedScreenPreview() {
    IronMindTheme { CrucibleFailedScreen() }
}