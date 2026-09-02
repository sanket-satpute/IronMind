package com.sanket_satpute_20.ironmind.accountability

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

@Composable
fun AccountabilitySetupScreen(
    onBack: () -> Unit = {},
    onSavePartner: (email: String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }

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
            TextButton(onClick = onBack) { Text("← Back", color = Color.Gray) }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Security,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "ACCOUNTABILITY",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text("The ultimate failure deterrent", fontSize = 13.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(32.dp))

            // How it works card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        Brush.verticalGradient(listOf(WarningAmber.copy(0.4f), SurfaceElevated)),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Gavel, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("HOW IT WORKS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarningAmber, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    RuleItem("1. Assign a partner you respect (or fear).")
                    RuleItem("2. If you skip a task, they get an automated email.")
                    RuleItem("3. The email details exactly what you failed to do.")
                    RuleItem("4. The social pressure forces you to act.")
                }
            }

            Spacer(Modifier.height(36.dp))

            Text("PARTNER EMAIL", fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter partner's email address", color = Color.DarkGray) },
                leadingIcon = {
                    Icon(Icons.Rounded.Email, contentDescription = null, tint = Color.Gray)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarningAmber,
                    unfocusedBorderColor = SurfaceElevated,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = WarningAmber
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Make sure they agree to hold you accountable before adding them.",
                fontSize = 12.sp,
                color = Color.Gray.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(36.dp))

            val isValid = email.contains("@") && email.contains(".")

            Button(
                onClick = { if (isValid) onSavePartner(email) },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningAmber,
                    disabledContainerColor = SurfaceDark,
                    contentColor = Color.Black,
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "LOCK IN PARTNER",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun RuleItem(text: String) {
    Row(modifier = Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.Top) {
        Text(text, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), lineHeight = 18.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun AccountabilitySetupScreenPreview() {
    IronMindTheme { AccountabilitySetupScreen() }
}