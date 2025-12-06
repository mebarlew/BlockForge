package com.blockforge.ui.screens

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.*

/**
 * Permissions setup screen with permission priming
 * Following best practices: explain value before asking, don't overwhelm
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Runtime permissions
    val phoneStatePermission = rememberPermissionState(Manifest.permission.READ_PHONE_STATE)
    val callLogPermission = rememberPermissionState(Manifest.permission.READ_CALL_LOG)
    val contactsPermission = rememberPermissionState(Manifest.permission.READ_CONTACTS)
    val callPhonePermission = rememberPermissionState(Manifest.permission.CALL_PHONE)

    // Special permissions
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasCallScreeningRole by remember { mutableStateOf(checkCallScreeningRole(context)) }

    // Launcher for Call Screening Role request
    val callScreeningRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        hasCallScreeningRole = checkCallScreeningRole(context)
    }

    // Refresh permissions when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                hasCallScreeningRole = checkCallScreeningRole(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Count granted permissions
    val grantedCount = listOf(
        phoneStatePermission.status.isGranted,
        callLogPermission.status.isGranted,
        contactsPermission.status.isGranted,
        callPhonePermission.status.isGranted,
        hasOverlayPermission,
        hasCallScreeningRole
    ).count { it }

    val allGranted = grantedCount == 6

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Set Up BlockForge",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "A few permissions are needed to protect you from spam calls",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress indicator
                    LinearProgressIndicator(
                        progress = { grantedCount / 6f },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$grantedCount of 6 permissions granted",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Privacy note
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Column {
                        Text(
                            text = "Your Privacy Matters",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "All data stays on your device. No tracking, no analytics, no data collection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Permission cards with value explanations
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CRITICAL: Call Screening Role - most important, show first
                PermissionPrimingCard(
                    title = "Call Screening",
                    benefit = "Block spam calls before they ring",
                    description = "This is the core permission that enables call blocking. Without it, BlockForge cannot intercept incoming calls.",
                    icon = Icons.Default.Shield,
                    isGranted = hasCallScreeningRole,
                    isCritical = true,
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                            callScreeningRoleLauncher.launch(intent)
                        }
                    }
                )

                PermissionPrimingCard(
                    title = "Phone State",
                    benefit = "Detect when calls come in",
                    description = "Allows BlockForge to know when you're receiving a call so it can check if it should be blocked.",
                    icon = Icons.Default.Phone,
                    isGranted = phoneStatePermission.status.isGranted,
                    onRequest = { phoneStatePermission.launchPermissionRequest() }
                )

                PermissionPrimingCard(
                    title = "Call Log",
                    benefit = "View your call history in-app",
                    description = "See your recent calls and blocked call history all in one place.",
                    icon = Icons.Default.History,
                    isGranted = callLogPermission.status.isGranted,
                    onRequest = { callLogPermission.launchPermissionRequest() }
                )

                PermissionPrimingCard(
                    title = "Contacts",
                    benefit = "Allow calls from people you know",
                    description = "When 'Block Unknown' is enabled, calls from your contacts will still come through.",
                    icon = Icons.Default.Contacts,
                    isGranted = contactsPermission.status.isGranted,
                    onRequest = { contactsPermission.launchPermissionRequest() }
                )

                PermissionPrimingCard(
                    title = "Make Calls",
                    benefit = "Call directly from the app",
                    description = "Use the built-in dialer to make calls. Tap any number in your call history to call back.",
                    icon = Icons.Default.Call,
                    isGranted = callPhonePermission.status.isGranted,
                    onRequest = { callPhonePermission.launchPermissionRequest() }
                )

                PermissionPrimingCard(
                    title = "Display Over Apps",
                    benefit = "See caller ID on incoming calls",
                    description = "Shows caller information as an overlay when someone calls, even if you're using another app.",
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue button
            AnimatedVisibility(
                visible = allGranted,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All Set! Continue to App",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Skip note for non-critical permissions
            if (!allGranted && hasCallScreeningRole) {
                Text(
                    text = "Core permissions granted. Other permissions are optional but recommended for the best experience.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )

                OutlinedButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Continue Anyway")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Permission card with value priming
 * Shows WHY the permission is needed before asking
 */
@Composable
private fun PermissionPrimingCard(
    title: String,
    benefit: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    isCritical: Boolean = false,
    onRequest: () -> Unit
) {
    val containerColor = when {
        isGranted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        isCritical -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon with status indicator
                Box {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Status badge
                    if (isGranted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Granted",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Title with critical badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isCritical && !isGranted) {
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Required",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Benefit (the VALUE proposition)
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isGranted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Grant button or checkmark
                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Granted",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    FilledTonalButton(
                        onClick = onRequest,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isCritical)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    ) {
                        Text(
                            text = "Allow",
                            color = if (isCritical)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Detailed explanation (shown when not granted)
            AnimatedVisibility(visible = !isGranted) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, start = 60.dp)
                )
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
