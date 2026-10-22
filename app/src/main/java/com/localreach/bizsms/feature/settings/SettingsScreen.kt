package com.localreach.bizsms.feature.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import com.localreach.bizsms.core.database.AppSettingsEntity
import com.localreach.bizsms.core.database.AppPreferencesDataStore
import com.localreach.bizsms.core.database.BizSmsDatabase
import com.localreach.bizsms.core.database.ProfileAndSettingsDao
import com.localreach.bizsms.core.model.SimSubscriptionInfo
import com.localreach.bizsms.domain.transport.TransportPolicyGate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileDao: ProfileAndSettingsDao,
    private val preferencesDataStore: AppPreferencesDataStore,
    private val database: BizSmsDatabase,
    val policyGate: TransportPolicyGate
) : ViewModel() {

    val appSettings = profileDao.observeAppSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _activeSimSubscriptions = MutableStateFlow<List<SimSubscriptionInfo>>(emptyList())
    val activeSimSubscriptions: StateFlow<List<SimSubscriptionInfo>> = _activeSimSubscriptions.asStateFlow()

    init {
        refreshSims()
    }

    fun refreshSims() {
        _activeSimSubscriptions.value = policyGate.getActiveSimSubscriptions()
    }

    fun updateTransportMode(mode: String) {
        viewModelScope.launch {
            val current = profileDao.getAppSettings() ?: AppSettingsEntity()
            profileDao.upsertAppSettings(current.copy(preferredTransport = mode))
        }
    }

    fun acknowledgeDirectSimPolicy(acknowledged: Boolean) {
        viewModelScope.launch {
            val current = profileDao.getAppSettings() ?: AppSettingsEntity()
            profileDao.upsertAppSettings(current.copy(directSimPolicyAcknowledged = acknowledged))
            preferencesDataStore.setDirectSimPolicyAcknowledged(acknowledged)
        }
    }

    fun toggleDirectSimFeature(enabled: Boolean) {
        viewModelScope.launch {
            val current = profileDao.getAppSettings() ?: AppSettingsEntity()
            profileDao.upsertAppSettings(current.copy(directSimFeatureEnabled = enabled))
            preferencesDataStore.setDirectSimFeatureEnabled(enabled)
        }
    }

    fun selectSimSubscription(subscriptionId: Int?) {
        viewModelScope.launch {
            val current = profileDao.getAppSettings() ?: AppSettingsEntity()
            profileDao.upsertAppSettings(current.copy(selectedSimSubscriptionId = subscriptionId))
        }
    }

    fun toggleIndiaTraiMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = profileDao.getAppSettings() ?: AppSettingsEntity()
            profileDao.upsertAppSettings(current.copy(indiaTraiComplianceMode = enabled))
            preferencesDataStore.setIndiaTraiMode(enabled)
        }
    }

    fun updateThemeMode(theme: String) {
        viewModelScope.launch {
            val current = profileDao.getAppSettings() ?: AppSettingsEntity()
            profileDao.upsertAppSettings(current.copy(themeMode = theme))
            preferencesDataStore.setThemeMode(theme)
        }
    }

    fun wipeAllLocalData(onComplete: () -> Unit) {
        viewModelScope.launch {
            database.wipeAllLocalData()
            preferencesDataStore.clearAllPreferences()
            onComplete()
        }
    }
}

