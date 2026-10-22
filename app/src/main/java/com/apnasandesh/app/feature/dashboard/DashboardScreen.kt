package com.apnasandesh.app.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnasandesh.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.apnasandesh.app.core.database.AppSettingsEntity
import com.apnasandesh.app.core.database.BusinessProfileEntity
import com.apnasandesh.app.core.database.CampaignDao
import com.apnasandesh.app.core.database.CustomerDao
import com.apnasandesh.app.core.database.ProfileAndSettingsDao
import com.apnasandesh.app.core.ui.theme.*
import com.apnasandesh.app.feature.common.MetricStatCard
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
            } else {
                profileDao.upsertAppSettings(
                    AppSettingsEntity(
                        id = "app_settings",
                        onboardingCompleted = true
                    )
                )
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
        signature: String,
        website: String = "",
        currencySymbol: String = "₹",
        preferredLanguage: String = "English",
        brandTone: String = "Professional"
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
                    website = website.trim(),
                    currencySymbol = currencySymbol.trim().ifEmpty { "₹" },
                    preferredLanguage = preferredLanguage.trim().ifEmpty { "English" },
                    brandTone = brandTone.trim().ifEmpty { "Professional" },
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }
}

/**
 * Screen 1: Onboarding & Core Product Value
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Identity Badge - ApnaSandesh Logo
            Image(
                painter = painterResource(id = R.drawable.apnasandesh_logo),
                contentDescription = "ApnaSandesh Logo",
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Welcome to ApnaSandesh",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Communicate directly with your existing customers using your own phone and SIM card. 100% private, offline, and subscription-free.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Widescreen Light Hero Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.apnasandesh_hero_banner),
                    contentDescription = "ApnaSandesh App Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Principles Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Built for Local Businesses",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    PrincipleRow(
                        icon = Icons.Default.CloudOff,
                        title = "Zero Cloud Sync",
                        description = "All contacts, drafts, and message history remain exclusively on your device. No cloud storage and no account required."
                    )
                    PrincipleRow(
                        icon = Icons.AutoMirrored.Filled.Send,
                        title = "Transparent SMS Composer",
                        description = "Uses Android's native SMS messaging app so you can inspect, review, and confirm every message before it is dispatched."
                    )
                    PrincipleRow(
                        icon = Icons.Default.Shield,
                        title = "Opt-Out Firewall",
                        description = "When a customer requests opt-out, they are permanently suppressed from future marketing broadcasts automatically."
                    )
                    PrincipleRow(
                        icon = Icons.Default.VerifiedUser,
                        title = "Telecom & Rate Guidance",
                        description = "Keeps you safe within personal SIM daily caps (100 SMS/day in India) and commercial communication hours."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onFinished,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PrincipleRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
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
    onSaveProfileExtended: ((String, String, String, String, String, String, String, String, String, String, String) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    var businessName by remember(profile) { mutableStateOf(profile?.businessName ?: "") }
    var ownerName by remember(profile) { mutableStateOf(profile?.ownerDisplayName ?: "") }
    var category by remember(profile) { mutableStateOf(profile?.businessCategory ?: "Retail & Services") }
    var phone by remember(profile) { mutableStateOf(profile?.communicationPhone ?: "") }
    var address by remember(profile) { mutableStateOf(profile?.businessAddress ?: "") }
    var callingCode by remember(profile) { mutableStateOf(profile?.defaultCountryCallingCode ?: "+91") }
    var signature by remember(profile) { mutableStateOf(profile?.defaultMessageSignature ?: "- My Shop") }
    var website by remember(profile) { mutableStateOf(profile?.website ?: "") }
    var currencySymbol by remember(profile) { mutableStateOf(profile?.currencySymbol ?: "₹") }
    var preferredLanguage by remember(profile) { mutableStateOf(profile?.preferredLanguage ?: "English") }
    var brandTone by remember(profile) { mutableStateOf(profile?.brandTone ?: "Professional") }

    var showSavedSnackbar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (onBack != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Business Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = "Business Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Configure your official business credentials. These are automatically populated into message placeholders like {{business_name}} and {{signature}}.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Identity Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Store & Brand Identity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business Name *") },
                    placeholder = { Text("e.g. Ramesh General Stores") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner / Manager Display Name *") },
                    placeholder = { Text("e.g. Ramesh Kumar") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Business Category") },
                    placeholder = { Text("e.g. Grocery, Salon, Auto Repair, Freelancer") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Contact Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Contact & Callback Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Official Contact Phone Number *") },
                    placeholder = { Text("+91 98765 43210") },
                    supportingText = { Text("Inserted into {{business_phone}} for customer callbacks.") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = callingCode,
                        onValueChange = { callingCode = it },
                        label = { Text("Default Calling Code") },
                        placeholder = { Text("+91") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.45f)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address (Optional)") },
                        placeholder = { Text("Shop #4, Market Rd") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.55f)
                    )
                }

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website / Online Link (Optional)") },
                    placeholder = { Text("https://myshop.com") },
                    supportingText = { Text("Populated into {{business_website}}.") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = { currencySymbol = it },
                        label = { Text("Currency") },
                        placeholder = { Text("₹") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.35f)
                    )

                    OutlinedTextField(
                        value = brandTone,
                        onValueChange = { brandTone = it },
                        label = { Text("Brand Tone") },
                        placeholder = { Text("Professional / Warm / Casual") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.65f)
                    )
                }
            }
        }

        // Signature Section
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
                    text = "Automated Message Signature",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = signature,
                    onValueChange = { signature = it },
                    label = { Text("Default Message Signature") },
                    placeholder = { Text("- Ramesh Stores, Bangalore") },
                    supportingText = { Text("Appended automatically when {{signature}} is enabled in campaigns.") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Signature preview
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Preview:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = signature.ifBlank { "(No signature set)" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (onSaveProfileExtended != null) {
                    onSaveProfileExtended(businessName, ownerName, category, phone, address, callingCode, signature, website, currencySymbol, preferredLanguage, brandTone)
                } else {
                    onSaveProfile(businessName, ownerName, category, phone, address, callingCode, signature)
                }
                showSavedSnackbar = true
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Business Profile", fontWeight = FontWeight.SemiBold)
        }

        if (showSavedSnackbar) {
            Snackbar(
                action = {
                    TextButton(onClick = { showSavedSnackbar = false }) {
                        Text("OK")
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Business Profile saved successfully.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
    onNavigateToSettings: () -> Unit,
    onNavigateToGroups: (() -> Unit)? = null,
    onNavigateToProfile: (() -> Unit)? = null
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCampaigns,
                icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                text = { Text("New Campaign", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(14.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Business Header Hero Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Business Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = (profile?.businessName ?: "LocalReach")
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .map { it.first().uppercase() }
                        .joinToString("")
                        .ifBlank { "LR" }

                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile?.businessName?.ifBlank { "My Business" } ?: "My Business",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${profile?.businessCategory ?: "Retail"} • ${profile?.ownerDisplayName ?: "Owner"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(StatusSuccess, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Offline-First • Private SIM",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (onNavigateToProfile != null) {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Primary Campaign CTA Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reach Your Customers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Send personalized SMS broadcasts directly from your SIM card.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                            lineHeight = 18.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToCampaigns,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = "New Campaign",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Key Business Metrics Grid
        Text(
            text = "Audience Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricStatCard(
                label = "Total Customers",
                value = customersCount.toString(),
                icon = Icons.Default.People,
                subtext = "Contacts",
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                label = "Eligible for SMS",
                value = eligibleCount.toString(),
                icon = Icons.Default.CheckCircle,
                subtext = "Eligible",
                accentColor = StatusSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricStatCard(
                label = "Opt-Outs",
                value = suppressedCount.toString(),
                icon = Icons.Default.Shield,
                subtext = "Blocked",
                accentColor = if (suppressedCount > 0) StatusError else null,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                label = "Total Campaigns",
                value = recentCampaignsCount.toString(),
                icon = Icons.Default.History,
                subtext = if (draftsCount > 0) "$draftsCount draft(s)" else "Sent",
                modifier = Modifier.weight(1f)
            )
        }

        // Daily TRAI SIM Limit Tracker Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = BrandAmberTertiaryLight.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SimCard,
                        contentDescription = null,
                        tint = BrandAmberTertiaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily SIM Advisory",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Personal SIM cards: keep under 100 SMS/day to avoid spam flags.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Operations Grid
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionTile(
                icon = Icons.Default.People,
                title = "Contacts",
                subtitle = "View directory",
                onClick = onNavigateToCustomers,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                icon = Icons.Default.Description,
                title = "Templates",
                subtitle = "Message drafts",
                onClick = onNavigateToTemplates,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionTile(
                icon = Icons.Default.UploadFile,
                title = "Import",
                subtitle = "CSV & contacts",
                onClick = onNavigateToImport,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                icon = Icons.Default.Tune,
                title = "Settings",
                subtitle = "SIM & preferences",
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
