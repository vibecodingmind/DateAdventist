package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CameraAlt
import coil.compose.AsyncImage
import com.example.data.local.ProfileEntity
import com.example.ui.components.ProfilePhotoCaptureDialog
import com.example.ui.components.SubscriptionBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.GoldTertiary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.SafetyViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    safetyViewModel: SafetyViewModel,
    onNavigateToSubscription: () -> Unit,
    onNavigateToSafety: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val currentProfile by authViewModel.currentProfile.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showCameraDialog by remember { mutableStateOf(false) }

    if (showCameraDialog) {
        ProfilePhotoCaptureDialog(
            onDismissRequest = { showCameraDialog = false },
            onPhotoCaptured = { uri ->
                safetyViewModel.submitSelfieVerification(uri.toString())
                showCameraDialog = false
            }
        )
    }

    if (currentProfile == null) return

    val prof = currentProfile!!

    // Dynamic Profile Completion Calculation
    val completionChecklist = remember(prof) {
        listOf(
            "Primary Avatar Photo" to prof.primaryPhoto.isNotBlank(),
            "Additional Gallery Photos" to (prof.photoUrls.size >= 2),
            "Bio Statement" to (prof.bio.length >= 15),
            "Local Adventist Church" to prof.localChurch.isNotBlank(),
            "Favorite Bible Verse" to prof.favoriteVerse.isNotBlank(),
            "Ministry Interests" to prof.ministryInterests.isNotEmpty(),
            "Personal Interests" to prof.interests.isNotEmpty(),
            "Dietary Preference" to prof.diet.isNotBlank()
        )
    }
    val completedCount = completionChecklist.count { it.second }
    val completionPct = ((completedCount.toFloat() / completionChecklist.size) * 100).toInt()
    val missingSections = completionChecklist.filter { !it.second }.map { it.first }

    if (isEditing) {
        EditProfileView(
            profile = prof,
            onSave = { updated ->
                authViewModel.updateProfileData(updated)
                isEditing = false
            },
            onCancel = { isEditing = false }
        ) {
            safetyViewModel.submitSelfieVerification(it)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090A0E))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("profile_screen")
        ) {
            // Profile Header Avatar with Dynamic Completion Badge
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Avatar Image with glowing ring border
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .border(
                                3.dp,
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF3366), GoldTertiary)
                                ),
                                CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = prof.primaryPhoto,
                            contentDescription = prof.fullName,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Profile Completion Badge at Bottom of Avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(50))
                            .background(if (completionPct == 100) Color(0xFF166534) else Color(0xFF1E3A8A))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$completionPct% COMPLETE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${prof.fullName}, ${prof.age}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (prof.isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        VerificationBadge()
                    }
                    if (prof.isPremium) {
                        Spacer(modifier = Modifier.width(6.dp))
                        SubscriptionBadge(text = "PRO MEMBER")
                    }
                }

                Text(
                    text = "${prof.occupation} • ${prof.city}",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3 Action Control Buttons Row (Matching Tinder Profile Screen Mockup)
                // 1. SETTINGS (Gear) | 2. EDIT PROFILE (Pencil + Primary Pink) | 3. SAFETY (Shield)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF161822))
                                .border(1.dp, Color(0xFF282A3A), CircleShape)
                                .testTag("button_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Settings",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SETTINGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Edit Profile Primary Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFF3366), Color(0xFFE11D48))
                                    )
                                )
                                .clickable { isEditing = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "EDIT PROFILE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Safety Center Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onNavigateToSafety,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF161822))
                                .border(1.dp, Color(0xFF282A3A), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = "Safety Center",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SAFETY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Membership Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161822)
                ),
                onClick = onNavigateToSubscription,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldTertiary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GoldTertiary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = GoldTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (prof.isPremium) "AdventHearts Premium Active ⭐" else "AdventHearts Gold",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (prof.isPremium) "Unlimited Likes, See Who Liked You & Global Passport" else "See Who Liked You & Priority Faith Matches",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Completion Progress Card with Actionable Prompts
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF282A3A), RoundedCornerShape(20.dp))
                    .testTag("profile_completion_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profile Strength & Faith Completion",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$completionPct%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (completionPct >= 80) Color(0xFF10B981) else Color(0xFFFF3366)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { (completionPct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFF3366),
                        trackColor = Color(0xFF282A3A)
                    )

                    if (missingSections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Complete missing sections for higher match compatibility:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            missingSections.forEach { sectionName ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222533)),
                                    modifier = Modifier
                                        .clickable { isEditing = true }
                                        .testTag("prompt_add_$sectionName")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "+ Add $sectionName",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF3366)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🎉 Your profile is 100% complete! You'll receive up to 3x more Sabbath matches.",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verification Card if not verified
            if (!prof.isVerified) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Get Verified Badge Blue Checkmark",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Status: ${prof.verificationStatus}. Submit a photo selfie to confirm your identity.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                showCameraDialog = true
                            },
                            modifier = Modifier.testTag("take_verification_selfie_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Verification Selfie")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bio & Faith details
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Relationship Intention",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = prof.relationshipIntention,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Bio",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = prof.bio, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Adventist Faith Details",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = "• Local Church: ${prof.localChurch}", fontSize = 13.sp)
                    Text(text = "• Faith Importance: ${prof.faithImportance}", fontSize = 13.sp)
                    Text(text = "• Favorite Verse: ${prof.favoriteVerse}", fontSize = 13.sp)
                    Text(text = "• Dietary Preference: ${prof.diet}", fontSize = 13.sp)
                    Text(text = "• Sabbath Activities: ${prof.sabbathObservance.joinToString(", ")}", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Center
            OutlinedButton(
                onClick = onNavigateToSafety,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Shield, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Safety & Privacy Center")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onNavigateToAdmin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Admin & Moderation Panel")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { authViewModel.logout() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EditProfileView(
    profile: ProfileEntity,
    onSave: (ProfileEntity) -> Unit,
    onCancel: () -> Unit,
    onSubmitSelfie: (String) -> Unit
) {
    var fullName by remember { mutableStateOf(profile.fullName) }
    var bio by remember { mutableStateOf(profile.bio) }
    var localChurch by remember { mutableStateOf(profile.localChurch) }
    var favoriteVerse by remember { mutableStateOf(profile.favoriteVerse) }
    var occupation by remember { mutableStateOf(profile.occupation) }
    var education by remember { mutableStateOf(profile.education) }
    var primaryPhoto by remember { mutableStateOf(profile.primaryPhoto) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Edit Adventist Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = localChurch,
            onValueChange = { localChurch = it },
            label = { Text("Local Church") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = favoriteVerse,
            onValueChange = { favoriteVerse = it },
            label = { Text("Favorite Verse") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = occupation,
            onValueChange = { occupation = it },
            label = { Text("Occupation") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        var showProfileCamera by remember { mutableStateOf(false) }

        if (showProfileCamera) {
            ProfilePhotoCaptureDialog(
                onDismissRequest = { showProfileCamera = false },
                onPhotoCaptured = { uri ->
                    primaryPhoto = uri.toString()
                    showProfileCamera = false
                }
            )
        }

        OutlinedTextField(
            value = primaryPhoto,
            onValueChange = { primaryPhoto = it },
            label = { Text("Primary Photo URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedButton(
            onClick = { showProfileCamera = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("take_profile_photo_button")
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Take Photo with Camera")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            fullName = fullName,
                            bio = bio,
                            localChurch = localChurch,
                            favoriteVerse = favoriteVerse,
                            occupation = occupation,
                            primaryPhoto = primaryPhoto
                        )
                    )
                }
            ) {
                Text("Save Profile")
            }
        }
    }
}
