package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.SubscriptionBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.VerifiedBlue
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    onBack: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToSafety: () -> Unit
) {
    val currentProfile by authViewModel.currentProfile.collectAsState()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val context = LocalContext.current

    fun openLegal(path: String) {
        val base = BuildConfig.LEGAL_BASE_URL.trimEnd('/')
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$base$path")))
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Discovery Settings state
    var maxDistance by remember { mutableFloatStateOf(currentProfile?.distanceKm?.toFloat() ?: 50f) }
    var minAge by remember { mutableFloatStateOf(18f) }
    var maxAge by remember { mutableFloatStateOf(45f) }
    var showMeOnApp by remember { mutableStateOf(true) }
    var globalPassport by remember { mutableStateOf(isPremium) }

    // Privacy Settings state
    var showActiveStatus by remember { mutableStateOf(true) }
    var showDistance by remember { mutableStateOf(true) }
    var showAge by remember { mutableStateOf(true) }
    var incognitoMode by remember { mutableStateOf(false) }

    // Notification Settings state
    var pushEnabled by remember { mutableStateOf(true) }
    var matchAlerts by remember { mutableStateOf(true) }
    var messageAlerts by remember { mutableStateOf(true) }
    var superLikeAlerts by remember { mutableStateOf(true) }
    var devotionalDigest by remember { mutableStateOf(true) }
    var soundHaptics by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "App Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090A0E))
            )
        },
        containerColor = Color(0xFF090A0E)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("settings_screen")
        ) {
            // Subscription Upgrade Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSubscription() }
                    .testTag("settings_subscription_banner")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GoldTertiary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = GoldTertiary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPremium) "AdventHearts Premium Active" else "AdventHearts Gold",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            SubscriptionBadge(text = if (isPremium) "PRO" else "GOLD", badgeColor = GoldTertiary)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPremium) "Tap to manage membership & benefits" else "See Who Liked You, Priority Faith Matching & Global Passport",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. ACCOUNT SETTINGS
            SettingsHeader("Account Settings", icon = Icons.Filled.Person)
            SettingsCard {
                SettingsInfoRow("Email Address", "christianradiosorg@gmail.com")
                SettingsDivider()
                SettingsInfoRow("Phone Number", "+1 (555) 019-2834")
                SettingsDivider()
                SettingsActionRow("Password & Security") { }
                SettingsDivider()
                SettingsInfoRow("Language & Region", "English (United States)")
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. DISCOVERY SETTINGS
            SettingsHeader("Discovery Preferences", icon = Icons.Filled.Tune)
            SettingsCard {
                SettingsInfoRow("Current Location", "${currentProfile?.city ?: "Silver Spring"}, ${currentProfile?.country ?: "USA"}")
                SettingsDivider()

                // Maximum Distance Slider
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Maximum Distance", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("${maxDistance.toInt()} km", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3366))
                    }
                    Slider(
                        value = maxDistance,
                        onValueChange = { maxDistance = it },
                        valueRange = 5f..500f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF3366),
                            activeTrackColor = Color(0xFFFF3366),
                            inactiveTrackColor = Color(0xFF282A3A)
                        )
                    )
                }
                SettingsDivider()

                // Age Range
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Age Preference", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("${minAge.toInt()} - ${maxAge.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3366))
                    }
                    RangeSlider(
                        value = minAge..maxAge,
                        onValueChange = {
                            minAge = it.start
                            maxAge = it.endInclusive
                        },
                        valueRange = 18f..75f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF3366),
                            activeTrackColor = Color(0xFFFF3366),
                            inactiveTrackColor = Color(0xFF282A3A)
                        )
                    )
                }
                SettingsDivider()

                SettingsToggleRow(
                    title = "Show me on AdventHearts",
                    subtitle = "When disabled, your profile is hidden from the discover deck",
                    checked = showMeOnApp,
                    onCheckedChange = { showMeOnApp = it }
                )
                SettingsDivider()

                SettingsToggleRow(
                    title = "Global Passport Mode",
                    subtitle = "Discover Adventist singles in any city or country worldwide",
                    checked = globalPassport,
                    onCheckedChange = {
                        if (!isPremium) {
                            onNavigateToSubscription()
                        } else {
                            globalPassport = it
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. PRIVACY SETTINGS
            SettingsHeader("Privacy & Visibility", icon = Icons.Filled.Lock)
            SettingsCard {
                SettingsToggleRow(
                    title = "Show Active Status",
                    subtitle = "Display when you were recently active on AdventHearts",
                    checked = showActiveStatus,
                    onCheckedChange = { showActiveStatus = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Show Distance on Profile",
                    subtitle = "Display approximate distance in kilometers",
                    checked = showDistance,
                    onCheckedChange = { showDistance = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Show Age on Profile",
                    subtitle = "Display your age on your profile card",
                    checked = showAge,
                    onCheckedChange = { showAge = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Incognito Mode",
                    subtitle = "Only members you have liked will see your profile",
                    checked = incognitoMode,
                    onCheckedChange = { incognitoMode = it }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. NOTIFICATION SETTINGS
            SettingsHeader("Notifications", icon = Icons.Filled.Notifications)
            SettingsCard {
                SettingsToggleRow(
                    title = "Push Notifications",
                    subtitle = "Enable or disable all app push notifications",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "New Match Alerts",
                    subtitle = "Notify when you get a mutual Sabbath match",
                    checked = messageAlerts,
                    onCheckedChange = { messageAlerts = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "New Message Notifications",
                    subtitle = "Notify when someone sends you a message",
                    checked = matchAlerts,
                    onCheckedChange = { matchAlerts = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Super Likes & Compliments",
                    subtitle = "Notify when someone super likes your faith profile",
                    checked = superLikeAlerts,
                    onCheckedChange = { superLikeAlerts = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Weekly Adventist Devotional Digest",
                    subtitle = "Receive inspirational Sabbath Sabbath thoughts and dating tips",
                    checked = devotionalDigest,
                    onCheckedChange = { devotionalDigest = it }
                )
                SettingsDivider()
                SettingsToggleRow(
                    title = "Sound & Haptic Feedback",
                    subtitle = "Play sounds and vibrations on matches",
                    checked = soundHaptics,
                    onCheckedChange = { soundHaptics = it }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. SAFETY & SECURITY
            SettingsHeader("Safety Center", icon = Icons.Filled.Shield)
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSafety() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Selfie Verification Status", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            if (currentProfile?.isVerified == true) {
                                VerificationBadge()
                            }
                        }
                        Text(
                            text = if (currentProfile?.isVerified == true) "Identity Verified" else "Status: ${currentProfile?.verificationStatus ?: "NOT_VERIFIED"}. Tap to submit selfie.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                }
                SettingsDivider()
                SettingsActionRow("Safety Center & Guidelines") { onNavigateToSafety() }
                SettingsDivider()
                SettingsActionRow("Blocked Contacts Manager") { }
                SettingsDivider()
                SettingsActionRow("Community Standards & Adventist Dating Ethics") { }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. LEGAL & ABOUT
            SettingsHeader("Legal & Support", icon = Icons.Filled.Security)
            SettingsCard {
                SettingsActionRow("Terms of Service") { openLegal("/legal/terms") }
                SettingsDivider()
                SettingsActionRow("Privacy Policy") { openLegal("/legal/privacy") }
                SettingsDivider()
                SettingsActionRow("Restore Purchases") { }
                SettingsDivider()
                SettingsInfoRow("App Version", "v2.4.0-build (AdventHearts Android)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out & Account Deletion
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_logout"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E))
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.testTag("button_delete_account_trigger")
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete or Deactivate Account", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to permanently delete your AdventHearts profile and all match data? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            authViewModel.deleteAccount()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Account Permanently")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFF3366),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF3366)
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF282A3A))
    )
}

@Composable
private fun SettingsInfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(value, fontSize = 13.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun SettingsActionRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF3366)
            )
        )
    }
}
