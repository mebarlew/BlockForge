package com.blockforge.ui.screens

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockforge.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
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
            // Blocking Options Section
            Text(
                text = "Blocking Options",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingToggleItem(
                        title = "Block All Calls",
                        description = "Block all incoming calls regardless of other settings",
                        icon = Icons.Default.Block,
                        checked = settings.blockAll,
                        onCheckedChange = { viewModel.toggleBlockAll(it) },
                        iconTint = MaterialTheme.colorScheme.error
                    )

                    HorizontalDivider()

                    SettingToggleItem(
                        title = "Block Unknown Numbers",
                        description = "Block calls from numbers not in your contacts",
                        icon = Icons.Default.ContactPhone,
                        checked = settings.blockUnknown,
                        onCheckedChange = { viewModel.toggleBlockUnknown(it) },
                        enabled = !settings.blockAll
                    )

                    HorizontalDivider()

                    SettingToggleItem(
                        title = "Block International Calls",
                        description = "Block calls from countries other than ${settings.userCountryCode}",
                        icon = Icons.Default.Public,
                        checked = settings.blockInternational,
                        onCheckedChange = { viewModel.toggleBlockInternational(it) },
                        enabled = !settings.blockAll
                    )
                }
            }

            // Country Code Section
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Region Settings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your Country Code",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Used to identify international calls",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CountryCodeButton("+1", "US/CA", settings.userCountryCode == "+1") {
                            viewModel.updateCountryCode("+1")
                        }
                        CountryCodeButton("+44", "UK", settings.userCountryCode == "+44") {
                            viewModel.updateCountryCode("+44")
                        }
                        CountryCodeButton("+49", "DE", settings.userCountryCode == "+49") {
                            viewModel.updateCountryCode("+49")
                        }
                        CountryCodeButton("+33", "FR", settings.userCountryCode == "+33") {
                            viewModel.updateCountryCode("+33")
                        }
                    }
                }
            }

            // Info Card
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "These settings work alongside your blocked prefix list. When 'Block All' is enabled, all other settings are overridden.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Debug Info Section
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Debug Info",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            DebugInfoCard()
        }
    }
}

@Composable
private fun DebugInfoCard() {
    val context = LocalContext.current
    val hasCallScreeningRole = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
        } else {
            false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasCallScreeningRole)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Call Screening Role",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (hasCallScreeningRole)
                            "Granted - blocking will work"
                        else
                            "NOT GRANTED - blocking will NOT work!",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasCallScreeningRole)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
                Icon(
                    imageVector = if (hasCallScreeningRole) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (hasCallScreeningRole)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            if (!hasCallScreeningRole) {
                HorizontalDivider()
                Text(
                    text = "⚠️ Go to Permissions screen and grant 'Call Screening Role' to enable blocking!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider()

            Text(
                text = "Check Logs",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "To see detailed blocking logs:\n1. Connect device via USB\n2. Run: adb logcat | grep CallBlockingService\n3. Make a test call\n4. Check if logs appear",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) iconTint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun CountryCodeButton(
    code: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(code, style = MaterialTheme.typography.labelLarge)
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null
    )
}
