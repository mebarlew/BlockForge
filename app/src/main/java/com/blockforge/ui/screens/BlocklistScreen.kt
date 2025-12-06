package com.blockforge.ui.screens

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.blockforge.ui.components.*
import com.blockforge.ui.viewmodel.BlocklistViewModel

/**
 * Blocking rules screen - manage blocked prefixes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocklistScreen(
    viewModel: BlocklistViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val blockedPrefixes by viewModel.blockedPrefixes.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }

    // Check Call Screening Role status
    var hasCallScreeningRole by remember { mutableStateOf(checkCallScreeningRole(context)) }

    // Launcher for Call Screening Role request
    val callScreeningRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        hasCallScreeningRole = checkCallScreeningRole(context)
    }

    // Refresh when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCallScreeningRole = checkCallScreeningRole(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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

    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showAddSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add prefix"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Prefix", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Blocking Rules",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${blockedPrefixes.size} prefix${if (blockedPrefixes.size != 1) "es" else ""} blocked",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Warning banner if Call Screening Role not granted
            if (!hasCallScreeningRole) {
                CallScreeningWarningBanner(
                    onOpenSettings = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                            callScreeningRoleLauncher.launch(intent)
                        }
                    }
                )
            }

            // Prefix list
            PrefixListContent(
                prefixes = blockedPrefixes,
                onDelete = { viewModel.deletePrefix(it) },
                onAddClick = { showAddSheet = true }
            )
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
    onDelete: (com.blockforge.data.database.BlockedPrefix) -> Unit,
    onAddClick: () -> Unit
) {
    if (prefixes.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Block,
            title = "No Blocking Rules",
            subtitle = "Add phone number prefixes to block spam calls from specific area codes or countries",
            actionText = "Add Your First Prefix",
            onAction = onAddClick
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
 * Common prefix suggestions for quick blocking
 */
private data class PrefixSuggestion(
    val prefix: String,
    val label: String,
    val description: String
)

private val commonPrefixes = listOf(
    PrefixSuggestion("+1-800", "Toll-Free US", "US toll-free numbers"),
    PrefixSuggestion("+1-888", "Toll-Free US", "US toll-free numbers"),
    PrefixSuggestion("+1-877", "Toll-Free US", "US toll-free numbers"),
    PrefixSuggestion("+1-866", "Toll-Free US", "US toll-free numbers"),
    PrefixSuggestion("+44", "UK", "United Kingdom"),
    PrefixSuggestion("+91", "India", "India"),
    PrefixSuggestion("+234", "Nigeria", "Nigeria"),
    PrefixSuggestion("+92", "Pakistan", "Pakistan")
)

/**
 * Enhanced bottom sheet for adding a prefix with quick suggestions
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
    val haptic = LocalHapticFeedback.current

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
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add Blocking Rule",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Block calls starting with a prefix",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick suggestions
            Text(
                text = "Quick Add",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commonPrefixes) { suggestion ->
                    SuggestionChip(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            prefix = suggestion.prefix
                            description = suggestion.description
                        },
                        label = { Text(suggestion.prefix) },
                        icon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (prefix == suggestion.prefix)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom prefix input
            Text(
                text = "Custom Prefix",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = prefix,
                onValueChange = { prefix = it },
                label = { Text("Prefix") },
                placeholder = { Text("e.g., +48, 1-800, 555") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Note (optional)") },
                placeholder = { Text("e.g., Spam calls from...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        Icons.Default.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAdd(prefix, description.ifBlank { null })
                    },
                    enabled = prefix.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Block Prefix", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Warning banner shown when Call Screening Role is not granted
 * This is CRITICAL - without this permission, the app cannot block calls!
 */
@Composable
private fun CallScreeningWarningBanner(
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Call Blocking Disabled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "BlockForge is not set as your call screening app. Calls will NOT be blocked!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Enable Call Blocking",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Check if the app has the Call Screening role
 */
private fun checkCallScreeningRole(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
    } else {
        // Before Android 10, call screening works differently
        true
    }
}

