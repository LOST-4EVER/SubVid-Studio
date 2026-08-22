package com.example.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleTrack
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun TimingSanitizerDialog(
    subtitleTrack: SubtitleTrack,
    onDismiss: () -> Unit,
    onSanitize: (minGapMs: Long) -> Unit,
    onSortChronologically: () -> Unit
) {
    var selectedGapMs by remember { mutableStateOf(50L) }

    // Detect issues
    val cues = subtitleTrack.cues
    val sorted = cues.sortedBy { it.startTimeMs }
    var overlappingCount = 0
    var negativeDurationCount = 0

    for (i in sorted.indices) {
        val cue = sorted[i]
        if (cue.endTimeMs <= cue.startTimeMs) {
            negativeDurationCount++
        }
        if (i < sorted.size - 1) {
            val next = sorted[i + 1]
            if (cue.endTimeMs > next.startTimeMs) {
                overlappingCount++
            }
        }
    }

    val totalIssues = overlappingCount + negativeDurationCount

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(StudioIcons.Timer, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                Text("Subtitle Gap & Overlap Sanitizer", color = ImmersiveTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status card
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (totalIssues > 0) AccentAmber.copy(alpha = 0.15f) else AccentEmerald.copy(alpha = 0.15f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (totalIssues > 0) AccentAmber.copy(alpha = 0.4f) else AccentEmerald.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (totalIssues > 0) StudioIcons.Warning else StudioIcons.Check,
                            contentDescription = null,
                            tint = if (totalIssues > 0) AccentAmber else AccentEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (totalIssues > 0) "$totalIssues Timing Conflicts Found" else "All Subtitle Timings Clean",
                                color = ImmersiveTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (totalIssues > 0) "$overlappingCount overlapping boundaries, $negativeDurationCount negative durations" else "No overlaps or zero-duration cues detected",
                                color = ImmersiveTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Minimum Gap selection
                Text("Minimum Gap Between Cues:", color = ImmersiveTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0L, 30L, 50L, 100L).forEach { gap ->
                        val isSelected = selectedGapMs == gap
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) AccentAmber else ImmersiveActionBg,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedGapMs = gap }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = if (gap == 0L) "0ms" else "${gap}ms",
                                    color = if (isSelected) Color.Black else ImmersiveTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Sort Chronologically secondary action
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ImmersiveActionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSortChronologically()
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(StudioIcons.List, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(14.dp))
                        Text("Sort All Cues Chronologically by Start Time", color = ImmersivePrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSanitize(selectedGapMs)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Auto-Repair Overlaps", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = ImmersiveTextSecondary, fontSize = 11.sp)
            }
        }
    )
}
