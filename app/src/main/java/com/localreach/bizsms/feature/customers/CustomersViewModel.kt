package com.localreach.bizsms.feature.customers

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localreach.bizsms.core.database.CustomerEntity
import com.localreach.bizsms.core.database.CustomerGroupEntity
import com.localreach.bizsms.core.database.GroupMembershipEntity
import com.localreach.bizsms.core.database.ProfileAndSettingsDao
import com.localreach.bizsms.data.customer.CustomerRepository
import com.localreach.bizsms.domain.validation.CsvImportSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val profileDao: ProfileAndSettingsDao
) : ViewModel() {

    val customers = customerRepository.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups = customerRepository.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppressedList = customerRepository.observeSuppressedNumbers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessProfile = profileDao.observeBusinessProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _csvPreview = MutableStateFlow<CsvImportSummary?>(null)
    val csvPreview: StateFlow<CsvImportSummary?> = _csvPreview.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun dismissMessage() {
        _uiMessage.value = null
    }

    fun addCustomer(
        fullName: String,
        rawPhone: String,
        tags: String,
        notes: String,
        consentStatus: String,
        consentSource: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val code = businessProfile.value?.defaultCountryCallingCode ?: "+91"
            val res = customerRepository.addOrUpdateCustomer(
                existingId = null,
                fullName = fullName,
                rawPhone = rawPhone,
                defaultCountryCode = code,
                tagsCsv = tags,
                notes = notes,
                consentStatus = consentStatus,
                consentSource = consentSource
            )
            res.fold(
                onSuccess = {
                    _uiMessage.value = "Customer ${it.fullName} saved (${it.normalizedPhoneE164})."
                    onSuccess()
                },
                onFailure = {
                    _uiMessage.value = "Validation Error: ${it.message}"
                }
            )
        }
    }

    fun updateCustomer(
        customer: CustomerEntity,
        fullName: String,
        rawPhone: String,
        tags: String,
        notes: String,
        consentStatus: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val code = businessProfile.value?.defaultCountryCallingCode ?: "+91"
            val res = customerRepository.addOrUpdateCustomer(
                existingId = customer.id,
                fullName = fullName,
                rawPhone = rawPhone,
                defaultCountryCode = code,
                tagsCsv = tags,
                notes = notes,
                consentStatus = consentStatus,
                consentSource = customer.consentSource
            )
            res.fold(
                onSuccess = {
                    _uiMessage.value = "Customer ${it.fullName} updated."
                    onSuccess()
                },
                onFailure = {
                    _uiMessage.value = "Update Error: ${it.message}"
                }
            )
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customerId)
            _uiMessage.value = "Customer deleted."
        }
    }

    fun recordOptOut(customer: CustomerEntity, reason: String) {
        viewModelScope.launch {
            customerRepository.recordOptOut(customer.id, customer.normalizedPhoneE164, reason)
            _uiMessage.value = "${customer.fullName} (${customer.normalizedPhoneE164}) permanently opted out and suppressed."
        }
    }

    fun importContact(uri: Uri) {
        viewModelScope.launch {
            val code = businessProfile.value?.defaultCountryCallingCode ?: "+91"
            val res = customerRepository.importFromSystemContactUri(uri, code)
            res.fold(
                onSuccess = {
                    _uiMessage.value = "Imported contact ${it.fullName} (${it.normalizedPhoneE164})."
                },
                onFailure = {
                    _uiMessage.value = "Contact Import: ${it.message}"
                }
            )
        }
    }

    fun previewCsv(uri: Uri) {
        viewModelScope.launch {
            val code = businessProfile.value?.defaultCountryCallingCode ?: "+91"
            val res = customerRepository.importCustomersFromCsvUri(uri, code)
            res.fold(
                onSuccess = { summary ->
                    _csvPreview.value = summary
                    _uiMessage.value = "CSV file parsed. ${summary.validCustomers.size} valid rows ready."
                },
                onFailure = {
                    _uiMessage.value = "CSV Parse Failed: ${it.message}"
                }
            )
        }
    }

    fun clearCsvPreview() {
        _csvPreview.value = null
    }

    fun exportCustomers(uri: Uri) {
        viewModelScope.launch {
            val res = customerRepository.exportCustomersToCsvUri(uri)
            res.fold(
                onSuccess = {
                    _uiMessage.value = "Exported $it customer records to CSV. Please store this file securely."
                },
                onFailure = {
                    _uiMessage.value = "Export failed: ${it.message}"
                }
            )
        }
    }

    fun createGroup(name: String, description: String) {
        viewModelScope.launch {
            customerRepository.createGroup(name, description)
            _uiMessage.value = "Group '$name' created."
        }
    }

    fun updateGroup(id: String, name: String, description: String) {
        viewModelScope.launch {
            customerRepository.updateGroup(id, name, description)
            _uiMessage.value = "Group updated."
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            customerRepository.deleteGroup(groupId)
            _uiMessage.value = "Group deleted."
        }
    }

    fun assignCustomerToGroup(groupId: String, customerId: String) {
        viewModelScope.launch {
            customerRepository.assignToGroup(groupId, customerId)
            _uiMessage.value = "Customer added to group."
        }
    }

    fun removeCustomerFromGroup(groupId: String, customerId: String) {
        viewModelScope.launch {
            customerRepository.removeFromGroup(groupId, customerId)
            _uiMessage.value = "Customer removed from group."
        }
    }
}
