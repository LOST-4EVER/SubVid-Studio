package com.example.ui.components.batch

import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatchOperationType
import com.example.model.BatchTask
import com.example.model.BatchTaskStatus
import com.example.ui.icons.StudioIcons
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ImmersiveActionBg
import com.example.ui.theme.ImmersiveCardBorder
import com.example.ui.theme.ImmersivePrimary
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary

@Composable
fun BatchTaskItem(
    task: BatchTask,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                when (task.operationType) {
                                    BatchOperationType.CONVERT_FORMAT -> AccentCyan.copy(alpha = 0.2f)
                                    BatchOperationType.TIME_SHIFT -> AccentAmber.copy(alpha = 0.2f)
                                    BatchOperationType.HARD_SUB_BURN_IN -> AccentEmerald.copy(alpha = 0.2f)
                                    else -> ImmersivePrimary.copy(alpha = 0.2f)
                                },
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (task.operationType) {
                                BatchOperationType.CONVERT_FORMAT -> StudioIcons.Subtitles
                                BatchOperationType.TIME_SHIFT -> StudioIcons.Timer
                                BatchOperationType.HARD_SUB_BURN_IN -> StudioIcons.Movie
                                else -> StudioIcons.Layers
                            },
                            contentDescription = null,
                            tint = when (task.operationType) {
                                BatchOperationType.CONVERT_FORMAT -> AccentCyan
                                BatchOperationType.TIME_SHIFT -> AccentAmber
                                BatchOperationType.HARD_SUB_BURN_IN -> AccentEmerald
                                else -> ImmersivePrimary
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = task.title,
                            color = ImmersiveTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = task.outputFileName,
                            color = ImmersiveTextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (task.status) {
                        BatchTaskStatus.COMPLETED -> AccentEmerald.copy(alpha = 0.2f)
                        BatchTaskStatus.PROCESSING -> AccentCyan.copy(alpha = 0.2f)
                        BatchTaskStatus.FAILED -> AccentRose.copy(alpha = 0.2f)
                        BatchTaskStatus.PENDING -> ImmersiveActionBg
                    }
                ) {
                    Text(
                        text = when (task.status) {
                            BatchTaskStatus.COMPLETED -> "DONE"
                            BatchTaskStatus.PROCESSING -> "${(task.progress * 100).toInt()}%"
                            BatchTaskStatus.FAILED -> "FAILED"
                            BatchTaskStatus.PENDING -> "QUEUED"
                        },
                        color = when (task.status) {
                            BatchTaskStatus.COMPLETED -> AccentEmerald
                            BatchTaskStatus.PROCESSING -> AccentCyan
                            BatchTaskStatus.FAILED -> AccentRose
                            BatchTaskStatus.PENDING -> ImmersiveTextSecondary
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (task.status == BatchTaskStatus.PROCESSING) {
                LinearProgressIndicator(
                    progress = { task.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    color = AccentCyan,
                    trackColor = ImmersiveActionBg
                )
            }
        }
    }
}
