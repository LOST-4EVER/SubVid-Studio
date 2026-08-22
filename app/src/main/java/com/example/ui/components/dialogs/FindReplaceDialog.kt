package com.example.ui.components.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SubtitleTrack
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun FindReplaceDialog(
    subtitleTrack: SubtitleTrack,
    onDismiss: () -> Unit,
    onReplaceAll: (findText: String, replaceText: String, matchCase: Boolean, useRegex: Boolean) -> Unit
) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var useRegex by remember { mutableStateOf(false) }

    // Real-time match counter
    val matchCount = remember(findText, matchCase, useRegex, subtitleTrack.cues) {
        if (findText.isEmpty()) 0
        else {
            try {
                if (useRegex) {
                    val regexOptions = if (matchCase) setOf() else setOf(RegexOption.IGNORE_CASE)
                    val regex = Regex(findText, regexOptions)
                    subtitleTrack.cues.sumOf { regex.findAll(it.text).count() }
                } else {
                    if (matchCase) {
                        subtitleTrack.cues.sumOf { cue ->
                            cue.text.windowed(findText.length, 1).count { it == findText }
                        }
                    } else {
                        val regex = Regex(Regex.escape(findText), RegexOption.IGNORE_CASE)
                        subtitleTrack.cues.sumOf { regex.findAll(it.text).count() }
                    }
                }
            } catch (e: Exception) {
                0
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmersiveSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(StudioIcons.SyncAudio, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                Text("Find & Replace Subtitles", color = ImmersiveTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Search across all ${subtitleTrack.cues.size} subtitle cues in the active track:",
                    color = ImmersiveTextSecondary,
                    fontSize = 11.sp
                )

                // Find input
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    label = { Text("Find text", color = ImmersiveTextSecondary, fontSize = 11.sp) },
                    placeholder = { Text("Enter word or pattern...", color = ImmersiveTextSecondary.copy(alpha = 0.5f), fontSize = 11.sp) },
                    trailingIcon = {
                        if (findText.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (matchCount > 0) AccentEmerald.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "$matchCount found",
                                    color = if (matchCount > 0) AccentEmerald else ImmersiveTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ImmersiveTextPrimary,
                        unfocusedTextColor = ImmersiveTextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedContainerColor = ImmersiveBg,
                        unfocusedContainerColor = ImmersiveBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("find_input_field")
                )

                // Replace input
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text("Replace with", color = ImmersiveTextSecondary, fontSize = 11.sp) },
                    placeholder = { Text("Replacement text...", color = ImmersiveTextSecondary.copy(alpha = 0.5f), fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ImmersiveTextPrimary,
                        unfocusedTextColor = ImmersiveTextPrimary,
                        focusedBorderColor = ImmersivePrimary,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedContainerColor = ImmersiveBg,
                        unfocusedContainerColor = ImmersiveBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("replace_input_field")
                )

                // Options Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Checkbox(
                            checked = matchCase,
                            onCheckedChange = { matchCase = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AccentCyan,
                                checkmarkColor = Color.Black,
                                uncheckedColor = ImmersiveBorder
                            )
                        )
                        Text("Match Case (Aa)", color = ImmersiveTextPrimary, fontSize = 11.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Checkbox(
                            checked = useRegex,
                            onCheckedChange = { useRegex = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AccentCyan,
                                checkmarkColor = Color.Black,
                                uncheckedColor = ImmersiveBorder
                            )
                        )
                        Text("Regex Mode (.*)", color = ImmersiveTextPrimary, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onReplaceAll(findText, replaceText, matchCase, useRegex)
                    onDismiss()
                },
                enabled = findText.isNotEmpty() && matchCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersivePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("replace_all_button")
            ) {
                Text("Replace All ($matchCount)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Cancel", color = ImmersiveTextSecondary, fontSize = 11.sp)
            }
        }
    )
}
