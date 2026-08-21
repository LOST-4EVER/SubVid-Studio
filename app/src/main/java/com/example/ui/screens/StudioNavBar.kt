package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
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
import com.example.model.AppTab
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextMuted
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun StudioNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    hasActiveProject: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RectangleShape,
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(58.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                NavigationItemData("Home", StudioIcons.Home, AppTab.HOME, "nav_home"),
                NavigationItemData("Editor", StudioIcons.Video, AppTab.EDITOR, "nav_editor"),
                NavigationItemData("Batch Hub", StudioIcons.BatchQueue, AppTab.BATCH, "nav_batch"),
                NavigationItemData("Settings", StudioIcons.Settings, AppTab.SETTINGS, "nav_settings")
            )

            items.forEach { item ->
                val selected = currentTab == item.tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (selected) ImmersivePrimary.copy(alpha = 0.12f) else Color.Transparent)
                        .clickable { onTabSelected(item.tab) }
                        .testTag(item.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    // Top active border indicator
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(ImmersivePrimary)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (selected) ImmersivePrimary else ImmersiveTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            if (item.tab == AppTab.EDITOR && hasActiveProject && !selected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(6.dp)
                                        .background(ImmersivePrimary, RectangleShape)
                                )
                            }
                        }
                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) ImmersivePrimary else ImmersiveTextMuted
                        )
                    }
                }
            }
        }
    }
}

private data class NavigationItemData(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tab: AppTab,
    val testTag: String
)

