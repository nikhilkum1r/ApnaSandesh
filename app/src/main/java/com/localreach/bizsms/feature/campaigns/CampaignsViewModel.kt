package com.localreach.bizsms.feature.campaigns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localreach.bizsms.core.database.BusinessProfileEntity
import com.localreach.bizsms.core.database.CampaignDao
import com.localreach.bizsms.core.database.CampaignEntity
import com.localreach.bizsms.core.database.CampaignRecipientEntity
import com.localreach.bizsms.core.database.CustomerDao
import com.localreach.bizsms.core.database.CustomerEntity
import com.localreach.bizsms.core.database.CustomerGroupEntity
import com.localreach.bizsms.core.database.GroupDao
import com.localreach.bizsms.core.database.MessageTemplateEntity
import com.localreach.bizsms.core.database.ProfileAndSettingsDao
import com.localreach.bizsms.core.database.TemplateDao
import com.localreach.bizsms.data.campaign.CampaignRepository
import com.localreach.bizsms.domain.transport.TransportPolicyGate
import com.localreach.bizsms.domain.transport.TransportSubmissionOutcome
import com.localreach.bizsms.domain.validation.SmsSegmentCalculator
import com.localreach.bizsms.domain.validation.TemplateRenderer
import com.localreach.bizsms.domain.validation.TraiComplianceAdvisory
import com.localreach.bizsms.domain.validation.TraiDltComplianceAdvisor
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

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    private val campaignRepository: CampaignRepository,
    private val customerDao: CustomerDao,
    private val groupDao: GroupDao,
    private val templateDao: TemplateDao,
    private val profileDao: ProfileAndSettingsDao,
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

    private val _activeCampaignId = MutableStateFlow<String?>(null)
    val activeCampaignId: StateFlow<String?> = _activeCampaignId.asStateFlow()

    val activeRecipients: StateFlow<List<CampaignRecipientEntity>> = _activeCampaignId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else campaignRepository.observeCampaignRecipients(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentRecipientIndex = MutableStateFlow(0)
    val currentRecipientIndex: StateFlow<Int> = _currentRecipientIndex.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun dismissMessage() {
        _statusMessage.value = null
    }

    fun selectCampaign(id: String) {
        _activeCampaignId.value = id
        _currentRecipientIndex.value = 0
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
                selectedCustomerIds = selectedCustomerIds
            )
            res.fold(
                onSuccess = { campaign ->
                    _activeCampaignId.value = campaign.id
                    _currentRecipientIndex.value = 0
                    onSuccess(campaign.id)
                },
                onFailure = { error ->
                    _statusMessage.value = error.message ?: "Failed to stage campaign"
                }
            )
        }
    }

    fun startSendingSession(campaignId: String) {
        viewModelScope.launch {
            campaignRepository.confirmCampaignSession(campaignId)
            _currentRecipientIndex.value = 0
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
}
