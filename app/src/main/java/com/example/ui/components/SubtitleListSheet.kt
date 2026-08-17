package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleCue
import com.example.model.SubtitleTrack
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveOnPrimary
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceCard
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleListSheet(
    subtitleTrack: SubtitleTrack,
    currentPositionMs: Long,
    selectedCue: SubtitleCue?,
    onDismiss: () -> Unit,
    onSelectCue: (SubtitleCue) -> Unit,
    onJumpToCue: (SubtitleCue) -> Unit,
    onUpdateCueText: (SubtitleCue, String) -> Unit,
    onDeleteCue: (SubtitleCue) -> Unit,
    onAddNewCue: () -> Unit,
    onBatchShiftTiming: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var editingCueId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ImmersiveSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ImmersiveBorder)
            )
        },
        modifier = Modifier.testTag("subtitle_list_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sheet Title & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ImmersivePrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(StudioIcons.Subtitles, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Subtitle Track Cues", color = ImmersiveTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${subtitleTrack.cues.size} segments loaded", color = ImmersiveTextMuted, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = onAddNewCue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersivePrimary,
                        contentColor = ImmersiveOnPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_new_cue_btn")
                ) {
                    Icon(StudioIcons.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Cue", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Search Bar & Timing Shift Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter subtitles...", color = ImmersiveTextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ImmersiveTextPrimary,
                        unfocusedTextColor = ImmersiveTextSecondary,
                        focusedBorderColor = ImmersivePrimary,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedContainerColor = ImmersiveBg,
                        unfocusedContainerColor = ImmersiveBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                )

                // Quick shift all subtitles -500ms / +500ms (Audio Sync feature)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ImmersiveActionBg,
                    modifier = Modifier.clickable { onBatchShiftTiming(-500L) }
                ) {
                    Text("-0.5s", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp))
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ImmersiveActionBg,
                    modifier = Modifier.clickable { onBatchShiftTiming(500L) }
                ) {
                    Text("+0.5s", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp))
                }
            }

            // Subtitle List
            val filteredCues = subtitleTrack.cues.filter {
                searchQuery.isEmpty() || it.text.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(filteredCues) { index, cue ->
                    val isSelected = selectedCue?.id == cue.id
                    val isActive = cue.isActiveAt(currentPositionMs)
                    val isEditing = editingCueId == cue.id

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) ImmersivePrimary.copy(alpha = 0.15f) else ImmersiveSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) ImmersivePrimary else if (isActive) AccentCyan else ImmersiveBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectCue(cue)
                                onJumpToCue(cue)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        color = if (isSelected) ImmersivePrimary else ImmersiveTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${SubtitleCue.formatTimestampShort(cue.startTimeMs)} ➔ ${SubtitleCue.formatTimestampShort(cue.endTimeMs)}",
                                        color = if (isActive) AccentCyan else ImmersiveTextSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Edit text button
                                    IconButton(
                                        onClick = {
                                            if (isEditing) {
                                                onUpdateCueText(cue, editingText)
                                                editingCueId = null
                                            } else {
                                                editingCueId = cue.id
                                                editingText = cue.text
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isEditing) StudioIcons.Check else StudioIcons.Edit,
                                            contentDescription = "Edit Text",
                                            tint = if (isEditing) AccentEmerald else ImmersivePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete cue button
                                    IconButton(
                                        onClick = { onDeleteCue(cue) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = StudioIcons.Delete,
                                            contentDescription = "Delete Cue",
                                            tint = Color(0xFFF87171),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (isEditing) {
                                OutlinedTextField(
                                    value = editingText,
                                    onValueChange = { editingText = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = ImmersiveTextPrimary,
                                        unfocusedTextColor = ImmersiveTextPrimary,
                                        focusedBorderColor = ImmersivePrimary,
                                        unfocusedBorderColor = ImmersiveBorder,
                                        focusedContainerColor = ImmersiveBg,
                                        unfocusedContainerColor = ImmersiveBg
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = cue.text,
                                    color = ImmersiveTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
