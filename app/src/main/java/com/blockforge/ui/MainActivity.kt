package com.blockforge.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.blockforge.ui.screens.BlocklistScreen
import com.blockforge.ui.screens.PermissionsScreen
import com.blockforge.ui.screens.SettingsScreen
import com.blockforge.ui.theme.BlockForgeTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for BlockForge
 * Entry point of the app
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BlockForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var allPermissionsGranted by remember { mutableStateOf(checkAllPermissions(context)) }
    var selectedScreen by remember { mutableIntStateOf(0) }

    // Show permissions screen if not all granted
    if (!allPermissionsGranted) {
        PermissionsScreen(
            onComplete = {
                allPermissionsGranted = checkAllPermissions(context)
            }
        )
    } else {
        // Main app with bottom navigation
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Block, contentDescription = null) },
                        label = { Text("Blocklist") },
                        selected = selectedScreen == 0,
                        onClick = { selectedScreen = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                        selected = selectedScreen == 1,
                        onClick = { selectedScreen = 1 }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedScreen) {
                    0 -> BlocklistScreen()
                    1 -> SettingsScreen()
                }
            }
        }
    }
}

private fun checkAllPermissions(context: Context): Boolean {
    val hasPhoneState = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED

    val hasCallLog = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CALL_LOG
    ) == PackageManager.PERMISSION_GRANTED

    val hasContacts = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED

    val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }

    val hasCallScreening = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
    } else {
        true
    }

    return hasPhoneState && hasCallLog && hasContacts && hasOverlay && hasCallScreening
}
