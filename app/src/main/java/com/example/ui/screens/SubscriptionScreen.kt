package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SubscriptionBadge
import com.example.ui.theme.BoostPurple
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.VerifiedBlue
import com.example.ui.viewmodel.SubscriptionViewModel

@Composable
fun SubscriptionScreen(
    subscriptionViewModel: SubscriptionViewModel,
    onBack: () -> Unit
) {
    val selectedPlan by subscriptionViewModel.selectedPlan.collectAsState()
    val isPremium by subscriptionViewModel.isPremium.collectAsState()
    val userTier by subscriptionViewModel.userTier.collectAsState()
    val subMessage by subscriptionViewModel.subSuccessMessage.collectAsState()
    val checkoutUrl by subscriptionViewModel.checkoutUrl.collectAsState()
    val paymentProvider by subscriptionViewModel.paymentProvider.collectAsState()

    var phoneNumber by remember { mutableStateOf("+1 (555) 019-2834") }
    var isIncognito by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0E))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("subscription_screen")
    ) {
        // Header with Active Subscription Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings & Subscription",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (isPremium) {
                SubscriptionBadge(text = "$userTier ACTIVE", badgeColor = GoldTertiary)
            } else {
                SubscriptionBadge(text = "FREE MEMBER", badgeColor = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, GoldTertiary, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = subMessage!!, fontSize = 13.sp, color = GoldTertiary, fontWeight = FontWeight.Bold)
                    if (checkoutUrl != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Checkout URL: $checkoutUrl", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Subscription Tier Cards
        Text(
            text = "Select Subscription Tier",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SubscriptionTierCard(
            badgeText = "PLATINUM",
            title = "AdventHearts Platinum",
            subtitle = "Priority Messaging, Message Before Match & All Gold Features",
            badgeColor = Color(0xFFE2E8F0),
            isSelected = selectedPlan == "PLATINUM",
            onClick = { subscriptionViewModel.selectPlan("PLATINUM") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SubscriptionTierCard(
            badgeText = "GOLD",
            title = "AdventHearts Gold",
            subtitle = "See Who Likes You, 5 Super Likes & Advanced Faith Filters",
            badgeColor = GoldTertiary,
            isSelected = selectedPlan == "GOLD",
            onClick = { subscriptionViewModel.selectPlan("GOLD") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SubscriptionTierCard(
            badgeText = "PLUS",
            title = "AdventHearts Plus",
            subtitle = "Unlimited Likes & Swipes, Hide Ads",
            badgeColor = Color(0xFFFF3366),
            isSelected = selectedPlan == "PLUS",
            onClick = { subscriptionViewModel.selectPlan("PLUS") }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Payment Method Selection (Stripe vs PayPal)
        Text(
            text = "Payment Method Checkout",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stripe Option
            PaymentMethodCard(
                name = "Stripe / Card",
                iconText = "💳 Stripe",
                isSelected = paymentProvider == "stripe",
                onClick = { subscriptionViewModel.selectPaymentProvider("stripe") },
                modifier = Modifier.weight(1f)
            )

            // PayPal Option
            PaymentMethodCard(
                name = "PayPal",
                iconText = "🅿️ PayPal",
                isSelected = paymentProvider == "paypal",
                onClick = { subscriptionViewModel.selectPaymentProvider("paypal") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checkout Button Triggered by Backend
        Button(
            onClick = { subscriptionViewModel.initiateBackendCheckout() },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF3366), Color(0xFFE11D48))
                    )
                )
                .testTag("button_activate_subscription")
        ) {
            Text(
                text = "Checkout with ${paymentProvider.uppercase()} — $selectedPlan",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Comparison Matrix Table
        Text(
            text = "Feature Comparison Matrix",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF282A3A), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ComparisonRow(feature = "Unlimited Swipes", free = false, plus = true, gold = true, platinum = true)
                HorizontalDivider(color = Color(0xFF282A3A))
                ComparisonRow(feature = "See Who Likes You", free = false, plus = false, gold = true, platinum = true)
                HorizontalDivider(color = Color(0xFF282A3A))
                ComparisonRow(feature = "Advanced Faith Filters", free = false, plus = false, gold = true, platinum = true)
                HorizontalDivider(color = Color(0xFF282A3A))
                ComparisonRow(feature = "Super Likes (5/week)", free = false, plus = false, gold = true, platinum = true)
                HorizontalDivider(color = Color(0xFF282A3A))
                ComparisonRow(feature = "Priority Inbox Messages", free = false, plus = false, gold = false, platinum = true)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Packs Grid Cards: [ Get Super Likes | Get Boosts ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .border(1.dp, Color(0xFF282A3A), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VerifiedBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = VerifiedBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Get Super Likes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .border(1.dp, Color(0xFF282A3A), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BoostPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = BoostPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Get Boosts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Go Incognito" Banner Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF282A3A), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222533)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Go Incognito",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Only show profile to people you like",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Switch(
                    checked = isIncognito,
                    onCheckedChange = { isIncognito = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF3366)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Account Settings Section
        Text(
            text = "Account Settings",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
            shape = RoundedCornerShape(16.dp),
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SubscriptionTierCard(
    badgeText: String,
    title: String,
    subtitle: String,
    badgeColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isSelected) badgeColor else Color(0xFF282A3A),
                RoundedCornerShape(20.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubscriptionBadge(text = badgeText, badgeColor = badgeColor)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PaymentMethodCard(
    name: String,
    iconText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
        onClick = onClick,
        modifier = modifier
            .border(
                1.5.dp,
                if (isSelected) VerifiedBlue else Color(0xFF282A3A),
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = iconText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) VerifiedBlue else Color.White
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    feature: String,
    free: Boolean,
    plus: Boolean,
    gold: Boolean,
    platinum: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.weight(1.5f)
        )

        StatusIcon(enabled = gold, modifier = Modifier.weight(0.5f))
        StatusIcon(enabled = platinum, modifier = Modifier.weight(0.5f))
    }
}

@Composable
private fun StatusIcon(enabled: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (enabled) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Included",
                tint = GoldTertiary,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Not Included",
                tint = Color(0xFF475569),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
