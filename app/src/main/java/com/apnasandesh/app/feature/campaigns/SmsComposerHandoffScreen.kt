package com.apnasandesh.app.feature.campaigns

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnasandesh.app.core.database.CampaignRecipientEntity
import com.apnasandesh.app.core.ui.theme.StatusSuccess
import com.apnasandesh.app.feature.common.CustomerAvatar
import com.apnasandesh.app.feature.common.StatusBadge

/**
 * Screen 13: Dedicated SMS Composer Handoff Screen
 * Compliant with Google Play Store release policies.
 * Sequentially hands off personalized messages to Android's native SMS app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsComposerHandoffScreen(
    campaignTitle: String,
    recipients: List<CampaignRecipientEntity>,
    currentIndex: Int,
    onBack: () -> Unit,
    onMarkSent: (CampaignRecipientEntity) -> Unit,
    onSkipRecipient: (CampaignRecipientEntity) -> Unit,
    onEditRecipientMessage: ((String, String) -> Unit)? = null,
    onViewHistory: () -> Unit
) {
    val context = LocalContext.current
    var editingMessage by remember { mutableStateOf<String?>(null) }

    val pendingRecipients = remember(recipients) {
        recipients.filter { it.state in setOf("DRAFT", "VALIDATED", "AWAITING_CONFIRMATION", "HANDED_OFF_TO_MESSAGING_APP", "QUEUED") }
    }

    val sentCount = remember(recipients) {
        recipients.count { it.state in setOf("SENT", "DELIVERED") }
    }
    val skippedCount = remember(recipients) {
        recipients.count { it.state in setOf("CANCELLED", "FAILED") }
    }
    val totalCount = recipients.size
    val currentRecipient = pendingRecipients.firstOrNull()
    val isAllDone = currentRecipient == null && totalCount > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SMS Composer Assistant", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(campaignTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            // Google Play Transparency Disclosure Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Transparent SMS Dispatch",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ApnaSandesh pre-fills your phone's default SMS app with the customer's phone number and rendered message. Review and tap Send inside your SMS app, then return here to proceed to the next customer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Progress Bar
            val completedCount = sentCount + skippedCount
            val progressFloat = if (totalCount > 0) completedCount.toFloat() / totalCount else 1f

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress: $completedCount of $totalCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progressFloat * 100).toInt()}% Completed",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            if (isAllDone) {
                // Completed Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, StatusSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(StatusSuccess.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Campaign Completed!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$sentCount messages marked as sent, $skippedCount skipped. All delivery timestamps are recorded locally in your database.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onViewHistory,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View in Campaign History", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (currentRecipient != null) {
                // Active Recipient Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomerAvatar(name = currentRecipient.recipientNameSnapshot, size = 48)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentRecipient.recipientNameSnapshot,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentRecipient.normalizedPhoneSnapshot,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = currentRecipient.state)
                        }

                        // Message Preview Box
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Personalized Message",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${currentRecipient.characterCount} chars • ${currentRecipient.estimatedSegments} segment",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentRecipient.resolvedMessageBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        // Primary Action: Open SMS App
                        Button(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:${currentRecipient.normalizedPhoneSnapshot}")
                                    putExtra("sms_body", currentRecipient.resolvedMessageBody)
                                    putExtra(Intent.EXTRA_TEXT, currentRecipient.resolvedMessageBody)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                runCatching {
                                    context.startActivity(sendIntent)
                                }.onFailure {
                                    // Fallback to view intent
                                    val fallback = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("sms:${currentRecipient.normalizedPhoneSnapshot}")
                                        putExtra("sms_body", currentRecipient.resolvedMessageBody)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(fallback)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in SMS App", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        // Confirmation Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onMarkSent(currentRecipient) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess, contentColor = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark as Sent", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onSkipRecipient(currentRecipient) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Skip", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Upcoming Recipients Queue Preview
            if (pendingRecipients.size > 1) {
                Text(
                    text = "Upcoming in Queue (${pendingRecipients.size - 1})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pendingRecipients.drop(1).take(4).forEach { nextRecip ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomerAvatar(name = nextRecip.recipientNameSnapshot, size = 32)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nextRecip.recipientNameSnapshot, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                    Text(nextRecip.normalizedPhoneSnapshot, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Waiting", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
