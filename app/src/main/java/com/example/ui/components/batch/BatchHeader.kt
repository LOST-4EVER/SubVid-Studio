package com.example.ui.components.batch

import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessingEngine
import com.example.model.ProcessingSettings
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun BatchHeader(
    settings: ProcessingSettings,
    onToggleEngine: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AccentEmerald.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(StudioIcons.Layers, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(20.dp))
                }

                Column {
                    Text(
                        text = "Batch Processing Hub",
                        color = ImmersiveTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Multi-file conversion, sync & burn-in queue",
                        color = ImmersiveTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Engine Badge & Toggle
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan.copy(alpha = 0.15f) else ImmersivePrimary.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan.copy(alpha = 0.4f) else ImmersivePrimary.copy(alpha = 0.4f)
                ),
                modifier = Modifier.clickable { onToggleEngine() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (settings.engine == ProcessingEngine.GPU_HARDWARE) StudioIcons.Gpu else StudioIcons.Cpu,
                        contentDescription = null,
                        tint = if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan else ImmersivePrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (settings.engine == ProcessingEngine.GPU_HARDWARE) "GPU Engine" else "CPU Multi-Core",
                        color = if (settings.engine == ProcessingEngine.GPU_HARDWARE) AccentCyan else ImmersivePrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
