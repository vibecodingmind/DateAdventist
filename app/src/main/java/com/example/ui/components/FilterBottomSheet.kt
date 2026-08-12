package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.viewmodel.DiscoveryFilterState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    filterState: DiscoveryFilterState,
    onApplyFilter: (DiscoveryFilterState) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var minAge by remember { mutableFloatStateOf(filterState.minAge.toFloat()) }
    var maxAge by remember { mutableFloatStateOf(filterState.maxAge.toFloat()) }
    var maxDistance by remember { mutableFloatStateOf(filterState.maxDistanceKm.toFloat()) }
    var gender by remember { mutableStateOf(filterState.genderPreference) }
    var affiliation by remember { mutableStateOf(filterState.adventistAffiliation) }
    var churchInvolvement by remember { mutableStateOf(filterState.churchInvolvement) }
    var diet by remember { mutableStateOf(filterState.dietPreference) }
    var intention by remember { mutableStateOf(filterState.relationshipIntention) }
    var verifiedOnly by remember { mutableStateOf(filterState.verifiedOnly) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF161822)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161822))
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .testTag("filter_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Adventist Preference Filters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = {
                    onReset()
                    onDismiss()
                }) {
                    Text("Reset", color = Color(0xFFFF3366))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gender
            Text(text = "Gender Preference", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                listOf("All", "Female", "Male").forEach { option ->
                    FilterChip(
                        selected = gender == option,
                        onClick = { gender = option },
                        label = { Text(option, color = if (gender == option) Color.White else Color(0xFF94A3B8)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF3366),
                            containerColor = Color(0xFF090A0E)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Age Range
            Text(
                text = "Age Range: ${minAge.toInt()} - ${maxAge.toInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            RangeSlider(
                value = minAge..maxAge,
                onValueChange = { range ->
                    minAge = range.start
                    maxAge = range.endInclusive
                },
                valueRange = 18f..75f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF3366),
                    activeTrackColor = Color(0xFFFF3366),
                    inactiveTrackColor = Color(0xFF282A3A)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Maximum Distance
            Text(
                text = "Maximum Distance: ${maxDistance.toInt()} km",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Slider(
                value = maxDistance,
                onValueChange = { maxDistance = it },
                valueRange = 10f..500f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF3366),
                    activeTrackColor = Color(0xFFFF3366),
                    inactiveTrackColor = Color(0xFF282A3A)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Adventist Affiliation
            Text(text = "Adventist Affiliation", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow {
                listOf("All", "Seventh-day Adventist", "Adventist Convert", "Adventist-Friendly").forEach { opt ->
                    FilterChip(
                        selected = affiliation == opt,
                        onClick = { affiliation = opt },
                        label = { Text(opt, color = if (affiliation == opt) Color.White else Color(0xFF94A3B8)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF3366),
                            containerColor = Color(0xFF090A0E)
                        ),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Church Involvement
            Text(text = "Church Involvement", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow {
                listOf("All", "Very active", "Active", "Occasionally involved").forEach { opt ->
                    FilterChip(
                        selected = churchInvolvement == opt,
                        onClick = { churchInvolvement = opt },
                        label = { Text(opt, color = if (churchInvolvement == opt) Color.White else Color(0xFF94A3B8)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF3366),
                            containerColor = Color(0xFF090A0E)
                        ),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Diet Preference
            Text(text = "Diet Preference", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow {
                listOf("All", "Vegetarian", "Vegan", "Plant-Based", "Standard").forEach { opt ->
                    FilterChip(
                        selected = diet == opt,
                        onClick = { diet = opt },
                        label = { Text(opt, color = if (diet == opt) Color.White else Color(0xFF94A3B8)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF3366),
                            containerColor = Color(0xFF090A0E)
                        ),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Relationship Intention
            Text(text = "Relationship Goal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow {
                listOf("All", "Marriage", "Serious Relationship", "Getting to know someone", "Friendship first").forEach { opt ->
                    FilterChip(
                        selected = intention == opt,
                        onClick = { intention = opt },
                        label = { Text(opt, color = if (intention == opt) Color.White else Color(0xFF94A3B8)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF3366),
                            containerColor = Color(0xFF090A0E)
                        ),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Verified Only Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Verified Members Only",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = verifiedOnly,
                    onCheckedChange = { verifiedOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = VerifiedBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Apply Button
            Button(
                onClick = {
                    onApplyFilter(
                        filterState.copy(
                            minAge = minAge.toInt(),
                            maxAge = maxAge.toInt(),
                            maxDistanceKm = maxDistance.toInt(),
                            genderPreference = gender,
                            adventistAffiliation = affiliation,
                            churchInvolvement = churchInvolvement,
                            dietPreference = diet,
                            relationshipIntention = intention,
                            verifiedOnly = verifiedOnly
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_apply_filter")
            ) {
                Text("Apply Preferences", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
