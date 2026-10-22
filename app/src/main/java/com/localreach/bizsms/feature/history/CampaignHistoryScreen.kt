package com.localreach.bizsms.feature.history

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.localreach.bizsms.core.database.CampaignDao
import com.localreach.bizsms.core.database.CampaignEntity
import com.localreach.bizsms.core.database.CampaignRecipientEntity
import com.localreach.bizsms.core.database.SendAttemptEntity
import com.localreach.bizsms.data.campaign.CampaignRepository
import com.localreach.bizsms.feature.common.StatusBadge
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Campaign History & Logs", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        notice?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(msg, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { viewModel.dismissNotice() }) { Text("OK") }
                }
            }
        }

        if (selectedCampaign != null) {
            // Detailed View for Selected Campaign
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
            // List of All Campaigns
            if (campaigns.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No past campaigns recorded yet.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
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

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(campaign.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                StatusBadge(status = campaign.status)
            }
            Text("Date: $formattedDate • Total Recipients: ${campaign.totalRecipients}", style = MaterialTheme.typography.bodySmall)
            Text("Transport: ${campaign.transportType}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text(campaign.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onExportReport, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("CSV Report")
            }
            OutlinedButton(onClick = onDuplicate, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Duplicate")
            }
            if (campaign.status in setOf("AWAITING_CONFIRMATION", "QUEUED", "IN_PROGRESS")) {
                Button(onClick = onResumeSending, modifier = Modifier.weight(1f)) {
                    Text("Resume")
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Recipients (${recipients.size})") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Send Log (${sendAttempts.size})") })
        }

        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                items(recipients, key = { it.id }) { r ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(r.recipientNameSnapshot, fontWeight = FontWeight.Bold)
                                StatusBadge(status = r.state)
                            }
                            Text(r.normalizedPhoneSnapshot, style = MaterialTheme.typography.bodySmall)
                            Text(r.resolvedMessageBody, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!r.lastErrorMessage.isNullOrBlank()) {
                                Text(r.lastErrorMessage, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                items(sendAttempts, key = { it.id }) { attempt ->
                    val dateFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(attempt.outcomeState, fontWeight = FontWeight.SemiBold)
                                Text("Mode: ${attempt.transportUsed} • Time: ${dateFormat.format(Date(attempt.startedAtMillis))}", style = MaterialTheme.typography.labelSmall)
                                if (!attempt.errorDetail.isNullOrBlank()) {
                                    Text(attempt.errorDetail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
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
