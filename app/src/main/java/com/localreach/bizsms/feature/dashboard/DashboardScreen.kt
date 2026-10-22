package com.localreach.bizsms.feature.dashboard

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
import com.localreach.bizsms.core.database.BusinessProfileEntity
import com.localreach.bizsms.core.database.CampaignDao
import com.localreach.bizsms.core.database.CustomerDao
import com.localreach.bizsms.core.database.ProfileAndSettingsDao
import com.localreach.bizsms.core.database.SuppressionEntryEntity
import com.localreach.bizsms.feature.common.MetricStatCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val profileDao: ProfileAndSettingsDao,
    private val customerDao: CustomerDao,
    private val campaignDao: CampaignDao
) : ViewModel() {

    val businessProfile = profileDao.observeBusinessProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSettings = profileDao.observeAppSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val customers = customerDao.observeAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppressionList = customerDao.observeSuppressionList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val campaigns = campaignDao.observeCampaigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun completeOnboarding() {
        viewModelScope.launch {
            val current = profileDao.getAppSettings()
            if (current != null) {
                profileDao.upsertAppSettings(current.copy(onboardingCompleted = true))
            }
        }
    }

    fun saveBusinessProfile(
        businessName: String,
        ownerDisplayName: String,
        businessCategory: String,
        communicationPhone: String,
        businessAddress: String,
        callingCode: String,
        signature: String
    ) {
        viewModelScope.launch {
            profileDao.upsertBusinessProfile(
                BusinessProfileEntity(
                    id = "default_profile",
                    businessName = businessName.trim(),
                    ownerDisplayName = ownerDisplayName.trim(),
                    businessCategory = businessCategory.trim(),
                    communicationPhone = communicationPhone.trim(),
                    businessAddress = businessAddress.trim().ifEmpty { null },
                    defaultCountryCallingCode = callingCode.trim().ifEmpty { "+91" },
                    defaultMessageSignature = signature.trim(),
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }
}

/**
 * Screen 1: Onboarding & Permissions Explanation
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Welcome to LocalReach SMS",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your private, offline-first business messaging tool. Connect with your existing customers directly through your own phone number and personal SIM card.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Core Principles", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    PrincipleRow(Icons.Default.CloudOff, "Zero Cloud Sync", "All customer records, message history, and templates stay strictly on your device. No cloud backend, no account required.")
                    PrincipleRow(Icons.Default.Send, "Transparent Sending", "Default mode uses your system SMS Composer where you inspect and confirm every message. No silent background sending.")
                    PrincipleRow(Icons.Default.Block, "Mandatory Opt-Out Firewall", "When a customer requests opt-out, they are permanently suppressed from future campaigns automatically.")
                    PrincipleRow(Icons.Default.Security, "TRAI & Telecom Compliance", "Advises on Indian personal SIM rate caps (100 SMS/day) and commercial promotional guidelines.")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onFinished,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Get Started", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun PrincipleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Screen 2: Business Profile Setup & Edit
 */
@Composable
fun BusinessProfileScreen(
    profile: BusinessProfileEntity?,
    onSaveProfile: (String, String, String, String, String, String, String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var businessName by remember(profile) { mutableStateOf(profile?.businessName ?: "") }
    var ownerName by remember(profile) { mutableStateOf(profile?.ownerDisplayName ?: "") }
    var category by remember(profile) { mutableStateOf(profile?.businessCategory ?: "Retail & Services") }
    var phone by remember(profile) { mutableStateOf(profile?.communicationPhone ?: "") }
    var address by remember(profile) { mutableStateOf(profile?.businessAddress ?: "") }
    var callingCode by remember(profile) { mutableStateOf(profile?.defaultCountryCallingCode ?: "+91") }
    var signature by remember(profile) { mutableStateOf(profile?.defaultMessageSignature ?: "- My Shop") }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
        Text("Business Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Configure how your business is identified in customer messages and template placeholders.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name *") },
            placeholder = { Text("e.g. Ramesh General Stores") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it },
            label = { Text("Owner / Manager Display Name *") },
            placeholder = { Text("e.g. Ramesh Kumar") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Business Category") },
            placeholder = { Text("e.g. Grocery, Salon, Auto Repair, Freelancer") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Official Contact Phone Number *") },
            placeholder = { Text("e.g. +91 98765 43210 (manually entered)") },
            supportingText = { Text("This number is inserted into {{business_phone}} for customer callbacks.") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Business Address (Optional)") },
            placeholder = { Text("e.g. Shop #4, Market Road") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = callingCode,
            onValueChange = { callingCode = it },
            label = { Text("Default Country Calling Code") },
            placeholder = { Text("+91") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = signature,
            onValueChange = { signature = it },
            label = { Text("Default Message Signature") },
            placeholder = { Text("- Ramesh General Stores") },
            supportingText = { Text("Appended automatically when {{signature}} is used.") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSaveProfile(businessName, ownerName, category, phone, address, callingCode, signature)
                showSavedSnackbar = true
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Business Profile")
        }

        if (showSavedSnackbar) {
            Snackbar(
                action = { TextButton(onClick = { showSavedSnackbar = false }) { Text("OK") } }
            ) {
                Text("Business Profile saved locally.")
            }
        }
    }
}

/**
 * Screen 3: Dashboard Screen
 */
@Composable
fun DashboardScreen(
    profile: BusinessProfileEntity?,
    customersCount: Int,
    eligibleCount: Int,
    suppressedCount: Int,
    draftsCount: Int,
    recentCampaignsCount: Int,
    onNavigateToCustomers: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToCampaigns: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = profile?.businessName ?: "My Business",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${profile?.businessCategory ?: "Business"} • ${profile?.ownerDisplayName ?: "Owner"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (!profile?.communicationPhone.isNullOrBlank()) {
                    Text(
                        text = "Phone: ${profile?.communicationPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Text("Key Business Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricStatCard(label = "Total Customers", value = customersCount.toString(), modifier = Modifier.weight(1f))
            MetricStatCard(label = "Eligible to Message", value = eligibleCount.toString(), subtext = "Opted-In", modifier = Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricStatCard(label = "Opt-Out Suppressed", value = suppressedCount.toString(), subtext = "Blocked", modifier = Modifier.weight(1f))
            MetricStatCard(label = "Total Campaigns", value = recentCampaignsCount.toString(), subtext = "$draftsCount Draft(s)", modifier = Modifier.weight(1f))
        }

        Text("Quick Operations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNavigateToCampaigns,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Campaign / Sending Session")
                }

                OutlinedButton(
                    onClick = onNavigateToCustomers,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.People, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Customer Directory")
                }

                OutlinedButton(
                    onClick = onNavigateToImport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Contacts & CSV")
                }

                OutlinedButton(
                    onClick = onNavigateToTemplates,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Message Templates")
                }
            }
        }
    }
}
