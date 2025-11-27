package com.blockforge.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blockforge.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * Transparent overlay activity showing caller ID information
 */
@AndroidEntryPoint
class CallerIdActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_COUNTRY_NAME = "country_name"
        const val EXTRA_CARRIER = "carrier"
        const val EXTRA_LINE_TYPE = "line_type"
        const val EXTRA_IS_SPAM = "is_spam"
        const val EXTRA_SPAM_SCORE = "spam_score"

        private const val AUTO_DISMISS_DELAY = 8000L // 8 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make activity appear over lock screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: "Unknown"
        val countryName = intent.getStringExtra(EXTRA_COUNTRY_NAME)
        val carrier = intent.getStringExtra(EXTRA_CARRIER)
        val lineType = intent.getStringExtra(EXTRA_LINE_TYPE)
        val isSpam = intent.getBooleanExtra(EXTRA_IS_SPAM, false)
        val spamScore = intent.getIntExtra(EXTRA_SPAM_SCORE, 0)

        setContent {
            BlockForgeTheme {
                CallerIdOverlay(
                    phoneNumber = phoneNumber,
                    countryName = countryName,
                    carrier = carrier,
                    lineType = lineType,
                    isSpam = isSpam,
                    spamScore = spamScore,
                    onDismiss = { finish() }
                )
            }
        }

        // Auto-dismiss after delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                finish()
            }
        }, AUTO_DISMISS_DELAY)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

@Composable
fun CallerIdOverlay(
    phoneNumber: String,
    countryName: String?,
    carrier: String?,
    lineType: String?,
    isSpam: Boolean,
    spamScore: Int,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Spam indicator or caller icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSpam) StatusInactive.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSpam) Icons.Default.Warning else Icons.Default.Phone,
                            contentDescription = null,
                            tint = if (isSpam) StatusInactive else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Spam warning
                    if (isSpam) {
                        Surface(
                            color = StatusInactive.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusInactive,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Likely Spam",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = StatusInactive,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Phone number
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (countryName != null) {
                            InfoChip(
                                icon = Icons.Default.Public,
                                text = countryName
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (lineType != null) {
                            InfoChip(
                                icon = getLineTypeIcon(lineType),
                                text = lineType
                            )
                        }
                    }

                    if (carrier != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoChip(
                            icon = Icons.Default.CellTower,
                            text = carrier
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dismiss button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getLineTypeIcon(lineType: String): ImageVector {
    return when (lineType.lowercase()) {
        "mobile" -> Icons.Default.Smartphone
        "landline" -> Icons.Default.Phone
        "voip" -> Icons.Default.Wifi
        "toll-free", "toll_free" -> Icons.Default.LocalPhone
        else -> Icons.Default.Phone
    }
}
