package com.blockforge.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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

// Pre-computed colors to avoid creating them during composition
private val ColorGreen = Color(0xFF4CAF50)
private val ColorBlue = Color(0xFF2196F3)
private val ColorOrange = Color(0xFFFF9800)

// Single date formatter instance (thread-safe for reading)
private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

/**
 * Log screen showing call history and blocked calls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    viewModel: BlocklistViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recentBlockedCalls by viewModel.recentBlockedCalls.collectAsState()
    val totalBlocked by viewModel.totalBlockedCount.collectAsState()
    val systemCallLog by viewModel.systemCallLog.collectAsState()
    val isLoading by viewModel.isLoadingCallLog.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Bottom sheet state for call actions
    var selectedNumber by remember { mutableStateOf<String?>(null) }
    var selectedContactName by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

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
                    onRefresh = { viewModel.refreshSystemCallLog() },
                    onCallClick = { number ->
                        // Single tap: call the number
                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:$number")
                        }
                        context.startActivity(intent)
                    },
                    onCallLongClick = { number, contactName ->
                        // Long press: show action sheet
                        selectedNumber = number
                        selectedContactName = contactName
                        showBottomSheet = true
                    }
                )
                1 -> BlockedCallsTab(
                    calls = recentBlockedCalls,
                    onDelete = { viewModel.deleteBlockedCall(it) },
                    onCallClick = { number ->
                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:$number")
                        }
                        context.startActivity(intent)
                    },
                    onCallLongClick = { number, contactName ->
                        selectedNumber = number
                        selectedContactName = contactName
                        showBottomSheet = true
                    }
                )
            }
        }
    }

    // Action bottom sheet
    if (showBottomSheet && selectedNumber != null) {
        CallActionBottomSheet(
            sheetState = sheetState,
            phoneNumber = selectedNumber!!,
            contactName = selectedContactName,
            onDismiss = { showBottomSheet = false },
            onCall = {
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$selectedNumber")
                }
                context.startActivity(intent)
                showBottomSheet = false
            },
            onSendSms = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$selectedNumber")
                }
                context.startActivity(intent)
                showBottomSheet = false
            },
            onBlockPrefix = {
                // Extract a reasonable prefix (country code + first few digits)
                val prefix = extractPrefix(selectedNumber!!)
                viewModel.addPrefix(prefix, "Blocked from call log")
                Toast.makeText(context, "Added prefix $prefix to blocklist", Toast.LENGTH_SHORT).show()
                showBottomSheet = false
            },
            onCopyNumber = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Phone Number", selectedNumber)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Number copied", Toast.LENGTH_SHORT).show()
                showBottomSheet = false
            }
        )
    }
}

/**
 * Extract a prefix from a phone number for blocking
 * For international numbers: country code + area code (first 4-6 digits)
 * For local numbers: first 3-4 digits
 */
private fun extractPrefix(number: String): String {
    val cleaned = number.filter { it.isDigit() || it == '+' }
    return when {
        cleaned.startsWith("+") -> {
            // International: take country code + 2-3 more digits
            cleaned.take(minOf(6, cleaned.length))
        }
        cleaned.length >= 4 -> cleaned.take(4)
        else -> cleaned
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SystemCallLogTab(
    calls: List<SystemCallEntry>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onCallClick: (String) -> Unit,
    onCallLongClick: (String, String?) -> Unit
) {
    val context = LocalContext.current
    val hasPermission = remember {
        context.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (calls.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Call History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (!hasPermission) {
                        "Call log permission not granted. Please grant permission in app settings."
                    } else {
                        "Your call history will appear here.\nPull down to refresh."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh")
                }
            }
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
                    SystemCallCard(
                        call = call,
                        onClick = { onCallClick(call.number) },
                        onLongClick = { onCallLongClick(call.number, call.contactName) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedCallsTab(
    calls: List<com.blockforge.data.database.BlockedCall>,
    onDelete: (com.blockforge.data.database.BlockedCall) -> Unit,
    onCallClick: (String) -> Unit,
    onCallLongClick: (String, String?) -> Unit
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
                    onClick = { onCallClick(call.phoneNumber) },
                    onLongClick = { onCallLongClick(call.phoneNumber, call.contactName) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

/**
 * Get icon, color, and label for a call type - no object allocation
 */
private fun getCallTypeIcon(type: CallType): ImageVector = when (type) {
    CallType.INCOMING -> Icons.Default.CallReceived
    CallType.OUTGOING -> Icons.Default.CallMade
    CallType.MISSED -> Icons.Default.CallMissed
    CallType.REJECTED -> Icons.Default.CallEnd
    CallType.BLOCKED -> Icons.Default.PhoneDisabled
    CallType.UNKNOWN -> Icons.Default.Phone
}

private fun getCallTypeColor(type: CallType): Color = when (type) {
    CallType.INCOMING -> ColorGreen
    CallType.OUTGOING -> ColorBlue
    CallType.MISSED -> ColorOrange
    CallType.REJECTED -> StatusInactive
    CallType.BLOCKED -> StatusInactive
    CallType.UNKNOWN -> Color.Gray
}

private fun getCallTypeLabel(type: CallType): String = when (type) {
    CallType.INCOMING -> "Incoming"
    CallType.OUTGOING -> "Outgoing"
    CallType.MISSED -> "Missed"
    CallType.REJECTED -> "Rejected"
    CallType.BLOCKED -> "Blocked by app"
    CallType.UNKNOWN -> "Unknown"
}

/**
 * Card for displaying a system call log entry
 * Optimized: no object allocation during composition
 * Tap to call, long press for options
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SystemCallCard(
    call: SystemCallEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val icon = getCallTypeIcon(call.type)
    val iconColor = getCallTypeColor(call.type)
    val typeLabel = getCallTypeLabel(call.type)
    val formattedDate = remember(call.date) { dateFormat.format(Date(call.date)) }
    val formattedDuration = remember(call.duration) { if (call.duration > 0) formatDuration(call.duration) else null }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
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
                    formattedDuration?.let { duration ->
                        Text(
                            text = duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Time
            Text(
                text = formattedDate,
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

/**
 * Bottom sheet with call actions
 * Following Material 3 guidelines and best UX practices
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallActionBottomSheet(
    sheetState: SheetState,
    phoneNumber: String,
    contactName: String?,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onSendSms: () -> Unit,
    onBlockPrefix: () -> Unit,
    onCopyNumber: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ) {}
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header with number info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (contactName != null) {
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = phoneNumber,
                    style = if (contactName != null)
                        MaterialTheme.typography.bodyMedium
                    else
                        MaterialTheme.typography.titleLarge,
                    fontWeight = if (contactName != null) FontWeight.Normal else FontWeight.Bold,
                    color = if (contactName != null)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

            // Action buttons
            ActionItem(
                icon = Icons.Default.Call,
                iconColor = ColorGreen,
                text = "Call",
                onClick = onCall
            )

            ActionItem(
                icon = Icons.Default.Message,
                iconColor = ColorBlue,
                text = "Send SMS",
                onClick = onSendSms
            )

            ActionItem(
                icon = Icons.Default.Block,
                iconColor = StatusInactive,
                text = "Block similar numbers",
                subtitle = "Add prefix to blocklist",
                onClick = onBlockPrefix
            )

            ActionItem(
                icon = Icons.Default.ContentCopy,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = "Copy number",
                onClick = onCopyNumber
            )
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    iconColor: Color,
    text: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
