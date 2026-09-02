package com.sanket_satpute_20.ironmind.contract

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Gavel
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
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun CommitmentContractScreen(
    onSigned: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }

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
            // Back
            TextButton(onClick = onBack) { Text("← Back", color = Color.Gray) }

            Spacer(Modifier.height(16.dp))

            // Icon + title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Gavel,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "COMMITMENT CONTRACT",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text("A promise to your future self", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(28.dp))

            // The contract document
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        Brush.verticalGradient(listOf(ErrorRed.copy(alpha = 0.5f), SurfaceElevated)),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "⚡ IRONMIND CONTRACT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed,
                        letterSpacing = 2.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    ContractLine("I commit to showing up daily, regardless of mood or motivation.")
                    ContractLine("I commit to completing every task I set for myself.")
                    ContractLine("I commit to confronting my skip history with honesty.")
                    ContractLine("I commit to never rationalizing failure as acceptable.")
                    ContractLine("I understand that discipline compounds over time.")
                    ContractLine("I choose discomfort now over regret later.")

                    Spacer(Modifier.height(20.dp))

                    HorizontalDivider(color = SurfaceElevated)

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Signed by:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Your name", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ErrorRed,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = ErrorRed
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Agreement checkbox
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ErrorRed,
                        uncheckedColor = Color.Gray
                    )
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "I understand this is a personal commitment to growth. No one is forcing me. I choose this path.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            val canSign = userName.trim().isNotEmpty() && agreed

            Button(
                onClick = { if (canSign) onSigned() },
                enabled = canSign,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    disabledContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "SIGN THE CONTRACT",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ContractLine(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("→", color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun CommitmentContractScreenPreview() {
    IronMindTheme { CommitmentContractScreen() }
}
