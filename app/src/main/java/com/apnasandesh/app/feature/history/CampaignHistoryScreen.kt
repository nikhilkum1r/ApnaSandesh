package com.apnasandesh.app.feature.history

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.CampaignEntity
import com.apnasandesh.app.core.database.CampaignRecipientEntity
import com.apnasandesh.app.core.database.SendAttemptEntity
import com.apnasandesh.app.core.ui.theme.StatusError
import com.apnasandesh.app.core.ui.theme.StatusSuccess
import com.apnasandesh.app.R
import com.apnasandesh.app.data.campaign.CampaignRepository
import com.apnasandesh.app.feature.common.CustomerAvatar
import com.apnasandesh.app.feature.common.EmptyStateView
import com.apnasandesh.app.feature.common.StatusBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class CampaignHistoryViewModel @Inject constructor(
    private val campaignRepository: CampaignRepository,
    private val campaignDao: CampaignDao
) : ViewModel() {

    val campaigns = campaignRepository.observeCampaigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCampaignId = MutableStateFlow<String?>(null)
    val selectedCampaignId: StateFlow<String?> = _selectedCampaignId.asStateFlow()

    val selectedCampaignRecipients: StateFlow<List<CampaignRecipientEntity>> = _selectedCampaignId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else campaignRepository.observeCampaignRecipients(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCampaignSendAttempts: StateFlow<List<SendAttemptEntity>> = _selectedCampaignId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else campaignRepository.observeSendAttempts(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiNotice = MutableStateFlow<String?>(null)
    val uiNotice: StateFlow<String?> = _uiNotice.asStateFlow()

    fun dismissNotice() {
        _uiNotice.value = null
    }

    fun selectCampaign(id: String?) {
        _selectedCampaignId.value = id
    }

    fun duplicateCampaign(campaignId: String, onDuplicated: (String) -> Unit) {
        viewModelScope.launch {
            val res = campaignRepository.duplicateCampaignAsDraft(campaignId)
            res.fold(
                onSuccess = { newId ->
                    _uiNotice.value = "Campaign duplicated as a new draft."
                    onDuplicated(newId)
                },
                onFailure = { error ->
                    _uiNotice.value = "Duplicate failed: ${error.message}"
                }
            )
        }
    }

    fun reconcileInterruptedSession(campaignId: String) {
        viewModelScope.launch {
            val count = campaignDao.reconcileStaleSubmittedRecipients(System.currentTimeMillis())
            _uiNotice.value = "Reconciled $count in-flight message(s) into UNKNOWN state for safe review."
        }
    }

    fun exportReport(campaignId: String, uri: Uri) {
        viewModelScope.launch {
            val res = campaignRepository.exportCampaignReportCsv(campaignId, uri)
            res.fold(
                onSuccess = { count ->
                    _uiNotice.value = "Exported recipient delivery audit report ($count rows) to CSV."
                },
                onFailure = { error ->
                    _uiNotice.value = "Report export failed: ${error.message}"
                }
            )
        }
    }
}

/**
 * Screen 12: Campaign History & Recipient Outcomes Detail Screen
 */
@Composable
fun CampaignHistoryScreen(
    viewModel: CampaignHistoryViewModel = hiltViewModel(),
    onNavigateToSending: (String) -> Unit
) {
    val campaigns by viewModel.campaigns.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedCampaignId.collectAsStateWithLifecycle()
    val recipients by viewModel.selectedCampaignRecipients.collectAsStateWithLifecycle()
    val sendAttempts by viewModel.selectedCampaignSendAttempts.collectAsStateWithLifecycle()
    val notice by viewModel.uiNotice.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val id = selectedId
        if (uri != null && id != null) {
            viewModel.exportReport(id, uri)
        }
    }

    val selectedCampaign = remember(campaigns, selectedId) {
        campaigns.firstOrNull { it.id == selectedId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (selectedCampaign == null) {
            val cCount = campaigns.size
            val cPlural = if (cCount == 1) "campaign" else "campaigns"
            Text(
                text = "$cCount $cPlural recorded",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Notification Banner
        notice?.let { msg ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(msg, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { viewModel.dismissNotice() }) { Text("OK") }
                }
            }
        }

        if (selectedCampaign != null) {
            CampaignDetailPane(
                campaign = selectedCampaign,
                recipients = recipients,
                sendAttempts = sendAttempts,
                onBack = { viewModel.selectCampaign(null) },
                onExportReport = { exportLauncher.launch("campaign_${selectedCampaign.name.replace(" ", "_")}_report.csv") },
                onDuplicate = { viewModel.duplicateCampaign(selectedCampaign.id) { viewModel.selectCampaign(it) } },
                onResumeSending = { onNavigateToSending(selectedCampaign.id) },
                onReconcile = { viewModel.reconcileInterruptedSession(selectedCampaign.id) }
            )
        } else {
            if (campaigns.isEmpty()) {
                EmptyStateView(
                    illustrationRes = R.drawable.ic_empty_history,
                    icon = Icons.Default.HistoryToggleOff,
                    title = "No Past Campaigns Recorded",
                    description = "When you compose and broadcast SMS campaigns, their delivery summaries and timestamps will be stored here.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(campaigns, key = { it.id }) { c ->
                        CampaignSummaryCard(
                            campaign = c,
                            onClick = { viewModel.selectCampaign(c.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CampaignSummaryCard(
    campaign: CampaignEntity,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(campaign.createdAtMillis) { dateFormat.format(Date(campaign.createdAtMillis)) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campaign.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = campaign.status)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${campaign.totalRecipients} Recipient${if (campaign.totalRecipients != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Mode: ${if (campaign.transportType.contains("COMPOSER", ignoreCase = true)) "SMS Composer" else "Direct SIM"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View Breakdown",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CampaignDetailPane(
    campaign: CampaignEntity,
    recipients: List<CampaignRecipientEntity>,
    sendAttempts: List<SendAttemptEntity>,
    onBack: () -> Unit,
    onExportReport: () -> Unit,
    onDuplicate: () -> Unit,
    onResumeSending: () -> Unit,
    onReconcile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val sentCount = remember(recipients) { recipients.count { it.state == "SENT" || it.state == "DELIVERED" } }
    val failedCount = remember(recipients) { recipients.count { it.state == "FAILED" } }
    val skippedCount = remember(recipients) { recipients.count { it.state == "CANCELLED" } }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Back header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = campaign.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Outcome: $sentCount sent • $skippedCount skipped • $failedCount failed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(status = campaign.status)
        }

        // Action Toolbar
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onExportReport,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("CSV Audit", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onDuplicate,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Duplicate", style = MaterialTheme.typography.labelMedium)
            }

            if (campaign.status in setOf("AWAITING_CONFIRMATION", "QUEUED", "IN_PROGRESS")) {
                Button(
                    onClick = onResumeSending,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resume", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Recipients (${recipients.size})", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Send Log (${sendAttempts.size})", fontWeight = FontWeight.SemiBold) }
            )
        }

        if (selectedTab == 0) {
            if (recipients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No recipient records found for this campaign.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(recipients, key = { it.id }) { r ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CustomerAvatar(name = r.recipientNameSnapshot, size = 32)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = r.recipientNameSnapshot,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = r.normalizedPhoneSnapshot,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    StatusBadge(status = r.state)
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = r.resolvedMessageBody,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                if (!r.lastErrorMessage.isNullOrBlank() && (r.state == "FAILED" || r.state == "CANCELLED")) {
                                    Text(
                                        text = "Error: ${r.lastErrorMessage}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusError
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (sendAttempts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No send attempts logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sendAttempts, key = { it.id }) { attempt ->
                        val dateFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Outcome: ${attempt.outcomeState}",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Mode: ${attempt.transportUsed} • Time: ${dateFormat.format(Date(attempt.startedAtMillis))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!attempt.errorDetail.isNullOrBlank()) {
                                        val isFailure = attempt.outcomeState == "FAILED" || attempt.outcomeState == "CANCELLED"
                                        Text(
                                            text = if (isFailure) "Error: ${attempt.errorDetail}" else attempt.errorDetail,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isFailure) StatusError else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                StatusBadge(status = attempt.outcomeState)
                            }
                        }
                    }
                }
            }
        }
    }
}
