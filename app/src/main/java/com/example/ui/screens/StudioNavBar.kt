package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun StudioNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = ImmersiveSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent, // Let Surface handle background
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
        ) {
            val items = listOf(
                NavigationItemData("Home", StudioIcons.Home, AppTab.HOME, "nav_home"),
                NavigationItemData("Batch Hub", StudioIcons.BatchQueue, AppTab.BATCH, "nav_batch"),
                NavigationItemData("Settings", StudioIcons.Settings, AppTab.SETTINGS, "nav_settings")
            )

            items.forEach { item ->
                val selected = currentTab == item.tab
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(item.tab) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ImmersivePrimary,
                        indicatorColor = ImmersivePrimary,
                        unselectedIconColor = ImmersiveTextSecondary,
                        unselectedTextColor = ImmersiveTextSecondary
                    ),
                    modifier = Modifier.testTag(item.testTag)
                )
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
