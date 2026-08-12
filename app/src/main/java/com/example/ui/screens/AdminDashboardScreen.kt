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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.AuditLogDto
import com.example.ui.components.SubscriptionBadge
import com.example.ui.theme.GoldTertiary
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.PaymentSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel
) {
    val analytics by adminViewModel.analytics.collectAsState()
    val pendingVerifications by adminViewModel.pendingVerifications.collectAsState()
    val reports by adminViewModel.allReports.collectAsState()
    val adminMsg by adminViewModel.adminMessage.collectAsState()
    val settings by adminViewModel.paymentSettings.collectAsState()
    val adminRole by adminViewModel.adminRole.collectAsState()
    val userStatuses by adminViewModel.userAccountStatuses.collectAsState()
    val filteredAuditLogs by adminViewModel.filteredAuditLogs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Analytics",
        "User Management",
        "Subscriptions",
        "Audit Logs",
        "Verifications (${pendingVerifications.size})",
        "Reports (${reports.size})",
        "Settings & Gateways"
    )

    // Role Authorization Check
    if (adminRole == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Access Denied", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You must log in with an authorized administrator session to view this dashboard.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { adminViewModel.loginAdmin("admin@adventhearts.com", "ADMIN") }) {
                        Text("Log In as Admin")
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = GoldTertiary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AdventHearts Admin Panel",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            SubscriptionBadge(text = adminRole ?: "ADMIN", badgeColor = GoldTertiary)
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, fontSize = 12.sp, maxLines = 1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (adminMsg != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = adminMsg!!, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { adminViewModel.dismissMessage() },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("OK", fontSize = 10.sp)
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Analytics Dashboard
                AnalyticsDashboard(analytics = analytics)
            }

            1 -> {
                // User Management Table
                UserManagementTable(
                    adminViewModel = adminViewModel,
                    userStatuses = userStatuses
                )
            }

            2 -> {
                // Subscriptions Management Table
                SubscriptionManagementTable(adminViewModel = adminViewModel)
            }

            3 -> {
                // Audit Logs Viewer
                AuditLogViewer(
                    adminViewModel = adminViewModel,
                    auditLogs = filteredAuditLogs
                )
            }

            4 -> {
                // Verifications Queue
                if (pendingVerifications.isEmpty()) {
                    Text(
                        text = "No pending selfie verifications in moderation queue.",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                        items(pendingVerifications) { prof ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = prof.primaryPhoto,
                                        contentDescription = prof.fullName,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = prof.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "Church: ${prof.localChurch}", fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = { adminViewModel.approveVerification(prof.userId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OutlinedButton(
                                        onClick = { adminViewModel.rejectVerification(prof.userId) },
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Reject", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // Reports
                if (reports.isEmpty()) {
                    Text(
                        text = "No safety reports currently flagged.",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                        items(reports) { rep ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Report #${rep.reportId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Reason: ${rep.reason}", fontSize = 12.sp)
                                    Text(text = "Details: ${rep.details}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row {
                                        Button(
                                            onClick = { adminViewModel.updateReportStatus(rep.reportId, "RESOLVED") },
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("Resolve", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            6 -> {
                // Settings & Payment Gateways
                AdminSettingsTab(
                    settings = settings,
                    onSave = { updated -> adminViewModel.updateSettings(updated) }
                )
            }
        }
    }
}

@Composable
private fun AnalyticsDashboard(analytics: com.example.ui.viewmodel.AdminAnalytics) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Key Platform Performance Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard("Daily Active Users", "${analytics.dailyActiveUsers}", modifier = Modifier.weight(1f))
            MetricCard("Monthly Active Users", "${analytics.monthlyActiveUsers}", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard("Active Subscriptions", "${analytics.activeSubscriptions}", modifier = Modifier.weight(1f))
            MetricCard("Matches Made", "${analytics.matchesMade}", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Subscription Conversion Rate", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${analytics.subscriptionConversionRatePct}%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text("Target: 15.0%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (analytics.subscriptionConversionRatePct / 20.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        MetricCard("Total Revenue (USD)", "$${analytics.totalRevenueUsd}", modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        // Visual chart breakdown card
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Monthly Activity & Growth Trend", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val sampleMonths = listOf("Jan" to 0.5f, "Feb" to 0.65f, "Mar" to 0.8f, "Apr" to 0.75f, "May" to 0.95f, "Jun" to 1.0f)
                    sampleMonths.forEach { (month, ratio) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height((80 * ratio).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(month, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserManagementTable(
    adminViewModel: AdminViewModel,
    userStatuses: Map<String, String>
) {
    val allProfiles by adminViewModel.allProfiles.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredProfiles = allProfiles.filter { prof ->
        searchQuery.isBlank() ||
        prof.fullName.contains(searchQuery, ignoreCase = true) ||
        prof.city.contains(searchQuery, ignoreCase = true) ||
        prof.country.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search users by name, location...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        if (filteredProfiles.isEmpty()) {
            Text(
                text = "No users found matching query.",
                modifier = Modifier.padding(20.dp),
                fontSize = 14.sp
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredProfiles) { userProf ->
                    val status = userStatuses[userProf.userId] ?: "ACTIVE"

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = userProf.primaryPhoto,
                                    contentDescription = userProf.fullName,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = userProf.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "${userProf.city}, ${userProf.country} • Age ${userProf.age}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "Tier: ${if (userProf.isPremium) "GOLD" else "FREE"}", fontSize = 10.sp)
                                }

                                StatusBadge(status = status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (status != "SUSPENDED") {
                                    OutlinedButton(
                                        onClick = { adminViewModel.updateUserAccountStatus(userProf.userId, "SUSPENDED", "Admin temporary suspension") },
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Suspend", fontSize = 10.sp)
                                    }
                                }
                                if (status != "BANNED") {
                                    Button(
                                        onClick = { adminViewModel.updateUserAccountStatus(userProf.userId, "BANNED", "Violation of community standards") },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Ban User", fontSize = 10.sp)
                                    }
                                }
                                if (status != "ACTIVE") {
                                    Button(
                                        onClick = { adminViewModel.updateUserAccountStatus(userProf.userId, "ACTIVE", "Account reinstated by admin") },
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Reinstate", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "BANNED" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "SUSPENDED" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        else -> Color(0xFFDCFCE7) to Color(0xFF166534)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuditLogViewer(
    adminViewModel: AdminViewModel,
    auditLogs: List<AuditLogDto>
) {
    val currentActionFilter by adminViewModel.auditActionFilter.collectAsState()
    var searchActorQuery by remember { mutableStateOf("") }

    val filterOptions = listOf("ALL", "VERIFY_APPROVE", "USER_SUSPEND", "USER_BAN", "SETTINGS_UPDATE")

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchActorQuery,
            onValueChange = {
                searchActorQuery = it
                adminViewModel.setAuditActorFilter(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Filter by actor or details...") },
            leadingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null) },
            singleLine = true
        )

        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filterOptions.forEach { opt ->
                FilterChip(
                    selected = currentActionFilter == opt,
                    onClick = { adminViewModel.setAuditActionFilter(opt) },
                    label = { Text(opt, fontSize = 10.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (auditLogs.isEmpty()) {
            Text(
                text = "No audit log records match the current filter criteria.",
                modifier = Modifier.padding(20.dp),
                fontSize = 14.sp
            )
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(auditLogs) { log ->
                    val dateFormatted = remember(log.timestamp) {
                        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(log.actionType, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Text(dateFormatted, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Actor: ${log.actor}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(log.details, fontSize = 11.sp)
                            if (log.targetUserId != null) {
                                Text("Target User: ${log.targetUserId}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdminSettingsTab(
    settings: PaymentSettings,
    onSave: (PaymentSettings) -> Unit
) {
    var stripeKey by remember(settings) { mutableStateOf(settings.stripePublishableKey) }
    var stripeSecret by remember(settings) { mutableStateOf(settings.stripeSecretKey) }
    var paypalId by remember(settings) { mutableStateOf(settings.paypalClientId) }
    var monthlyPrice by remember(settings) { mutableStateOf(settings.monthlyPriceUsd) }
    var annualPrice by remember(settings) { mutableStateOf(settings.annualPriceUsd) }
    var lifetimePrice by remember(settings) { mutableStateOf(settings.lifetimePriceUsd) }
    var isSandbox by remember(settings) { mutableStateOf(settings.isSandboxMode) }
    var requireSelfie by remember(settings) { mutableStateOf(settings.requireSelfieVerification) }
    var maintenance by remember(settings) { mutableStateOf(settings.maintenanceMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Payment Gateway Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = stripeKey,
            onValueChange = { stripeKey = it },
            label = { Text("Stripe Publishable Key") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = stripeSecret,
            onValueChange = { stripeSecret = it },
            label = { Text("Stripe Secret Key") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = paypalId,
            onValueChange = { paypalId = it },
            label = { Text("PayPal Client ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Subscription Pricing Tiers (USD)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = monthlyPrice,
                onValueChange = { monthlyPrice = it },
                label = { Text("Monthly ($)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = annualPrice,
                onValueChange = { annualPrice = it },
                label = { Text("Annual ($)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = lifetimePrice,
                onValueChange = { lifetimePrice = it },
                label = { Text("Lifetime ($)") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("System Rules & Flags", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sandbox / Test Mode")
            Switch(checked = isSandbox, onCheckedChange = { isSandbox = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Require Selfie Verification for Badges")
            Switch(checked = requireSelfie, onCheckedChange = { requireSelfie = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Maintenance Mode")
            Switch(checked = maintenance, onCheckedChange = { maintenance = it })
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSave(
                    settings.copy(
                        stripePublishableKey = stripeKey,
                        stripeSecretKey = stripeSecret,
                        paypalClientId = paypalId,
                        monthlyPriceUsd = monthlyPrice,
                        annualPriceUsd = annualPrice,
                        lifetimePriceUsd = lifetimePrice,
                        isSandboxMode = isSandbox,
                        requireSelfieVerification = requireSelfie,
                        maintenanceMode = maintenance
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Payment & System Settings")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubscriptionManagementTable(
    adminViewModel: AdminViewModel
) {
    val allProfiles by adminViewModel.allProfiles.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var tierFilter by remember { mutableStateOf("ALL") } // ALL, PREMIUM, FREE

    val filteredList = remember(allProfiles, searchQuery, tierFilter) {
        allProfiles.filter { p ->
            val matchesSearch = searchQuery.isBlank() ||
                    p.fullName.contains(searchQuery, ignoreCase = true) ||
                    p.userId.contains(searchQuery, ignoreCase = true) ||
                    p.localChurch.contains(searchQuery, ignoreCase = true)

            val matchesTier = when (tierFilter) {
                "PREMIUM" -> p.isPremium
                "FREE" -> !p.isPremium
                else -> true
            }

            matchesSearch && matchesTier
        }
    }

    val totalPremiumCount = remember(allProfiles) { allProfiles.count { it.isPremium } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_subscription_table")
    ) {
        // Summary Banner Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard("Active Subscribers", "$totalPremiumCount", modifier = Modifier.weight(1f))
            MetricCard("Est. Monthly Revenue", "$${totalPremiumCount * 14.99}", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search subscriber by name, church, or ID...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tier Filters
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL", "PREMIUM", "FREE").forEach { tier ->
                FilterChip(
                    selected = tierFilter == tier,
                    onClick = { tierFilter = tier },
                    label = { Text(tier) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No user accounts matched subscription filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                items(filteredList) { prof ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = prof.primaryPhoto,
                                    contentDescription = prof.fullName,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(prof.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (prof.isPremium) {
                                            SubscriptionBadge(text = "PRO", badgeColor = GoldTertiary)
                                        } else {
                                            SubscriptionBadge(text = "FREE", badgeColor = Color(0xFF64748B))
                                        }
                                    }
                                    Text("ID: ${prof.userId} • ${prof.city}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (prof.isPremium) {
                                    OutlinedButton(
                                        onClick = { adminViewModel.updateUserSubscriptionTier(prof.userId, false) },
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Downgrade to Free", fontSize = 11.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = { adminViewModel.updateUserSubscriptionTier(prof.userId, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldTertiary),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Grant Pro Tier", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
