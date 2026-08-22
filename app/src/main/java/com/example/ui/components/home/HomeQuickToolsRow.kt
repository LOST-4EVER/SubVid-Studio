package com.example.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

data class QuickToolItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeQuickToolsRow(
    onImportSubtitle: () -> Unit,
    onOpenBatchHub: () -> Unit,
    onOpenStylePacks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        QuickToolItem(
            title = "Import Subtitles",
            subtitle = "SRT • VTT • ASS",
            icon = StudioIcons.Subtitles,
            iconTint = AccentCyan,
            onClick = onImportSubtitle
        ),
        QuickToolItem(
            title = "Batch Pipeline",
            subtitle = "Queue & Convert",
            icon = StudioIcons.Layers,
            iconTint = AccentPurple,
            onClick = onOpenBatchHub
        ),
        QuickToolItem(
            title = "Style Packs",
            subtitle = "Cinema • Neon",
            icon = StudioIcons.Sparkles,
            iconTint = AccentAmber,
            onClick = onOpenStylePacks
        )
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tools.forEach { tool ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { tool.onClick() }
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(tool.icon, contentDescription = null, tint = tool.iconTint, modifier = Modifier.size(20.dp))
                    }
                    Text(tool.title, color = ImmersiveTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(tool.subtitle, color = ImmersiveTextSecondary, fontSize = 9.sp, maxLines = 1)
                }
            }
        }
    }
}
