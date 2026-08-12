package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VerifiedBlue
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    authViewModel: AuthViewModel,
    onCompleteOnboarding: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 8

    // Form states
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("24") }
    var gender by remember { mutableStateOf("Female") }
    var country by remember { mutableStateOf("United States") }
    var city by remember { mutableStateOf("Berrien Springs") }
    var intention by remember { mutableStateOf("Marriage") }

    // Faith state
    var affiliation by remember { mutableStateOf("Seventh-day Adventist Member") }
    var localChurch by remember { mutableStateOf("Pioneer Memorial Church") }
    var faithImportance by remember { mutableStateOf("Central to everything I do") }
    var churchInvolvement by remember { mutableStateOf("Very active") }
    var favoriteVerse by remember { mutableStateOf("Jeremiah 29:11") }

    // Lifestyle state
    var diet by remember { mutableStateOf("Vegetarian") }
    var wantsChildren by remember { mutableStateOf("Yes, definitely") }
    var photoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80") }

    Scaffold(containerColor = Color(0xFF090A0E)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF090A0E))
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("onboarding_screen")
        ) {
            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step $step of $totalSteps",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF3366)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${(step * 100 / totalSteps)}% Complete",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { step.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFFF3366),
                trackColor = Color(0xFF161822)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (step) {
                1 -> {
                    Text(
                        text = "Welcome to AdventHearts",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Let's build your faith-centered dating profile to connect you with Seventh-day Adventist singles.",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name", color = Color(0xFF94A3B8)) },
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
                            .testTag("onboard_name")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address", color = Color(0xFF94A3B8)) },
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
                            .testTag("onboard_email")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color(0xFF94A3B8)) },
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
                            .testTag("onboard_password")
                    )
                }

                2 -> {
                    Text(text = "Basic Information", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age (Must be 18+)", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerifiedBlue,
                            unfocusedBorderColor = Color(0xFF282A3A),
                            focusedContainerColor = Color(0xFF161822),
                            unfocusedContainerColor = Color(0xFF161822),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Gender",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        listOf("Female", "Male").forEach { option ->
                            FilterChip(
                                selected = gender == option,
                                onClick = { gender = option },
                                label = { Text(option, color = if (gender == option) Color.White else Color(0xFF94A3B8)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF3366),
                                    containerColor = Color(0xFF161822)
                                ),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerifiedBlue,
                            unfocusedBorderColor = Color(0xFF282A3A),
                            focusedContainerColor = Color(0xFF161822),
                            unfocusedContainerColor = Color(0xFF161822),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City / Region", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerifiedBlue,
                            unfocusedBorderColor = Color(0xFF282A3A),
                            focusedContainerColor = Color(0xFF161822),
                            unfocusedContainerColor = Color(0xFF161822),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                3 -> {
                    Text(text = "Relationship Intention", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = "What kind of relationship are you seeking on AdventHearts?",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    listOf(
                        "Marriage" to "Dating intentionally with Christian marriage as the goal.",
                        "Serious Relationship" to "Building a strong relationship rooted in shared faith.",
                        "Getting to know someone" to "Exploring faith compatibility at a natural pace.",
                        "Friendship first" to "Establishing spiritual friendship before dating."
                    ).forEach { (title, desc) ->
                        Card(
                            onClick = { intention = title },
                            colors = CardDefaults.cardColors(
                                containerColor = if (intention == title) Color(0xFFFF3366).copy(alpha = 0.2f) else Color(0xFF161822)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, if (intention == title) Color(0xFFFF3366) else Color(0xFF282A3A), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                4 -> {
                    Text(text = "Your Adventist Faith", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Adventist Affiliation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Seventh-day Adventist Member", "Adventist Convert", "Adventist-Friendly").forEach { opt ->
                            FilterChip(
                                selected = affiliation == opt,
                                onClick = { affiliation = opt },
                                label = { Text(opt, color = if (affiliation == opt) Color.White else Color(0xFF94A3B8)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF3366),
                                    containerColor = Color(0xFF161822)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = localChurch,
                        onValueChange = { localChurch = it },
                        label = { Text("Local SDA Church / Congregation", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerifiedBlue,
                            unfocusedBorderColor = Color(0xFF282A3A),
                            focusedContainerColor = Color(0xFF161822),
                            unfocusedContainerColor = Color(0xFF161822),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = favoriteVerse,
                        onValueChange = { favoriteVerse = it },
                        label = { Text("Favorite Bible Verse", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerifiedBlue,
                            unfocusedBorderColor = Color(0xFF282A3A),
                            focusedContainerColor = Color(0xFF161822),
                            unfocusedContainerColor = Color(0xFF161822),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                5 -> {
                    Text(text = "Church Involvement & Faith Role", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "How central is your Adventist faith in your daily life?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    listOf("Central to everything I do", "Very important", "Important", "Still growing").forEach { opt ->
                        FilterChip(
                            selected = faithImportance == opt,
                            onClick = { faithImportance = opt },
                            label = { Text(opt, color = if (faithImportance == opt) Color.White else Color(0xFF94A3B8)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF3366),
                                containerColor = Color(0xFF161822)
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Local Church Involvement",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    listOf("Very active", "Active", "Occasionally involved", "Currently looking for church").forEach { opt ->
                        FilterChip(
                            selected = churchInvolvement == opt,
                            onClick = { churchInvolvement = opt },
                            label = { Text(opt, color = if (churchInvolvement == opt) Color.White else Color(0xFF94A3B8)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF3366),
                                containerColor = Color(0xFF161822)
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                6 -> {
                    Text(text = "Lifestyle & Health Principles", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Dietary Preference",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Vegetarian", "Vegan", "Plant-Based", "Standard").forEach { opt ->
                            FilterChip(
                                selected = diet == opt,
                                onClick = { diet = opt },
                                label = { Text(opt, color = if (diet == opt) Color.White else Color(0xFF94A3B8)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF3366),
                                    containerColor = Color(0xFF161822)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Do you want children?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    listOf("Yes, definitely", "Open to children", "Not sure", "No").forEach { opt ->
                        FilterChip(
                            selected = wantsChildren == opt,
                            onClick = { wantsChildren = opt },
                            label = { Text(opt, color = if (wantsChildren == opt) Color.White else Color(0xFF94A3B8)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF3366),
                                containerColor = Color(0xFF161822)
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }

                7 -> {
                    Text(text = "Add Profile Photo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = "Enter a photo URL or use default portrait photo for your profile.",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = photoUrl,
                        onValueChange = { photoUrl = it },
                        label = { Text("Photo URL", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerifiedBlue,
                            unfocusedBorderColor = Color(0xFF282A3A),
                            focusedContainerColor = Color(0xFF161822),
                            unfocusedContainerColor = Color(0xFF161822),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                8 -> {
                    Text(text = "Ready to Discover Matches!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = "Your profile is set up with faith-first preferences. Start connecting with Adventist members worldwide.",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.border(1.dp, Color(0xFF282A3A), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✨ Community Promise:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "AdventHearts promotes respectful Christian interaction. Be intentional, honor Sabbath values, and communicate with purpose.",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    TextButton(onClick = { step-- }) {
                        Text("Back", color = Color(0xFF94A3B8))
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (step < totalSteps) {
                            step++
                        } else {
                            val parsedAge = ageText.toIntOrNull() ?: 24
                            authViewModel.register(
                                fullName = fullName.ifBlank { "Adventist Single" },
                                email = email.ifBlank { "user@adventhearts.com" },
                                pass = password.ifBlank { "pass123" },
                                age = parsedAge,
                                gender = gender,
                                country = country,
                                city = city,
                                intention = intention
                            )
                            onCompleteOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366)),
                    modifier = Modifier.testTag("button_onboard_next")
                ) {
                    Text(if (step == totalSteps) "Complete Profile" else "Next", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
