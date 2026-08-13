package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.AdventRedPrimary
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.NavySecondary
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun WelcomeScreen(
    authViewModel: AuthViewModel,
    onStartRegistration: () -> Unit,
    onDemoLogin: (String) -> Unit
) {
    val authError by authViewModel.authError.collectAsState()

    var showLoginForm by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavySecondary, Color(0xFF0F172A))
                )
            )
            .testTag("welcome_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // AdventHearts Branding Header
            Surface(
                shape = CircleShape,
                color = AdventRedPrimary.copy(alpha = 0.2f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "AdventHearts Heart",
                        tint = AdventRedPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AdventHearts",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Faith. Connection. Purpose.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = GoldTertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Faith-first Seventh-day Adventist dating platform designed for intentional, marriage-oriented relationships.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Value Props Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ValueCard(
                    icon = Icons.Filled.Church,
                    title = "Faith First",
                    desc = "Adventist beliefs & Sabbath lifestyle",
                    modifier = Modifier.weight(1f)
                )
                ValueCard(
                    icon = Icons.Filled.Shield,
                    title = "Verified & Safe",
                    desc = "Identity & photo moderation",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (authError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = authError!!,
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (showLoginForm) {
                // Login Form
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Sign In to AdventHearts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_login_email")
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_login_password")
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { authViewModel.login(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("button_submit_login")
                        ) {
                            Text("Sign In")
                        }

                        TextButton(
                            onClick = { showForgotPassword = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("button_forgot_password")
                        ) {
                            Text("Forgot password?")
                        }

                        TextButton(
                            onClick = { showLoginForm = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back")
                        }
                    }
                }
            } else {
                // Primary Welcome Actions
                Button(
                    onClick = onStartRegistration,
                    colors = ButtonDefaults.buttonColors(containerColor = AdventRedPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("button_create_account")
                ) {
                    Text(
                        text = "Create AdventHearts Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showLoginForm = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("button_login_view")
                ) {
                    Text("I Already Have an Account")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "By creating an account you agree to the AdventHearts Terms of Service and Privacy Policy.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (BuildConfig.DEBUG) {
                    Text(
                        text = "Quick Demo Switcher:",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onDemoLogin("usr_me") },
                            modifier = Modifier.testTag("demo_user_joshua")
                        ) {
                            Text("Member (Joshua)", fontSize = 11.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { onDemoLogin("usr_admin") },
                            modifier = Modifier.testTag("demo_user_admin")
                        ) {
                            Text("Admin Panel", fontSize = 11.sp, color = GoldTertiary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showForgotPassword) {
            AlertDialog(
                onDismissRequest = { showForgotPassword = false },
                title = { Text("Reset password") },
                text = {
                    Column {
                        Text(
                            "Enter your email to receive a reset token, then paste the token and choose a new password.",
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { authViewModel.forgotPassword(email) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Send reset email")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetToken,
                            onValueChange = { resetToken = it },
                            label = { Text("Reset token") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.resetPassword(resetToken, newPassword)
                            showForgotPassword = false
                        }
                    ) {
                        Text("Update password")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPassword = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
private fun ValueCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldTertiary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
