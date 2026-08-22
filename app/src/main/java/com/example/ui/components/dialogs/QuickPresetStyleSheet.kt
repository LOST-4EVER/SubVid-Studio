package com.example.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

data class StylePreset(
    val id: String,
    val name: String,
    val description: String,
    val previewText: String,
    val textColor: Color,
    val strokeColor: Color,
    val bgColor: Color,
    val tag: String,
    val tagColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPresetStyleSheet(
    onDismiss: () -> Unit,
    onSelectPreset: (presetId: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val presets = listOf(
        StylePreset(
            id = "cinema_gold",
            name = "Cinema Gold",
            description = "Warm golden typography with crisp drop shadow for dramatic cinema feel",
            previewText = "“In space no one can hear you scream.”",
            textColor = Color(0xFFFFD700),
            strokeColor = Color.Black,
            bgColor = Color.Transparent,
            tag = "CINEMA",
            tagColor = AccentAmber
        ),
        StylePreset(
            id = "tiktok_neon",
            name = "TikTok / Reels Neon Yellow",
            description = "High-energy bold yellow with thick black outline for viral social clips",
            previewText = "WAIT UNTIL THE END 😱🔥",
            textColor = Color(0xFFFFF500),
            strokeColor = Color.Black,
            bgColor = Color.Transparent,
            tag = "VIRAL",
            tagColor = AccentCyan
        ),
        StylePreset(
            id = "clean_minimal",
            name = "Clean Minimalist White",
            description = "Modern thin white sans-serif with subtle soft outline for vlogs & interviews",
            previewText = "Welcome to today's design review.",
            textColor = Color.White,
            strokeColor = Color(0x88000000),
            bgColor = Color.Transparent,
            tag = "MINIMAL",
            tagColor = ImmersivePrimary
        ),
        StylePreset(
            id = "closed_caption",
            name = "Closed Caption Box",
            description = "Broadcast standard high-contrast semi-transparent black background box",
            previewText = "[Music playing softly in background]",
            textColor = Color.White,
            strokeColor = Color.Transparent,
            bgColor = Color(0xD9000000),
            tag = "ACCESSIBLE",
            tagColor = AccentEmerald
        ),
        StylePreset(
            id = "anime_ass",
            name = "Anime Fansub Cyan",
            description = "Vibrant light-cyan subtitle with midnight-blue shadow outline",
            previewText = "Watashi wa SubVid Studio desu!",
            textColor = Color(0xFFE0FFFF),
            strokeColor = Color(0xFF191970),
            bgColor = Color.Transparent,
            tag = "ANIME",
            tagColor = AccentPurple
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ImmersiveSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(StudioIcons.Sparkles, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                Text("Studio Subtitle Style Packs", color = ImmersiveTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text("Tap any preset to apply typography and color styling to all subtitles in the project:", color = ImmersiveTextSecondary, fontSize = 11.sp)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                items(presets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ImmersiveBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectPreset(preset.id)
                                onDismiss()
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
                                Text(preset.name, color = ImmersiveTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = preset.tagColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = preset.tag,
                                        color = preset.tagColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(preset.description, color = ImmersiveTextSecondary, fontSize = 10.sp)

                            // Preview Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF14171D), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = preset.bgColor,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = preset.previewText,
                                        color = preset.textColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
