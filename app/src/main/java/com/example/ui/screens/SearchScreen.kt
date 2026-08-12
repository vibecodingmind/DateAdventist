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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.CompatibilityBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.VerifiedBlue
import com.example.ui.viewmodel.DiscoveryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    discoveryViewModel: DiscoveryViewModel,
    onSelectProfile: (ProfileEntity) -> Unit
) {
    val profiles by discoveryViewModel.discoveryProfiles.collectAsState()
    val filterState by discoveryViewModel.filterState.collectAsState()

    val presets = listOf(
        "Pathfinders",
        "Vegetarian",
        "Youth Ministry",
        "A cappella",
        "Marriage Focused",
        "Andrews / Loma Linda"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0E))
            .testTag("search_screen")
    ) {
        // Search Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Explore",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { q -> discoveryViewModel.updateFilter { it.copy(searchQuery = q) } },
                placeholder = { Text("Search by church, university, or interest...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = Color(0xFF64748B))
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
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
                    .testTag("search_screen_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Quick Presets",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = filterState.searchQuery == preset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) Color(0xFFFF3366) else Color(0xFF161822))
                            .border(1.dp, if (isSelected) Color(0xFFFF3366) else Color(0xFF282A3A), RoundedCornerShape(50))
                            .clickable {
                                discoveryViewModel.updateFilter {
                                    it.copy(searchQuery = if (it.searchQuery == preset) "" else preset)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Search Results Section
        Text(
            text = "Found ${profiles.size} Adventist Profiles",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = VerifiedBlue,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(profiles) { profile ->
                SearchResultRow(
                    profile = profile,
                    compatibilityScore = discoveryViewModel.getCompatibilityScore(profile),
                    onClick = { onSelectProfile(profile) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    profile: ProfileEntity,
    compatibilityScore: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF282A3A), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profile.primaryPhoto,
                contentDescription = profile.fullName,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${profile.fullName}, ${profile.age}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (profile.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerificationBadge()
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Church,
                        contentDescription = null,
                        tint = VerifiedBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = profile.localChurch,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${profile.city}, ${profile.country}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            CompatibilityBadge(score = compatibilityScore, showBreakdownOnClick = false)
        }
    }
}
