package com.blockforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockforge.data.repository.CallType
import com.blockforge.data.repository.SystemCallEntry
import com.blockforge.ui.components.BlockedCallCard
import com.blockforge.ui.components.EmptyState
import com.blockforge.ui.theme.StatusInactive
import com.blockforge.ui.viewmodel.BlocklistViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Log screen showing call history and blocked calls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    viewModel: BlocklistViewModel = hiltViewModel()
) {
    val recentBlockedCalls by viewModel.recentBlockedCalls.collectAsState()
    val totalBlocked by viewModel.totalBlockedCount.collectAsState()
    val systemCallLog by viewModel.systemCallLog.collectAsState()
    val isLoading by viewModel.isLoadingCallLog.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Call Log") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (selectedTab == 1 && recentBlockedCalls.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearBlockedCallLogs() }
                        ) {
                            Text(
                                text = "Clear All",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All Calls") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Blocked")
                            if (totalBlocked > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge { Text("$totalBlocked") }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Block, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> SystemCallLogTab(
                    calls = systemCallLog,
                    isLoading = isLoading,
                    onRefresh = { viewModel.refreshSystemCallLog() }
                )
                1 -> BlockedCallsTab(
                    calls = recentBlockedCalls,
                    onDelete = { viewModel.deleteBlockedCall(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SystemCallLogTab(
    calls: List<SystemCallEntry>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (calls.isEmpty() && !isLoading) {
            EmptyState(
                icon = Icons.Default.History,
                title = "No Call History",
                subtitle = "Your call history will appear here"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = calls,
                    key = { it.id }
                ) { call ->
                    SystemCallCard(call = call)
                }
            }
        }
    }
}

@Composable
private fun BlockedCallsTab(
    calls: List<com.blockforge.data.database.BlockedCall>,
    onDelete: (com.blockforge.data.database.BlockedCall) -> Unit
) {
    if (calls.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Block,
            title = "No Blocked Calls",
            subtitle = "Blocked calls will appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = calls,
                key = { it.id }
            ) { call ->
                BlockedCallCard(
                    call = call,
                    onDelete = { onDelete(call) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

/**
 * Card for displaying a system call log entry
 */
@Composable
private fun SystemCallCard(call: SystemCallEntry) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    // Icon and color based on call type
    val (icon, iconColor, typeLabel) = remember(call.type) {
        when (call.type) {
            CallType.INCOMING -> Triple(
                Icons.Default.CallReceived,
                Color(0xFF4CAF50),  // Green
                "Incoming"
            )
            CallType.OUTGOING -> Triple(
                Icons.Default.CallMade,
                Color(0xFF2196F3),  // Blue
                "Outgoing"
            )
            CallType.MISSED -> Triple(
                Icons.Default.CallMissed,
                Color(0xFFFF9800),  // Orange
                "Missed"
            )
            CallType.REJECTED -> Triple(
                Icons.Default.CallEnd,
                StatusInactive,
                "Rejected"
            )
            CallType.BLOCKED -> Triple(
                Icons.Default.Block,
                StatusInactive,
                "Blocked"
            )
            CallType.UNKNOWN -> Triple(
                Icons.Default.Phone,
                Color.Gray,
                "Unknown"
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Call type icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = typeLabel,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Contact name or phone number
                Text(
                    text = call.contactName ?: call.number,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Phone number if we have contact name
                if (call.contactName != null) {
                    Text(
                        text = call.number,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Call info row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type badge
                    Surface(
                        color = iconColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = iconColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    // Duration (for answered calls)
                    if (call.duration > 0) {
                        Text(
                            text = formatDuration(call.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Time
            Text(
                text = dateFormat.format(Date(call.date)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Format duration in seconds to a readable string
 */
private fun formatDuration(seconds: Long): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}
