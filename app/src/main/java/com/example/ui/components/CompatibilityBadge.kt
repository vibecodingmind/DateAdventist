package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CompatibilityGreen

@Composable
fun CompatibilityBadge(
    score: Int,
    modifier: Modifier = Modifier,
    showBreakdownOnClick: Boolean = true
) {
    var showModal by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CompatibilityGreen.copy(alpha = 0.18f))
            .clickable(enabled = showBreakdownOnClick) { showModal = true }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = CompatibilityGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$score% Match",
            color = CompatibilityGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        if (showBreakdownOnClick) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Details",
                tint = CompatibilityGreen,
                modifier = Modifier.size(12.dp)
            )
        }
    }

    if (showModal) {
        AlertDialog(
            onDismissRequest = { showModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = CompatibilityGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compatibility Score: $score%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Calculated using AdventHearts Faith-First Compatibility Engine:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    BreakdownRow(title = "Faith & Adventist Values (25%)", progress = 0.95f)
                    BreakdownRow(title = "Relationship Intention (20%)", progress = 0.90f)
                    BreakdownRow(title = "Lifestyle & Sabbath Habits (15%)", progress = 0.85f)
                    BreakdownRow(title = "Family & Marriage Goals (15%)", progress = 0.90f)
                    BreakdownRow(title = "Location & Proximity (10%)", progress = 0.75f)
                    BreakdownRow(title = "Common Ministry Interests (5%)", progress = 0.80f)

                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "💡 Note: AdventHearts compatibility scores offer guidance based on shared spiritual values, but personal prayer and connection remain key.",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModal = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun BreakdownRow(title: String, progress: Float) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CompatibilityGreen
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CompatibilityGreen,
            trackColor = CompatibilityGreen.copy(alpha = 0.15f)
        )
    }
}
