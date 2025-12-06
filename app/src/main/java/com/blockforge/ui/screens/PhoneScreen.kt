package com.blockforge.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Phone dialer screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneScreen() {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phone number display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (phoneNumber.isEmpty()) "Enter number" else phoneNumber,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Light,
                        color = if (phoneNumber.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = if (phoneNumber.length > 12) 24.sp else 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dial pad
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1: 1, 2, 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DialButton("1", "", onClick = { phoneNumber += "1" })
                    DialButton("2", "ABC", onClick = { phoneNumber += "2" })
                    DialButton("3", "DEF", onClick = { phoneNumber += "3" })
                }

                // Row 2: 4, 5, 6
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DialButton("4", "GHI", onClick = { phoneNumber += "4" })
                    DialButton("5", "JKL", onClick = { phoneNumber += "5" })
                    DialButton("6", "MNO", onClick = { phoneNumber += "6" })
                }

                // Row 3: 7, 8, 9
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DialButton("7", "PQRS", onClick = { phoneNumber += "7" })
                    DialButton("8", "TUV", onClick = { phoneNumber += "8" })
                    DialButton("9", "WXYZ", onClick = { phoneNumber += "9" })
                }

                // Row 4: *, 0, #
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DialButton("*", "", onClick = { phoneNumber += "*" })
                    DialButton(
                        number = "0",
                        letters = "+",
                        onClick = { phoneNumber += "0" },
                        onLongClick = { phoneNumber += "+" }  // Long press adds +
                    )
                    DialButton("#", "", onClick = { phoneNumber += "#" })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Row 5: Call and Delete buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Empty space for balance
                    Box(modifier = Modifier.size(72.dp))

                    // Call button
                    FloatingActionButton(
                        onClick = {
                            if (phoneNumber.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_CALL).apply {
                                    data = Uri.parse("tel:$phoneNumber")
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = {
                            if (phoneNumber.isNotEmpty()) {
                                phoneNumber = phoneNumber.dropLast(1)
                            }
                        },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DialButton(
    number: String,
    letters: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
