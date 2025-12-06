package com.blockforge.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockforge.ui.components.*
import com.blockforge.ui.viewmodel.BlocklistViewModel

/**
 * Main screen with modern tabbed interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocklistScreen(
    viewModel: BlocklistViewModel = hiltViewModel()
) {
    val blockedPrefixes by viewModel.blockedPrefixes.collectAsState()
    val recentBlockedCalls by viewModel.recentBlockedCalls.collectAsState()
    val totalBlocked by viewModel.totalBlockedCount.collectAsState()
    val todayBlocked by viewModel.todayBlockedCount.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTab == 0,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add prefix"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Prefix")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hero header with stats
            HeroHeader(
                totalBlocked = totalBlocked,
                todayBlocked = todayBlocked,
                prefixCount = blockedPrefixes.size
            )

            // Tab row
            BlockForgeTabRow(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // Content based on selected tab
            when (selectedTab) {
                0 -> PrefixListContent(
                    prefixes = blockedPrefixes,
                    onDelete = { viewModel.deletePrefix(it) }
                )
                1 -> CallLogContent(
                    calls = recentBlockedCalls,
                    onDelete = { viewModel.deleteBlockedCall(it) },
                    onClearAll = { viewModel.clearBlockedCallLogs() }
                )
            }
        }

        // Bottom sheet for adding prefix
        if (showAddSheet) {
            AddPrefixBottomSheet(
                sheetState = sheetState,
                onDismiss = { showAddSheet = false },
                onAdd = { prefix, description ->
                    viewModel.addPrefix(prefix, description)
                    showAddSheet = false
                }
            )
        }
    }
}

/**
 * Prefix list tab content
 */
@Composable
private fun PrefixListContent(
    prefixes: List<com.blockforge.data.database.BlockedPrefix>,
    onDelete: (com.blockforge.data.database.BlockedPrefix) -> Unit
) {
    if (prefixes.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Block,
            title = "No Blocked Prefixes",
            subtitle = "Tap the button below to add your first prefix"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = prefixes,
                key = { it.id }
            ) { prefix ->
                PrefixCard(
                    prefix = prefix,
                    onDelete = { onDelete(prefix) },
                    modifier = Modifier.animateItem()
                )
            }
            // Add some space at the bottom for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * Call log tab content
 */
@Composable
private fun CallLogContent(
    calls: List<com.blockforge.data.database.BlockedCall>,
    onDelete: (com.blockforge.data.database.BlockedCall) -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (calls.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClearAll) {
                    Text(
                        text = "Clear All",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (calls.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
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
}

/**
 * Modern bottom sheet for adding a prefix
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPrefixBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAdd: (String, String?) -> Unit
) {
    var prefix by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

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
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Add Blocked Prefix",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "All calls starting with this prefix will be blocked",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = prefix,
                onValueChange = { prefix = it },
                label = { Text("Prefix") },
                placeholder = { Text("e.g., +48, 1-800, 555") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                placeholder = { Text("e.g., Polish spam calls") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onAdd(prefix, description.ifBlank { null }) },
                    enabled = prefix.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Add Prefix")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
