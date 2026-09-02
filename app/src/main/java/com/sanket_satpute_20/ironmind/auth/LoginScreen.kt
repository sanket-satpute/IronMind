package com.sanket_satpute_20.ironmind.auth

import android.util.Patterns
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanket_satpute_20.ironmind.BuildConfig
import com.sanket_satpute_20.ironmind.data.FirebaseSyncRepository
import com.sanket_satpute_20.ironmind.data.HistoryRecorder
import com.sanket_satpute_20.ironmind.data.IronMindDatabase
import com.sanket_satpute_20.ironmind.data.PrefManager
import com.sanket_satpute_20.ironmind.data.TaskDao
import com.sanket_satpute_20.ironmind.home.TaskViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    taskViewModel: TaskViewModel,
    firebaseSyncRepository: FirebaseSyncRepository,
    taskDao: TaskDao
) {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        LoginPreviewContent()
        return
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = Firebase.auth
    
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val credentialManager = remember { CredentialManager.create(context) }

    val handleGoogleSignIn: () -> Unit = {
        coroutineScope.launch {
            try {
                isLoading = true
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                val user = auth.currentUser
                if (user != null && user.isAnonymous) {
                    val name = taskViewModel.secureIdentity(firebaseCredential)
                    if (name == null) {
                        Log.d("SANKET_ERROR_1", "Link failed (likely already in use), falling back to Sign In.")
                        auth.signInWithCredential(firebaseCredential).await()
                        auth.currentUser?.reload()?.await()
                    } else {
                        Log.d("SANKET_SUCCESS_1", "Successfully linked: $name")
                    }
                } else {
                    auth.signInWithCredential(firebaseCredential).await()
                    auth.currentUser?.reload()?.await()
                }

                val tasks = firebaseSyncRepository.downloadAllTasks()
                if (tasks.isNotEmpty()) {
                    taskDao.insertAllTasks(tasks)
                }

                val prefs = PrefManager.getInstance(context)
                firebaseSyncRepository.fetchCloudUserStateSnapshot()?.let { snapshot ->
                    HistoryRecorder.applyUserStateSnapshotToPrefs(snapshot, prefs)
                }
                HistoryRecorder.recordUserStateSnapshot(context)
                firebaseSyncRepository.uploadUserStateSnapshot(HistoryRecorder.buildUserStateSnapshot(context))
                firebaseSyncRepository.syncStructuredHistory(IronMindDatabase.getDatabase(context))

                taskViewModel.refreshUser()
                onLoginSuccess()
            } catch (e: GetCredentialException) {
                Log.e("SANKET_ERROR_1", "Google Sign-in failed: ${e.message}")
                Toast.makeText(context, "Google Sign-in Unavailable", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SANKET_ERROR_1", "Fatal Google Auth Error: ${e.localizedMessage}", e)
                Toast.makeText(context, "Sign-in Failed", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isRegisterMode) "CREATE IDENTITY" else "ACCESS IDENTITY",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
            
            Text(
                text = if (isRegisterMode) "Secure your progress in the cloud." else "Resume your journey into discipline.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ErrorRed,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ErrorRed,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) return@Button
                    if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        Toast.makeText(context, "Enter a valid email address.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password.length < 6) {
                        Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch {
                        try {
                            isLoading = true
                            if (isRegisterMode) {
                                auth.createUserWithEmailAndPassword(email.trim(), password).await()
                                // Save profile details to Firestore for email users too
                                taskViewModel.refreshUser()
                            } else {
                                auth.signInWithEmailAndPassword(email.trim(), password).await()
                            }
                            
                            val tasks = firebaseSyncRepository.downloadAllTasks()
                            if (tasks.isNotEmpty()) taskDao.insertAllTasks(tasks)
                            
                            val prefs = PrefManager.getInstance(context)
                            firebaseSyncRepository.fetchCloudUserStateSnapshot()?.let { snapshot ->
                                HistoryRecorder.applyUserStateSnapshotToPrefs(snapshot, prefs)
                            }
                            HistoryRecorder.recordUserStateSnapshot(context)
                            firebaseSyncRepository.uploadUserStateSnapshot(HistoryRecorder.buildUserStateSnapshot(context))
                            firebaseSyncRepository.syncStructuredHistory(IronMindDatabase.getDatabase(context))
                            
                            taskViewModel.refreshUser()
                            onLoginSuccess()
                        } catch (e: Exception) {
                            Log.e("SANKET_ERROR_1", "Auth Failure: ${e.message}")
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(text = if (isRegisterMode) "REGISTER" else "LOGIN", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.DarkGray)
                Text(" OR ", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { handleGoogleSignIn() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.AccountCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("CONTINUE WITH GOOGLE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                Text(
                    text = if (isRegisterMode) "Already have an account? Login" else "Don't have an account? Register",
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun LoginPreviewContent() {
    Scaffold(containerColor = Color.Black) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.Security, null, tint = ErrorRed, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("ACCESS IDENTITY", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("Your discipline record is waiting.", color = Color.Gray)
            Spacer(Modifier.height(28.dp))
            OutlinedTextField("", {}, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField("", {}, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                Text("LOGIN", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("CONTINUE WITH GOOGLE", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun LoginScreenPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        LoginPreviewContent()
    }
}
