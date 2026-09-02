package com.sanket_satpute_20.ironmind.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.ironmind.data.Task
import com.sanket_satpute_20.ironmind.psychology.UserType
import com.sanket_satpute_20.ironmind.ui.components.PathNodeUI
import java.time.LocalTime
import kotlin.math.sin

internal fun LazyListScope.HomeMissionBoardSection(
    tasks: List<Task>,
    homeMissionItems: List<HomeMissionItem>,
    currentTime: LocalTime,
    userType: UserType,
    allDone: Boolean,
    resolvedExpanded: Boolean,
    onResolvedExpandedChange: (Boolean) -> Unit,
    highlightedTaskId: Int?,
    onAddTaskClick: () -> Unit,
    onDone: (Task) -> Unit,
    onSkip: (Task) -> Unit,
    onDoneChain: (String) -> Unit,
    onSkipChain: (String) -> Unit,
    onStartCountdown: (Task) -> Unit,
    onStartPomodoro: (Task) -> Unit,
    onOpenLiveTask: (Task) -> Unit
) {
    if (tasks.isEmpty()) {
        item { EmptyState(onAddTaskClick = onAddTaskClick, userType = userType) }
        return
    }

    val visibleMissionItems = homeMissionItems.filterNot { highlightedTaskId != null && it.task.id == highlightedTaskId }
    val resolvedMissions = visibleMissionItems.filter {
        it.state == HomeMissionState.COMPLETED || it.state == HomeMissionState.SKIPPED
    }
    val activeMissions = visibleMissionItems.filter {
        it.state != HomeMissionState.COMPLETED && it.state != HomeMissionState.SKIPPED
    }

    item { AllDoneCelebration(allDone = allDone) }

    item {
        Spacer(modifier = Modifier.height(8.dp))
        MissionSectionHeader(
            title = "TODAY'S MISSIONS",
            subtitle = "Complete the next right action. Everything else can wait.",
            accent = Color.White,
            count = activeMissions.size
        )
    }

    // Active work stays in the decision path. Resolved work is proof, not clutter.
    itemsIndexed(activeMissions) { index, missionItem ->
        TaskCardPremium(
            missionItem = missionItem,
            index = index,
            currentTime = currentTime,
            onDone = { onDone(missionItem.task) },
            onSkip = { onSkip(missionItem.task) },
            onStartCountdown = { onStartCountdown(missionItem.task) },
            onStartPomodoro = { onStartPomodoro(missionItem.task) },
            onOpenLiveTask = { onOpenLiveTask(missionItem.task) },
            onDoneLinkedTask = { onDone(it) },
            onSkipLinkedTask = { onSkip(it) },
            onDoneChain = { onDoneChain(missionItem.task.parentMissionId) },
            onSkipChain = { onSkipChain(missionItem.task.parentMissionId) }
        )
    }

    if (resolvedMissions.isNotEmpty()) {
        item {
            ResolvedMissionSummaryCard(
                completedCount = resolvedMissions.count { it.state == HomeMissionState.COMPLETED },
                skippedCount = resolvedMissions.count { it.state == HomeMissionState.SKIPPED },
                onExpand = { onResolvedExpandedChange(!resolvedExpanded) }
            )
        }
    }

    if (resolvedExpanded) {
        item {
            MissionSectionHeader(
                title = "ARCHIVED PROOF",
                subtitle = "Completed and skipped missions stay visible without competing with active work.",
                accent = Color.White.copy(alpha = 0.72f),
                count = resolvedMissions.size,
                trailingText = "CLOSE",
                onTrailingClick = { onResolvedExpandedChange(false) }
            )
        }
        itemsIndexed(resolvedMissions) { index, missionItem ->
            TaskCardPremium(
                missionItem = missionItem,
                index = activeMissions.size + index,
                currentTime = currentTime,
                onDone = { onDone(missionItem.task) },
                onSkip = { onSkip(missionItem.task) },
                onStartCountdown = { onStartCountdown(missionItem.task) },
                onStartPomodoro = { onStartPomodoro(missionItem.task) },
                onOpenLiveTask = { onOpenLiveTask(missionItem.task) },
                onDoneLinkedTask = { onDone(it) },
                onSkipLinkedTask = { onSkip(it) },
                onDoneChain = { onDoneChain(missionItem.task.parentMissionId) },
                onSkipChain = { onSkipChain(missionItem.task.parentMissionId) }
            )
        }
    }
}
