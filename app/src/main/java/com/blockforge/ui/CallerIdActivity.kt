package com.blockforge.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.LocalPhone
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blockforge.ui.theme.BlockForgeTheme
import com.blockforge.ui.theme.StatusInactive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

private const val AUTO_DISMISS_DELAY = 8000L

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make activity appear over lock screen (minSdk 29 >= O_MR1, so always use new API)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

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
        // Auto-dismiss after delay
        delay(AUTO_DISMISS_DELAY)
        onDismiss()
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

                    // Spam warning with score
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
                                    text = if (spamScore > 0) "Spam Risk: $spamScore%" else "Likely Spam",
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
