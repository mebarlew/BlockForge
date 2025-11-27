package com.blockforge.ui.screens

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*

/**
 * Permissions setup screen with status indicators
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current

    // Runtime permissions
    val phoneStatePermission = rememberPermissionState(Manifest.permission.READ_PHONE_STATE)
    val callLogPermission = rememberPermissionState(Manifest.permission.READ_CALL_LOG)
    val contactsPermission = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    // Special permissions
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasCallScreeningRole by remember { mutableStateOf(checkCallScreeningRole(context)) }

    val allGranted = phoneStatePermission.status.isGranted &&
            callLogPermission.status.isGranted &&
            contactsPermission.status.isGranted &&
            hasOverlayPermission &&
            hasCallScreeningRole

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Permissions") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (allGranted) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue to App")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "BlockForge needs these permissions to work",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "All data stays on your device. No tracking or analytics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Permission cards
            PermissionCard(
                title = "Phone State",
                description = "Detect incoming calls",
                icon = Icons.Default.Phone,
                isGranted = phoneStatePermission.status.isGranted,
                onRequest = { phoneStatePermission.launchPermissionRequest() }
            )

            PermissionCard(
                title = "Call Log",
                description = "Log blocked calls for review",
                icon = Icons.Default.History,
                isGranted = callLogPermission.status.isGranted,
                onRequest = { callLogPermission.launchPermissionRequest() }
            )

            PermissionCard(
                title = "Contacts",
                description = "Block unknown numbers (not in contacts)",
                icon = Icons.Default.Contacts,
                isGranted = contactsPermission.status.isGranted,
                onRequest = { contactsPermission.launchPermissionRequest() }
            )

            PermissionCard(
                title = "Display Over Apps",
                description = "Show caller ID overlay",
                icon = Icons.Default.Layers,
                isGranted = hasOverlayPermission,
                onRequest = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            )

            PermissionCard(
                title = "Call Screening Role",
                description = "Required to block calls",
                icon = Icons.Default.Block,
                isGranted = hasCallScreeningRole,
                isRequired = true,
                onRequest = {
                    requestCallScreeningRole(context)
                }
            )

            // Refresh button
            if (!allGranted) {
                OutlinedButton(
                    onClick = {
                        hasOverlayPermission = Settings.canDrawOverlays(context)
                        hasCallScreeningRole = checkCallScreeningRole(context)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Status")
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean = true,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isRequired) {
                        Text(
                            text = "Required",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = onRequest,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Grant")
                }
            }
        }
    }
}

private fun checkCallScreeningRole(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    } else {
        false
    }
}

private fun requestCallScreeningRole(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        context.startActivity(intent)
    }
}
