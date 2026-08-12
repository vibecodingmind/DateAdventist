package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import com.example.data.local.ProfileEntity
import com.example.ui.theme.BoostPurple
import com.example.ui.theme.CompatibilityGreen
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.LikeRose
import com.example.ui.theme.PassRed
import com.example.ui.theme.VerifiedBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeCard(
    profile: ProfileEntity,
    compatibilityScore: Int,
    onLike: () -> Unit,
    onPass: () -> Unit,
    onSuperLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDetailExpanded by remember { mutableStateOf(false) }
    var currentPhotoIndex by remember { mutableStateOf(0) }

    val photos = profile.photoUrls.ifEmpty { listOf(profile.primaryPhoto) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, Color(0xFF282A3A), RoundedCornerShape(28.dp))
            .testTag("swipe_card_${profile.userId}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Photo & Overlay Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
            ) {
                AsyncImage(
                    model = photos.getOrElse(currentPhotoIndex) { profile.primaryPhoto },
                    contentDescription = "Profile photo of ${profile.fullName}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Compatibility Badge Overlay Top Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CompatibilityGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$compatibilityScore% MATCH",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                // Photo Indicators bar if multiple photos
                if (photos.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, start = 16.dp, end = 90.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        photos.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (index == currentPhotoIndex) Color.White else Color.White.copy(alpha = 0.35f)
                                    )
                                    .clickable { currentPhotoIndex = index }
                            )
                        }
                    }
                }

                // Photo Tap left/right areas
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable {
                                if (currentPhotoIndex > 0) currentPhotoIndex--
                            }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable {
                                if (currentPhotoIndex < photos.size - 1) currentPhotoIndex++
                            }
                    )
                }

                // Dark Bottom Gradient Overlay for maximum contrast readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF161822).copy(alpha = 0.95f), Color(0xFF161822))
                            )
                        )
                )

                // Overlay Info (Name, Age with Green Status Dot, Interest Tags, Bio Preview)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Name, Age & Green Active Online Dot (Matching "Kate 30 •")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${profile.fullName}, ${profile.age}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Online Status Indicator Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(CompatibilityGreen)
                        )

                        if (profile.isVerified) {
                            VerificationBadge()
                        }
                        if (profile.isPremium) {
                            SubscriptionBadge(text = "PRO")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${profile.city} • ${profile.distanceKm} miles away",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interest & Faith Pill Chips directly on the bottom overlay
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        PillTag(text = profile.adventistAffiliation)
                        PillTag(text = profile.relationshipIntention)
                        if (profile.interests.isNotEmpty()) {
                            PillTag(text = profile.interests.first())
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = profile.bio.take(75) + if (profile.bio.length > 75) "..." else "",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Circular Expand Toggle Arrow Button
                        IconButton(
                            onClick = { isDetailExpanded = !isDetailExpanded },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isDetailExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Toggle details",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Expanded Profile Details Section
            AnimatedVisibility(visible = isDetailExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161822))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "About ${profile.fullName.split(" ").first()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile.bio,
                        fontSize = 14.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Faith & Church Life",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerifiedBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TagChip(icon = Icons.Filled.Church, text = "Church: ${profile.localChurch}")
                    Spacer(modifier = Modifier.height(4.dp))
                    TagChip(icon = Icons.Filled.MenuBook, text = "Bible Study: ${profile.personalBibleStudy}")
                    Spacer(modifier = Modifier.height(4.dp))
                    TagChip(icon = Icons.Filled.Restaurant, text = "Diet: ${profile.diet}")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Lifestyle & Intentions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldTertiary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Occupation: ${profile.occupation}",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "• Education: ${profile.education}",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "• Family Goals: ${profile.wantsChildren}",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5 Floating Action Control Buttons (Matching Tinder Mobile App Mockup)
            // 1. Rewind (Yellow) | 2. Dislike (Red X) | 3. Like (Vibrant Pink Heart) | 4. Super Like (Blue Star) | 5. Boost (Purple Lightning)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Rewind Button (Gold/Yellow)
                Surface(
                    onClick = { /* Rewind action */ },
                    shape = CircleShape,
                    color = Color(0xFF222533),
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, GoldTertiary.copy(alpha = 0.5f), CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Rewind",
                            tint = GoldTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 2. Dislike / Pass Button (Red X)
                Surface(
                    onClick = onPass,
                    shape = CircleShape,
                    color = Color(0xFF222533),
                    modifier = Modifier
                        .size(56.dp)
                        .border(1.dp, PassRed.copy(alpha = 0.6f), CircleShape)
                        .testTag("button_pass_${profile.userId}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dislike",
                            tint = PassRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // 3. Like Button (Enlarged Vibrant Pink/Rose Heart - Primary Action)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF3366), Color(0xFFE11D48))
                            )
                        )
                        .clickable { onLike() }
                        .testTag("button_like_${profile.userId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Like",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // 4. Super Like Button (Blue/Star)
                Surface(
                    onClick = onSuperLike,
                    shape = CircleShape,
                    color = Color(0xFF222533),
                    modifier = Modifier
                        .size(56.dp)
                        .border(1.dp, VerifiedBlue.copy(alpha = 0.6f), CircleShape)
                        .testTag("button_superlike_${profile.userId}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Super Like",
                            tint = VerifiedBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // 5. Boost Button (Purple Lightning)
                Surface(
                    onClick = { /* Boost action */ },
                    shape = CircleShape,
                    color = Color(0xFF222533),
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, BoostPurple.copy(alpha = 0.5f), CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Boost",
                            tint = BoostPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PillTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.18f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TagChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF222533))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VerifiedBlue,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
