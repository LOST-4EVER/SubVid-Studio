package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary

@Composable
fun EditorToolbeltStrip(
    onPlacementClick: () -> Unit,
    onStyleClick: () -> Unit,
    onSubtitlesListClick: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFullscreenClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Fullscreen Theater Mode Button
        if (onFullscreenClick != null) {
            Surface(
                shape = RectangleShape,
                color = ImmersiveSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onFullscreenClick() }
                    .testTag("toolbelt_fullscreen_btn")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Icon(StudioIcons.Fullscreen, contentDescription = null, tint = ImmersivePrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Theater", color = ImmersiveTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Placement Matrix Button
        Surface(
            shape = RectangleShape,
            color = ImmersiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier
                .weight(1f)
                .clickable { onPlacementClick() }
                .testTag("toolbelt_position_btn")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Icon(StudioIcons.Position, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(4.dp))
                Text("Placement", color = ImmersiveTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Style Customizer Button
        Surface(
            shape = RectangleShape,
            color = ImmersiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier
                .weight(1f)
                .clickable { onStyleClick() }
                .testTag("toolbelt_style_btn")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Icon(StudioIcons.Style, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(4.dp))
                Text("Styling", color = ImmersiveTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Subtitle List Sheet Button
        Surface(
            shape = RectangleShape,
            color = ImmersiveSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
            modifier = Modifier
                .weight(1f)
                .clickable { onSubtitlesListClick() }
                .testTag("toolbelt_subtitles_btn")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Icon(StudioIcons.Subtitles, contentDescription = null, tint = ImmersiveTextPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(4.dp))
                Text("Cues List", color = ImmersiveTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Export Suite Button
        Surface(
            shape = RectangleShape,
            color = ImmersivePrimary,
            modifier = Modifier
                .weight(1.1f)
                .clickable { onExportClick() }
                .testTag("toolbelt_export_btn")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Icon(StudioIcons.Export, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(4.dp))
                Text("Export", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
