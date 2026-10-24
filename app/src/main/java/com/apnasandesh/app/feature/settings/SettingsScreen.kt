package com.apnasandesh.app.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnasandesh.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.apnasandesh.app.core.database.AppSettingsEntity
import com.apnasandesh.app.core.database.AppPreferencesDataStore
import com.apnasandesh.app.core.database.BizSmsDatabase
import com.apnasandesh.app.core.database.ProfileAndSettingsDao
import com.apnasandesh.app.core.model.SimSubscriptionInfo
import com.apnasandesh.app.core.ui.theme.*
import com.apnasandesh.app.domain.transport.TransportPolicyGate
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

    val businessProfile = profileDao.observeBusinessProfile()
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
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val activeSims by viewModel.activeSimSubscriptions.collectAsStateWithLifecycle()

    var showDirectSimDisclosureDialog by remember { mutableStateOf(false) }
    var showTraiGuideDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var wipeSuccessSnackbar by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshSims()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Section 1: Business Profile Shortcut
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile?.businessName?.ifBlank { "Business Profile" } ?: "Business Profile",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${profile?.communicationPhone ?: "No phone"} • ${profile?.ownerDisplayName ?: "Owner"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onNavigateToProfile,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Edit")
                }
            }
        }

        // Section 2: SMS Transport Engine
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SMS Dispatch Engine",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Option 1: Native SMS Composer
                Surface(
                    color = if (settings?.preferredTransport != "DIRECT_SIM") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (settings?.preferredTransport != "DIRECT_SIM") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RadioButton(
                            selected = settings?.preferredTransport != "DIRECT_SIM",
                            onClick = { viewModel.updateTransportMode("COMPOSER_HANDOFF") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Android SMS Composer",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = StatusSuccessContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Recommended",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusSuccess,
                                        fontWeight = FontWeight.Bold,
                                        softWrap = false,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sends via your phone's SMS app with full review before sending.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                val isDirectSimDeclared = remember { viewModel.policyGate.isDirectSmsPermissionDeclared() }
                var showPlayPolicyNoticeDialog by remember { mutableStateOf(false) }

                // Option 2: Direct SIM Radio
                Surface(
                    color = if (isDirectSimDeclared && settings?.preferredTransport == "DIRECT_SIM") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isDirectSimDeclared && settings?.preferredTransport == "DIRECT_SIM") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RadioButton(
                            selected = isDirectSimDeclared && settings?.preferredTransport == "DIRECT_SIM",
                            onClick = {
                                if (isDirectSimDeclared) {
                                    if (settings?.directSimPolicyAcknowledged == true) {
                                        viewModel.updateTransportMode("DIRECT_SIM")
                                        if (!viewModel.policyGate.hasDirectSmsRuntimePermissions()) {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.SEND_SMS,
                                                    Manifest.permission.READ_PHONE_STATE
                                                )
                                            )
                                        }
                                    } else {
                                        showDirectSimDisclosureDialog = true
                                    }
                                } else {
                                    showPlayPolicyNoticeDialog = true
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Direct SIM Dispatch",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (!isDirectSimDeclared) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Website Edition",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDirectSimDeclared) "Automated background dispatch (requires SMS permissions)." else "Automated background dispatch (requires Direct APK from official website).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!isDirectSimDeclared) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Play Compliant Release: Uses Android SMS Composer. Automated Direct SIM dispatch is available in the Direct APK on our website.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (showPlayPolicyNoticeDialog) {
                    AlertDialog(
                        onDismissRequest = { showPlayPolicyNoticeDialog = false },
                        title = {
                            Text(
                                text = "Google Play Policy Compliance",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        text = {
                            Text(
                                text = "To strictly comply with Google Play's SMS and Call Log policy, this edition of ApnaSandesh exclusively uses the Android SMS Composer mode (ACTION_SENDTO) with zero sensitive permissions.\n\nAutomated sequential Direct SIM dispatch is available in the Direct APK edition available for download from our official website (apnasandesh.trustoryx.digital).",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { showPlayPolicyNoticeDialog = false }) {
                                Text("Understood", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // Direct SIM controls when acknowledged
                if (isDirectSimDeclared && settings?.directSimPolicyAcknowledged == true) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Direct SIM Dispatch",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Requires SEND_SMS permission grant",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

                    // Dual SIM Configuration
                    Text(
                        text = "Dual-SIM Subscription Slot",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (activeSims.isEmpty()) {
                        OutlinedButton(
                            onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE)) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Detect Installed Dual SIM Slots")
                        }
                    } else {
                        activeSims.forEach { sim ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
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

        // Section 3: Telecom Regulatory & India TRAI Mode
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Telecom & Rate Quota Advisory",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "India TRAI Compliance Mode",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Advises on 100 SMS/day limit and 10 AM - 9 PM commercial window.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings?.indiaTraiComplianceMode != false,
                        onCheckedChange = { viewModel.toggleIndiaTraiMode(it) }
                    )
                }

                OutlinedButton(
                    onClick = { showTraiGuideDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TRAI Compliance Guide")
                }
            }
        }

        // Section 4: Appearance & Theme Mode
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Appearance & Theme",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select application visual theme:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = settings?.themeMode == "SYSTEM" || settings?.themeMode == null,
                        onClick = { viewModel.updateThemeMode("SYSTEM") },
                        label = { Text("System") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = settings?.themeMode == "LIGHT",
                        onClick = { viewModel.updateThemeMode("LIGHT") },
                        label = { Text("Light") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = settings?.themeMode == "DARK",
                        onClick = { viewModel.updateThemeMode("DARK") },
                        label = { Text("Dark") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 5: Privacy & Data Sovereignty
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Data Sovereignty & Offline Guarantee",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text = "This app declares zero internet permissions in AndroidManifest.xml. Your customer phone numbers, message texts, and opt-out suppression records cannot be leaked or transmitted to any third-party server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                OutlinedButton(
                    onClick = { showPrivacyPolicyDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Offline Privacy Commitment")
                }
            }
        }

        // Section 6: Danger Zone (Erase Data)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, StatusError.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Danger Zone: Clear Local Database",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = StatusError
                )
                Text(
                    text = "Permanently wipe all customer records, suppression lists, message history, and business profile settings from this phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { showWipeConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete All Local Business Data", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 7: About ApnaSandesh
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.apnasandesh_logo),
                        contentDescription = "ApnaSandesh Logo",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "ApnaSandesh",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "v1.0.0 • Production Release",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "A production-grade, offline-first communication suite for small business owners and service providers. Send SMS directly via your personal phone and SIM card with zero cloud sync.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = StatusSuccessContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_privacy_shield_light),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_sim_chip_light),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Personal SIM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (wipeSuccessSnackbar) {
            Snackbar(action = { TextButton(onClick = { wipeSuccessSnackbar = false }) { Text("OK") } }) {
                Text("All local business data successfully deleted.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Direct SIM Disclosure Modal
    if (showDirectSimDisclosureDialog) {
        AlertDialog(
            onDismissRequest = { showDirectSimDisclosureDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Direct SIM Policy & Quota Disclosure", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                Button(
                    onClick = {
                        viewModel.acknowledgeDirectSimPolicy(true)
                        viewModel.updateTransportMode("DIRECT_SIM")
                        viewModel.toggleDirectSimFeature(true)
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_PHONE_STATE
                            )
                        )
                        showDirectSimDisclosureDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
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
            shape = RoundedCornerShape(16.dp),
            title = { Text("India TRAI (TCCCPR) Compliance Guide", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("1. Personal SIM Daily Limit:", fontWeight = FontWeight.Bold)
                    Text("Indian telecom operators enforce a regulatory limit of 100 SMS per day per SIM card. Exceeding 100 messages triggers commercial tariff spikes or automated SIM suspension.")
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
                Button(
                    onClick = { showTraiGuideDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close Guide")
                }
            }
        )
    }

    // Privacy Policy Modal
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("In-App Privacy Commitment", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("• Zero Internet Permission: This application does not declare android.permission.INTERNET in AndroidManifest.xml. It is technically impossible for this app to transmit your customer data over the internet.")
                    Text("• App-Private Storage: All customer profiles, message templates, contact groupings, and dispatch logs reside strictly in app-private sandbox storage (/data/data/com.apnasandesh.app/databases/).")
                    Text("• Cloud Backup Excluded: Both data_extraction_rules.xml and backup_rules.xml explicitly exclude the SQLite database from Google Drive cloud backups.")
                    Text("• No Third-Party Trackers: Zero analytics SDKs, advertising libraries, or telemetry frameworks.")
                    Text("• Sensitive Permissions: SEND_SMS is used solely for Direct SIM sending on your command; READ_PHONE_STATE is used solely for Dual-SIM routing. No personal call logs, incoming SMS, or IMEI are accessed.")
                    Text("• Complete Self-Service Deletion: You can delete all data permanently anytime via 'Danger Zone' in Settings or by clearing app storage.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Got It")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://trustoryx.digital/privacy"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Full Web Policy")
                }
            }
        )
    }

    // Wipe Confirmation Modal
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Permanently Delete All Data?", fontWeight = FontWeight.Bold) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                    shape = RoundedCornerShape(8.dp)
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
