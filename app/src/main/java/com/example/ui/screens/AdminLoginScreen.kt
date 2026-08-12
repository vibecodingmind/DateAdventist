package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SubscriptionBadge
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.VerifiedBlue
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun AdminLoginScreen(
    adminViewModel: AdminViewModel,
    onLoginSuccess: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val adminMessage by adminViewModel.adminMessage.collectAsState()

    var email by remember { mutableStateOf("admin@adventhearts.com") }
    var password by remember { mutableStateOf("AdminPass2026!") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0E))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .testTag("admin_login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AdminPanelSettings,
            contentDescription = "Admin Portal",
            tint = GoldTertiary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "AdventHearts Admin Portal",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Role-Based Access Control (RBAC) System",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1065)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color(0xFF9333EA), RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFF3E8FF),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Admin Email", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(Icons.Filled.Mail, contentDescription = null, tint = VerifiedBlue) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerifiedBlue,
                unfocusedBorderColor = Color(0xFF282A3A),
                focusedContainerColor = Color(0xFF161822),
                unfocusedContainerColor = Color(0xFF161822),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_admin_email")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = VerifiedBlue) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerifiedBlue,
                unfocusedBorderColor = Color(0xFF282A3A),
                focusedContainerColor = Color(0xFF161822),
                unfocusedContainerColor = Color(0xFF161822),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_admin_password")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and password are required."
                    return@Button
                }
                isLoading = true
                val role = when {
                    email.contains("super") -> "SUPER_ADMIN"
                    email.contains("mod") -> "MODERATOR"
                    else -> "ADMIN"
                }
                adminViewModel.loginAdmin(email, role)
                isLoading = false
                onLoginSuccess(role)
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = GoldTertiary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("button_admin_login")
        ) {
            Text("Login to Admin Dashboard", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quick Dev Roles (RBAC Test)",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    email = "superadmin@adventhearts.com"
                    adminViewModel.loginAdmin(email, "SUPER_ADMIN")
                    onLoginSuccess("SUPER_ADMIN")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("SuperAdmin", fontSize = 11.sp, color = GoldTertiary)
            }

            OutlinedButton(
                onClick = {
                    email = "admin@adventhearts.com"
                    adminViewModel.loginAdmin(email, "ADMIN")
                    onLoginSuccess("ADMIN")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Admin", fontSize = 11.sp, color = VerifiedBlue)
            }

            OutlinedButton(
                onClick = {
                    email = "moderator@adventhearts.com"
                    adminViewModel.loginAdmin(email, "MODERATOR")
                    onLoginSuccess("MODERATOR")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Moderator", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}
