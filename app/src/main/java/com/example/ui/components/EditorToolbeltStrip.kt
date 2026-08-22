package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun EditorToolbeltStrip(
    hasSelectedCue: Boolean,
    onAddSubtitleClick: () -> Unit,
    onSplitClick: () -> Unit,
    onPlacementClick: () -> Unit,
    onStyleClick: () -> Unit,
    onPresetPacksClick: () -> Unit,
    onFindReplaceClick: () -> Unit,
    onSanitizeTimingClick: () -> Unit,
    onResizeTextClick: () -> Unit,
    onSubtitlesListClick: () -> Unit,
    onOptimizationClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Add Subtitle at Playhead
            ToolActionItem(
                icon = StudioIcons.Add,
                label = "Add Text",
                tint = ImmersivePrimary,
                testTag = "toolbelt_add_subtitle_btn",
                onClick = onAddSubtitleClick
            )

            // 2. Split at Playhead
            ToolActionItem(
                icon = StudioIcons.Split,
                label = "Split",
                tint = AccentCyan,
                testTag = "toolbelt_split_btn",
                onClick = onSplitClick
            )

            // 3. Find & Replace
            ToolActionItem(
                icon = StudioIcons.FindReplace,
                label = "Find/Replace",
                tint = AccentCyan,
                testTag = "toolbelt_find_replace_btn",
                onClick = onFindReplaceClick
            )

            // 4. Style Presets
            ToolActionItem(
                icon = StudioIcons.Sparkles,
                label = "Presets",
                tint = ImmersivePrimary,
                testTag = "toolbelt_presets_btn",
                onClick = onPresetPacksClick
            )

            // 5. Timing Sanitizer
            ToolActionItem(
                icon = StudioIcons.Broom,
                label = "Sanitize",
                tint = AccentEmerald,
                testTag = "toolbelt_sanitize_btn",
                onClick = onSanitizeTimingClick
            )

            // 6. Resize / Font Size
            ToolActionItem(
                icon = StudioIcons.Crop,
                label = "Resize Text",
                tint = AccentEmerald,
                testTag = "toolbelt_resize_btn",
                onClick = onResizeTextClick
            )

            // 7. Custom Style & Colors
            ToolActionItem(
                icon = StudioIcons.Style,
                label = "Style",
                tint = AccentAmber,
                testTag = "toolbelt_style_btn",
                onClick = onStyleClick
            )

            // 8. Placement & Position
            ToolActionItem(
                icon = StudioIcons.Placement,
                label = "Position",
                tint = AccentCyan,
                testTag = "toolbelt_position_btn",
                onClick = onPlacementClick
            )

            // 9. Duplicate Cue
            ToolActionItem(
                icon = StudioIcons.Copy,
                label = "Duplicate",
                tint = ImmersiveTextPrimary,
                enabled = hasSelectedCue,
                testTag = "toolbelt_duplicate_btn",
                onClick = onDuplicateClick
            )

            // 10. Optimization Options
            ToolActionItem(
                icon = StudioIcons.Speed,
                label = "Optimize",
                tint = AccentCyan,
                testTag = "toolbelt_optimize_btn",
                onClick = onOptimizationClick
            )

            // 11. All Cues List
            ToolActionItem(
                icon = StudioIcons.Subtitles,
                label = "All Cues",
                tint = ImmersivePrimary,
                testTag = "toolbelt_cues_list_btn",
                onClick = onSubtitlesListClick
            )

            // 12. Delete Selected Cue
            ToolActionItem(
                icon = StudioIcons.Delete,
                label = "Delete",
                tint = AccentRose,
                enabled = hasSelectedCue,
                testTag = "toolbelt_delete_btn",
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun ToolActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String,
    enabled: Boolean = true
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) ImmersiveActionBg else ImmersiveActionBg.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = Modifier
            .width(72.dp)
            .clickable(enabled = enabled) { onClick() }
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) tint else tint.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = if (enabled) ImmersiveTextPrimary else ImmersiveTextSecondary.copy(alpha = 0.4f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
