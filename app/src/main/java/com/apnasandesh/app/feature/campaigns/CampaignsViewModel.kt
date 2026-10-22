package com.apnasandesh.app.feature.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apnasandesh.app.core.database.BusinessProfileEntity
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.CampaignEntity
import com.apnasandesh.app.core.database.CampaignRecipientEntity
import com.apnasandesh.app.core.database.CustomerDao
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.database.CustomerGroupEntity
import com.apnasandesh.app.core.database.GroupDao
import com.apnasandesh.app.core.database.MessageTemplateEntity
import com.apnasandesh.app.core.database.ProfileAndSettingsDao
import com.apnasandesh.app.core.database.TemplateDao
import com.apnasandesh.app.data.campaign.CampaignRepository
import com.apnasandesh.app.domain.transport.TransportPolicyGate
import com.apnasandesh.app.domain.transport.TransportSubmissionOutcome
import com.apnasandesh.app.domain.validation.SmsSegmentCalculator
import com.apnasandesh.app.domain.validation.TemplateRenderer
import com.apnasandesh.app.domain.validation.TraiComplianceAdvisory
import com.apnasandesh.app.domain.validation.TraiDltComplianceAdvisor
import com.apnasandesh.app.domain.campaign.AutomaticCampaignSendingEngine
import com.apnasandesh.app.domain.campaign.LiveCampaignEngineState
import android.content.Context
import com.apnasandesh.app.domain.campaign.CampaignSendingForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class CampaignsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val campaignRepository: CampaignRepository,
    private val customerDao: CustomerDao,
    private val groupDao: GroupDao,
    private val templateDao: TemplateDao,
    private val profileDao: ProfileAndSettingsDao,
    val campaignEngine: AutomaticCampaignSendingEngine,
    val segmentCalculator: SmsSegmentCalculator,
    val templateRenderer: TemplateRenderer,
    val traiAdvisor: TraiDltComplianceAdvisor,
    val policyGate: TransportPolicyGate
) : ViewModel() {

    val businessProfile = profileDao.observeBusinessProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSettings = profileDao.observeAppSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val templates = templateDao.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers = customerDao.observeAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups = groupDao.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSimSubscriptions = MutableStateFlow<List<com.apnasandesh.app.core.model.SimSubscriptionInfo>>(emptyList())
    val activeSimSubscriptions: StateFlow<List<com.apnasandesh.app.core.model.SimSubscriptionInfo>> = _activeSimSubscriptions.asStateFlow()

    init {
        refreshSims()
    }

    fun refreshSims() {
        _activeSimSubscriptions.value = policyGate.getActiveSimSubscriptions()
    }

    private val _activeCampaignId = MutableStateFlow<String?>(null)
    val activeCampaignId: StateFlow<String?> = _activeCampaignId.asStateFlow()

    val activeRecipients: StateFlow<List<CampaignRecipientEntity>> = _activeCampaignId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else campaignRepository.observeCampaignRecipients(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val engineState: StateFlow<LiveCampaignEngineState?> = campaignEngine.liveEngineState

    private val _currentRecipientIndex = MutableStateFlow(0)
    val currentRecipientIndex: StateFlow<Int> = _currentRecipientIndex.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun dismissMessage() {
        _statusMessage.value = null
        campaignEngine.dismissStatusMessage()
    }

    fun selectCampaign(id: String) {
        _activeCampaignId.value = id
        _currentRecipientIndex.value = 0
        viewModelScope.launch {
            campaignEngine.syncCampaignState(id)
        }
    }

    fun updateRecipientMessage(recipientId: String, newMessage: String) {
        viewModelScope.launch {
            val res = campaignRepository.updateSingleRecipientMessage(recipientId, newMessage)
            if (res.isFailure) {
                _statusMessage.value = res.exceptionOrNull()?.message ?: "Failed to update recipient message"
            }
        }
    }

    fun stageCampaign(
        name: String,
        templateId: String?,
        rawMessageTemplate: String,
        appendSignature: Boolean,
        isPromotional: Boolean,
        transportType: String,
        selectedSubscriptionId: Int?,
        selectedCustomerIds: Set<String>?,
        sendingIntervalSeconds: Int = 3,
        scheduledAtMillis: Long? = null,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val res = campaignRepository.createOrUpdateValidatedCampaignDraft(
                existingCampaignId = null,
                name = name,
                templateId = templateId,
                rawMessageTemplate = rawMessageTemplate,
                appendSignature = appendSignature,
                isPromotional = isPromotional,
                transportType = transportType,
                selectedSubscriptionId = selectedSubscriptionId,
                selectedCustomerIds = selectedCustomerIds,
                sendingIntervalSeconds = sendingIntervalSeconds,
                scheduledAtMillis = scheduledAtMillis
            )
            res.fold(
                onSuccess = { campaign ->
                    _activeCampaignId.value = campaign.id
                    _currentRecipientIndex.value = 0
                    campaignEngine.syncCampaignState(campaign.id)
                    onSuccess(campaign.id)
                },
                onFailure = { error ->
                    _statusMessage.value = error.message ?: "Failed to stage campaign"
                }
            )
        }
    }

    fun startSendingSession(campaignId: String, intervalSeconds: Int? = null) {
        val interval = intervalSeconds ?: 3
        try {
            CampaignSendingForegroundService.startService(context, campaignId, interval)
        } catch (_: Exception) {
            campaignEngine.startCampaign(campaignId, interval)
        }
    }

    fun pauseCampaign(campaignId: String) {
        campaignEngine.pauseCampaign(campaignId)
    }

    fun resumeCampaign(campaignId: String) {
        campaignEngine.resumeCampaign(campaignId)
    }

    fun stopCampaign(campaignId: String) {
        try {
            CampaignSendingForegroundService.stopService(context)
        } catch (_: Exception) {}
        campaignEngine.stopCampaign(campaignId)
    }

    fun retryFailedRecipients(campaignId: String) {
        campaignEngine.retryFailedRecipients(campaignId)
    }

    fun scheduleCampaignWork(campaignId: String, scheduledAtMillis: Long) {
        viewModelScope.launch {
            val delayMillis = (scheduledAtMillis - System.currentTimeMillis()).coerceAtLeast(0)
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.apnasandesh.app.data.campaign.ScheduledCampaignExecutionWorker>()
                .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setInputData(androidx.work.workDataOf(com.apnasandesh.app.data.campaign.ScheduledCampaignExecutionWorker.KEY_CAMPAIGN_ID to campaignId))
                .build()
            androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                "scheduled_campaign_$campaignId",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
            campaignRepository.markCampaignScheduled(campaignId, scheduledAtMillis)
            _statusMessage.value = "Campaign scheduled successfully."
        }
    }

    fun dispatchCurrentRecipient(recipient: CampaignRecipientEntity) {
        viewModelScope.launch {
            val outcome = campaignRepository.dispatchSingleRecipient(recipient.campaignId, recipient.id)
            when (outcome) {
                is TransportSubmissionOutcome.HandedOffToComposer -> {
                    _statusMessage.value = "Opened in SMS app. Confirm message and return to mark as sent."
                }
                is TransportSubmissionOutcome.SubmittedToSimRadio -> {
                    _statusMessage.value = "Submitted to SIM radio. Awaiting delivery reports."
                }
                is TransportSubmissionOutcome.BlockedByPolicyOrPermission -> {
                    _statusMessage.value = outcome.reason
                }
                is TransportSubmissionOutcome.FailedImmediately -> {
                    _statusMessage.value = "Failed: ${outcome.diagnostic}"
                }
            }
        }
    }

    fun markCurrentRecipientSent(recipient: CampaignRecipientEntity) {
        viewModelScope.launch {
            campaignRepository.markRecipientComposerDone(recipient.id, recipient.campaignId)
            advanceToNextRecipient()
        }
    }

    fun markCurrentRecipientSkipped(recipient: CampaignRecipientEntity) {
        viewModelScope.launch {
            campaignRepository.markRecipientComposerSkipped(recipient.id, recipient.campaignId)
            advanceToNextRecipient()
        }
    }

    fun advanceToNextRecipient() {
        val next = _currentRecipientIndex.value + 1
        if (next < activeRecipients.value.size) {
            _currentRecipientIndex.value = next
        }
    }

    fun cancelCampaign(campaignId: String) {
        viewModelScope.launch {
            campaignRepository.cancelCampaign(campaignId)
            _statusMessage.value = "Campaign cancelled."
        }
    }

    fun duplicateCampaign(campaignId: String, onDuplicated: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val result = campaignRepository.duplicateCampaign(campaignId)
            result.onSuccess { newCamp ->
                _statusMessage.value = "Draft campaign duplicated successfully."
                onDuplicated?.invoke(newCamp.id)
            }.onFailure { err ->
                _statusMessage.value = "Failed to duplicate: ${err.message}"
            }
        }
    }

    fun deleteCampaign(campaignId: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            val result = campaignRepository.deleteCampaign(campaignId)
            result.onSuccess {
                _statusMessage.value = "Campaign deleted."
                if (_activeCampaignId.value == campaignId) {
                    _activeCampaignId.value = null
                }
                onDeleted?.invoke()
            }.onFailure { err ->
                _statusMessage.value = "Failed to delete: ${err.message}"
            }
        }
    }
}
