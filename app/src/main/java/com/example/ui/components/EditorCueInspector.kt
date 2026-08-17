package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleCue
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary

@Composable
fun EditorCueInspector(
    selectedCue: SubtitleCue,
    totalCuesCount: Int,
    currentCueIndex: Int,
    onSelectPreviousCue: () -> Unit,
    onSelectNextCue: () -> Unit,
    onDuplicateCue: () -> Unit,
    onUpdateCueText: (SubtitleCue, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with index and navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ImmersiveActionBg,
                        modifier = Modifier.clickable { onSelectPreviousCue() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(StudioIcons.Rewind, contentDescription = "Prev Cue", tint = ImmersivePrimary, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Prev", color = ImmersiveTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Cue ${if (currentCueIndex >= 0) currentCueIndex + 1 else 1}/$totalCuesCount",
                        color = ImmersivePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ImmersiveActionBg,
                        modifier = Modifier.clickable { onSelectNextCue() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Next", color = ImmersiveTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(2.dp))
                            Icon(StudioIcons.Forward, contentDescription = "Next Cue", tint = ImmersivePrimary, modifier = Modifier.size(11.dp))
                        }
                    }
                }

                // Duplicate button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ImmersiveActionBg,
                    modifier = Modifier.clickable { onDuplicateCue() }
                ) {
                    Text("+ Duplicate", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }

            // Text Editor Field
            OutlinedTextField(
                value = selectedCue.text,
                onValueChange = { newText ->
                    onUpdateCueText(selectedCue, newText)
                },
                singleLine = false,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ImmersiveTextPrimary,
                    unfocusedTextColor = ImmersiveTextPrimary,
                    focusedBorderColor = ImmersivePrimary,
                    unfocusedBorderColor = ImmersiveBorder,
                    focusedContainerColor = ImmersiveBg,
                    unfocusedContainerColor = ImmersiveBg
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cue_text_input")
            )
        }
    }
}
