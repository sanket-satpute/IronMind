package com.sanket_satpute_20.ironmind.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PeopleAlt
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme
import com.sanket_satpute_20.ironmind.ui.theme.DeepBackground
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceDark
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed
import com.sanket_satpute_20.ironmind.ui.theme.WarningAmber

private val suggestedContacts = listOf(
    Triple("Neha R.", "neha.r@email.com", 22),
    Triple("Karan T.", "karan.t@email.com", 15),
    Triple("Divya M.", "divya.m@email.com", 8),
    Triple("Suresh P.", "suresh.p@email.com", 31),
)

@Composable
fun ContactDiscoveryScreen(
    onBack: () -> Unit = {},
    onManageCircle: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredContacts = suggestedContacts.filter {
        it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true)
    }
    val invitedSet = remember { mutableStateSetOf<String>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBackground)
                    .padding(24.dp)
            ) {
                Column {
                    TextButton(onClick = onBack) { Text("← Back", color = Color.Gray) }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.PeopleAlt,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "DISCOVER CONTACTS",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Find people to hold you accountable",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = { Text("Search by name or email...", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ErrorRed,
                    unfocusedBorderColor = SurfaceElevated,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = ErrorRed
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Manage circle button
            OutlinedButton(
                onClick = onManageCircle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
            ) {
                Text("Manage My Circle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "SUGGESTED FROM CONTACTS",
                fontSize = 11.sp,
                color = Color.Gray,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredContacts) { (name, email, streak) ->
                    val alreadyInvited = name in invitedSet
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DeepBackground, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(SurfaceElevated, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name.first().toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("${streak}d streak", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Button(
                            onClick = { invitedSet.add(name) },
                            enabled = !alreadyInvited,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (alreadyInvited) SurfaceDark else ErrorRed,
                                contentColor = if (alreadyInvited) Color.Gray else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (alreadyInvited) "Invited" else "Invite", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactDiscoveryScreenPreview() {
    IronMindTheme { ContactDiscoveryScreen() }
}