/**
 * Screen 13: Settings & Privacy Controls Screen
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val activeSims by viewModel.activeSimSubscriptions.collectAsStateWithLifecycle()

    var showDirectSimDisclosureDialog by remember { mutableStateOf(false) }
    var showTraiGuideDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var wipeSuccessSnackbar by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.refreshSims()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings & Privacy", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        // Profile Shortcut Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Business Profile & Signature", fontWeight = FontWeight.Bold)
                    Text("Edit business name, contact number, and signature", style = MaterialTheme.typography.bodySmall)
                }
                FilledTonalButton(onClick = onNavigateToProfile) {
                    Text("Edit")
                }
            }
        }

        // Transport Mode & Policy Gate
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("SMS Transport Engine", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Choose how messages are dispatched from this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings?.preferredTransport == "COMPOSER_HANDOFF",
                        onClick = { viewModel.updateTransportMode("COMPOSER_HANDOFF") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Android SMS Composer (Default & Google Play Baseline)", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Opens your default messaging app pre-filled. Requires zero sensitive permissions. You review and confirm every message.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings?.preferredTransport == "DIRECT_SIM",
                        onClick = {
                            if (settings?.directSimPolicyAcknowledged == true) {
                                viewModel.updateTransportMode("DIRECT_SIM")
                            } else {
                                showDirectSimDisclosureDialog = true
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Direct SIM Radio (Gated Enterprise / Sideload)", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Sends directly via SmsManager using your SIM card plan. Requires explicit policy acknowledgment and SEND_SMS permission.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (settings?.directSimPolicyAcknowledged == true) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Direct SIM Dispatch")
                        Switch(
                            checked = settings?.directSimFeatureEnabled == true,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.SEND_SMS,
                                            Manifest.permission.READ_PHONE_STATE
                                        )
                                    )
                                }
                                viewModel.toggleDirectSimFeature(enabled)
                            }
                        )
                    }

                    // Dual SIM Selector
                    Text("Dual-SIM Subscription Pinning", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    if (activeSims.isEmpty()) {
                        Text(
                            "Grant READ_PHONE_STATE permission to detect active dual-SIM slots, or using single SIM default.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        OutlinedButton(onClick = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE))
                        }) {
                            Text("Detect Installed SIM Cards")
                        }
                    } else {
                        activeSims.forEach { sim ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = settings?.selectedSimSubscriptionId == sim.subscriptionId,
                                    onClick = { viewModel.selectSimSubscription(sim.subscriptionId) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Slot ${sim.simSlotIndex + 1}: ${sim.carrierName} (${sim.displayName})")
                            }
                        }
                    }
                }
            }
        }

        // Regulatory & Compliance
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Telecom Regulatory Guidance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("India TRAI Compliance Mode")
                        Text("Enforces 100 SMS/day warning and 10 AM - 9 PM promotional window", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = settings?.indiaTraiComplianceMode != false,
                        onCheckedChange = { viewModel.toggleIndiaTraiMode(it) }
                    )
                }

                OutlinedButton(
                    onClick = { showTraiGuideDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View India TRAI / DLT Compliance Guide")
                }
            }
        }

        // Appearance
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Appearance & Theme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings?.themeMode == "SYSTEM",
                        onClick = { viewModel.updateThemeMode("SYSTEM") },
                        label = { Text("System") }
                    )
                    FilterChip(
                        selected = settings?.themeMode == "LIGHT",
                        onClick = { viewModel.updateThemeMode("LIGHT") },
                        label = { Text("Light") }
                    )
                    FilterChip(
                        selected = settings?.themeMode == "DARK",
                        onClick = { viewModel.updateThemeMode("DARK") },
                        label = { Text("Dark") }
                    )
                }
            }
        }

        // Privacy Policy & Local Data Sovereignty
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Privacy & Data Sovereignty", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "All customer phone numbers, names, and message records are kept in local SQLite storage with zero internet access. Android Cloud Backup is explicitly disabled to prevent customer data leaks.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(
                    onClick = { showPrivacyPolicyDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View In-App Privacy Commitment")
                }
            }
        }

        // Destructive Wipe
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Erase All Data", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text(
                    "Permanently delete all customer records, suppression lists, campaign histories, and profiles from this phone.",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = { showWipeConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete All Local Business Data")
                }
            }
        }

        if (wipeSuccessSnackbar) {
            Snackbar(action = { TextButton(onClick = { wipeSuccessSnackbar = false }) { Text("OK") } }) {
                Text("All local data successfully deleted.")
            }
        }
    }

    // Direct SIM Disclosure Modal
    if (showDirectSimDisclosureDialog) {
        AlertDialog(
            onDismissRequest = { showDirectSimDisclosureDialog = false },
            title = { Text("Direct SIM Policy & Quota Disclosure") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "• Google Play Policy Restriction: Google Play restricts the SEND_SMS permission strictly to default SMS handler apps or qualified enterprise sideloaded apps.",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("• Carrier Quota Responsibility: Standard personal consumer SIM plans in India have a strict regulatory quota of 100 SMS/day. High volume sending can lead to carrier rate surges or SIM disconnection.")
                    Text("• Non-Bypass Notice: This app does not bypass carrier spam filters or TRAI DLT registration.")
                    Text("Do you acknowledge these terms and wish to enable Direct SIM controls?")
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.acknowledgeDirectSimPolicy(true)
                    viewModel.updateTransportMode("DIRECT_SIM")
                    showDirectSimDisclosureDialog = false
                }) {
                    Text("I Acknowledge & Agree")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectSimDisclosureDialog = false }) { Text("Cancel") }
            }
        )
    }

    // TRAI Guide Modal
    if (showTraiGuideDialog) {
        AlertDialog(
            onDismissRequest = { showTraiGuideDialog = false },
            title = { Text("India TRAI (TCCCPR) Compliance Guide") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("1. Personal SIM Daily Limit:", fontWeight = FontWeight.Bold)
                    Text("Indian telecom operators (Jio, Airtel, Vi, BSNL) enforce a regulatory limit of 100 SMS per day per SIM card. Exceeding 100 messages triggers commercial tariff spikes or automated SIM suspension.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Permitted Commercial Hours:", fontWeight = FontWeight.Bold)
                    Text("Commercial & promotional messages are only permitted between 10:00 AM and 9:00 PM IST. Never message customers outside these hours.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("3. Opt-Out Suppression Requirement:", fontWeight = FontWeight.Bold)
                    Text("If any customer asks to stop receiving messages, you must record an opt-out immediately. This app automatically enforces permanent suppression.")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("4. DLT Enterprise Gateway Alternative:", fontWeight = FontWeight.Bold)
                    Text("If your business requires high-volume bulk broadcasting (e.g. 500+ messages), do not use a personal SIM. Register a DLT Principal Entity header and use an authorized commercial telecom gateway.")
                }
            },
            confirmButton = {
                Button(onClick = { showTraiGuideDialog = false }) { Text("Close Guide") }
            }
        )
    }

    // Privacy Policy Modal
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("In-App Privacy Commitment") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• Zero Internet Permission: This application does not declare android.permission.INTERNET in AndroidManifest.xml. It is technically impossible for this app to transmit your customer data over the internet.")
                    Text("• App-Private Storage: All databases, preferences, and temporary files reside strictly in app-private sandbox storage.")
                    Text("• Cloud Backup Excluded: Both data_extraction_rules.xml and backup_rules.xml explicitly exclude the SQLite database from Google Drive cloud backups.")
                    Text("• No Third-Party Trackers: Zero analytics SDKs, advertising libraries, or telemetry frameworks.")
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) { Text("Got It") }
            }
        )
    }

    // Wipe Confirmation Modal
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            title = { Text("Permanently Delete All Data?") },
            text = {
                Text("This action will immediately erase all customer directories, opt-out suppression lists, templates, and campaign records from this phone. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAllLocalData {
                            showWipeConfirmDialog = false
                            wipeSuccessSnackbar = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
