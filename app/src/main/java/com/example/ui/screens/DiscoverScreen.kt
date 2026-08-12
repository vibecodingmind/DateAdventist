package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ProfileEntity
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.SubscriptionBadge
import com.example.ui.components.SwipeCard
import com.example.ui.theme.AdventRedPrimary
import com.example.ui.viewmodel.DiscoveryViewModel

@Composable
fun DiscoverScreen(
    discoveryViewModel: DiscoveryViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateToSubscription: () -> Unit = {}
) {
    val profiles by discoveryViewModel.discoveryProfiles.collectAsState()
    val filterState by discoveryViewModel.filterState.collectAsState()
    val selectedTab by discoveryViewModel.selectedTab.collectAsState()
    val matchAlertProfile by discoveryViewModel.matchAlertProfile.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var currentCardIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf("Recommended", "Nearby", "New", "Most Compatible", "Verified")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0E))
            .testTag("discover_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sleek Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF3366), Color(0xFFE11D48))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "AdventHearts",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigateToSubscription() }
                        .padding(end = 8.dp)
                ) {
                    SubscriptionBadge(text = "GET PRO ⭐")
                }

                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161822))
                        .testTag("button_open_filters")
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filters",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filterState.searchQuery,
                    onValueChange = { q -> discoveryViewModel.updateFilter { it.copy(searchQuery = q) } },
                    placeholder = { Text("Search Adventist singles...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF282A3A),
                        focusedContainerColor = Color(0xFF161822),
                        unfocusedContainerColor = Color(0xFF161822),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("search_input")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Discovery Tabs
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = Color(0xFF090A0E),
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { discoveryViewModel.setTab(tab) },
                        text = {
                            Text(
                                text = tab,
                                fontSize = 13.sp,
                                color = if (selectedTab == tab) Color(0xFFFF3366) else Color(0xFF94A3B8),
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Stack / Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (profiles.isNotEmpty() && currentCardIndex < profiles.size) {
                    val targetProfile = profiles[currentCardIndex]
                    val score = discoveryViewModel.getCompatibilityScore(targetProfile)

                    SwipeCard(
                        profile = targetProfile,
                        compatibilityScore = score,
                        onLike = {
                            discoveryViewModel.onLike(targetProfile, isSuperLike = false)
                            currentCardIndex++
                        },
                        onPass = {
                            discoveryViewModel.onPass(targetProfile)
                            currentCardIndex++
                        },
                        onSuperLike = {
                            discoveryViewModel.onLike(targetProfile, isSuperLike = true)
                            currentCardIndex++
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Thoughtful Empty State as requested
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Your next connection may be closer than you think.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You've reviewed all profiles in this view. Expand your search filter preferences or check back soon!",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    currentCardIndex = 0
                                    discoveryViewModel.updateFilter {
                                        it.copy(maxDistanceKm = 1000, minAge = 18, maxAge = 65)
                                    }
                                }
                            ) {
                                Text("Expand Filter Distance")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Filter Sheet
        if (showFilterSheet) {
            FilterBottomSheet(
                filterState = filterState,
                onApplyFilter = { updated ->
                    discoveryViewModel.updateFilter { updated }
                    currentCardIndex = 0
                },
                onReset = {
                    discoveryViewModel.updateFilter { com.example.ui.viewmodel.DiscoveryFilterState() }
                    currentCardIndex = 0
                },
                onDismiss = { showFilterSheet = false }
            )
        }

        // Mutual Match Overlay Modal ("It's a Match! ❤️")
        if (matchAlertProfile != null) {
            val matchedProf = matchAlertProfile!!
            AlertDialog(
                onDismissRequest = { discoveryViewModel.dismissMatchAlert() },
                title = {
                    Text(
                        text = "It's a Match! ❤️",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdventRedPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "You and ${matchedProf.fullName} liked each other!",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AsyncImage(
                            model = matchedProf.primaryPhoto,
                            contentDescription = matchedProf.fullName,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "“Both of you value Sabbath worship and Adventist ministry!”",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            discoveryViewModel.dismissMatchAlert()
                            onNavigateToChat("match_sarah_joshua") // Navigate to active chat
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdventRedPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.Message, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Message Now")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { discoveryViewModel.dismissMatchAlert() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Keep Discovering")
                    }
                }
            )
        }
    }
}
