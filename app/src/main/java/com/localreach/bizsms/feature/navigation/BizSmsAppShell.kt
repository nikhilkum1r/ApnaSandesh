package com.localreach.bizsms.feature.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localreach.bizsms.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.localreach.bizsms.core.ui.theme.StatusSuccess
import com.localreach.bizsms.feature.campaigns.CampaignEditorScreen
import com.localreach.bizsms.feature.campaigns.CampaignReviewScreen
import com.localreach.bizsms.feature.campaigns.CampaignSendingScreen
import com.localreach.bizsms.feature.campaigns.CampaignsViewModel
import com.localreach.bizsms.feature.campaigns.RecipientSelectionScreen
import com.localreach.bizsms.feature.customers.CustomerDirectoryScreen
import com.localreach.bizsms.feature.customers.CustomerGroupsScreen
import com.localreach.bizsms.feature.customers.CustomerImportScreen
import com.localreach.bizsms.feature.customers.CustomersViewModel
import com.localreach.bizsms.feature.dashboard.BusinessProfileScreen
import com.localreach.bizsms.feature.dashboard.DashboardScreen
import com.localreach.bizsms.feature.dashboard.DashboardViewModel
import com.localreach.bizsms.feature.dashboard.OnboardingScreen
import com.localreach.bizsms.feature.history.CampaignHistoryScreen
import com.localreach.bizsms.feature.settings.SettingsScreen
import com.localreach.bizsms.feature.templates.TemplateLibraryScreen

enum class NavDestination(val label: String) {
    DASHBOARD("Dashboard"),
    CAMPAIGNS("Campaigns"),
    CUSTOMERS("Customers"),
    TEMPLATES("Templates"),
    HISTORY("History"),
    SETTINGS("Settings"),
    PROFILE("Profile"),
    IMPORT("Import / Export"),
    GROUPS("Groups"),
    CAMPAIGN_RECIPIENTS("Recipients"),
    CAMPAIGN_REVIEW("Review"),
    CAMPAIGN_SENDING("Sending")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BizSmsAppShell(
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    customersViewModel: CustomersViewModel = hiltViewModel(),
    campaignsViewModel: CampaignsViewModel = hiltViewModel()
) {
    val appSettings by dashboardViewModel.appSettings.collectAsStateWithLifecycle()
    val profile by dashboardViewModel.businessProfile.collectAsStateWithLifecycle()
    val customers by customersViewModel.customers.collectAsStateWithLifecycle()
    val groups by customersViewModel.groups.collectAsStateWithLifecycle()
    val suppressed by customersViewModel.suppressedList.collectAsStateWithLifecycle()
    val csvPreview by customersViewModel.csvPreview.collectAsStateWithLifecycle()

    val templates by campaignsViewModel.templates.collectAsStateWithLifecycle()
    val activeRecipients by campaignsViewModel.activeRecipients.collectAsStateWithLifecycle()
    val currentIndex by campaignsViewModel.currentRecipientIndex.collectAsStateWithLifecycle()
    val campaignStatusMsg by campaignsViewModel.statusMessage.collectAsStateWithLifecycle()
    val allCampaigns by dashboardViewModel.campaigns.collectAsStateWithLifecycle()

    var currentNav by remember { mutableStateOf(NavDestination.DASHBOARD) }

    // Staging parameters across campaign creation steps
    var stagedTitle by remember { mutableStateOf("") }
    var stagedTemplateId by remember { mutableStateOf<String?>(null) }
    var stagedBody by remember { mutableStateOf("") }
    var stagedAppendSig by remember { mutableStateOf(true) }
    var stagedIsPromo by remember { mutableStateOf(false) }

    // Handle initial onboarding
    if (appSettings != null && !appSettings!!.onboardingCompleted) {
        OnboardingScreen(onFinished = { dashboardViewModel.completeOnboarding() })
        return
    }

    val isTopLevel = currentNav in setOf(
        NavDestination.DASHBOARD,
        NavDestination.CAMPAIGNS,
        NavDestination.CUSTOMERS,
        NavDestination.TEMPLATES,
        NavDestination.HISTORY
    )

    // Calculate smart back destination
    val onNavigateBack: () -> Unit = {
        currentNav = when (currentNav) {
            NavDestination.PROFILE -> NavDestination.SETTINGS
            NavDestination.IMPORT -> NavDestination.CUSTOMERS
            NavDestination.GROUPS -> NavDestination.CUSTOMERS
            NavDestination.CAMPAIGN_RECIPIENTS -> NavDestination.CAMPAIGNS
            NavDestination.CAMPAIGN_REVIEW -> NavDestination.CAMPAIGN_RECIPIENTS
            NavDestination.CAMPAIGN_SENDING -> NavDestination.HISTORY
            NavDestination.SETTINGS -> NavDestination.DASHBOARD
            else -> NavDestination.DASHBOARD
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val titleText = when (currentNav) {
                            NavDestination.DASHBOARD -> profile?.businessName?.ifBlank { "LocalReach SMS" } ?: "LocalReach SMS"
                            NavDestination.CAMPAIGNS -> "Compose Campaign"
                            NavDestination.CUSTOMERS -> "Customer Directory"
                            NavDestination.TEMPLATES -> "Message Templates"
                            NavDestination.HISTORY -> "Campaign History"
                            NavDestination.SETTINGS -> "Settings & Privacy"
                            NavDestination.PROFILE -> "Business Profile"
                            NavDestination.IMPORT -> "Import & Export"
                            NavDestination.GROUPS -> "Customer Groups"
                            NavDestination.CAMPAIGN_RECIPIENTS -> "Select Recipients"
                            NavDestination.CAMPAIGN_REVIEW -> "Campaign Review"
                            NavDestination.CAMPAIGN_SENDING -> "Dispatch Studio"
                        }

                        Text(
                            text = titleText,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isTopLevel && currentNav == NavDestination.DASHBOARD) {
                            Text(
                                text = "100% Offline • Private SIM • Zero Cloud Sync",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (!isTopLevel) {
                            Text(
                                text = profile?.businessName?.ifBlank { "LocalReach SMS" } ?: "LocalReach SMS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (!isTopLevel) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp, end = 4.dp)
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_textflow_mark),
                                contentDescription = "TextFlow Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                },
                actions = {
                    if (currentNav != NavDestination.SETTINGS) {
                        IconButton(onClick = { currentNav = NavDestination.SETTINGS }) {
                            Icon(Icons.Default.Tune, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentNav == NavDestination.DASHBOARD,
                        onClick = { currentNav = NavDestination.DASHBOARD },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { BottomNavLabel("Dashboard", currentNav == NavDestination.DASHBOARD) },
                        alwaysShowLabel = true
                    )
                    NavigationBarItem(
                        selected = currentNav == NavDestination.CAMPAIGNS,
                        onClick = { currentNav = NavDestination.CAMPAIGNS },
                        icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Campaigns") },
                        label = { BottomNavLabel("Campaigns", currentNav == NavDestination.CAMPAIGNS) },
                        alwaysShowLabel = true
                    )
                    NavigationBarItem(
                        selected = currentNav == NavDestination.CUSTOMERS,
                        onClick = { currentNav = NavDestination.CUSTOMERS },
                        icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                        label = { BottomNavLabel("Customers", currentNav == NavDestination.CUSTOMERS) },
                        alwaysShowLabel = true
                    )
                    NavigationBarItem(
                        selected = currentNav == NavDestination.TEMPLATES,
                        onClick = { currentNav = NavDestination.TEMPLATES },
                        icon = { Icon(Icons.Default.Description, contentDescription = "Templates") },
                        label = { BottomNavLabel("Templates", currentNav == NavDestination.TEMPLATES) },
                        alwaysShowLabel = true
                    )
                    NavigationBarItem(
                        selected = currentNav == NavDestination.HISTORY,
                        onClick = { currentNav = NavDestination.HISTORY },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { BottomNavLabel("History", currentNav == NavDestination.HISTORY) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentNav) {
                NavDestination.DASHBOARD -> DashboardScreen(
                    profile = profile,
                    customersCount = customers.size,
                    eligibleCount = customers.count { !it.isOptedOut && it.consentStatus == "GRANTED" },
                    suppressedCount = suppressed.size,
                    draftsCount = allCampaigns.count { it.status == "DRAFT" },
                    recentCampaignsCount = allCampaigns.size,
                    onNavigateToCustomers = { currentNav = NavDestination.CUSTOMERS },
                    onNavigateToImport = { currentNav = NavDestination.IMPORT },
                    onNavigateToCampaigns = { currentNav = NavDestination.CAMPAIGNS },
                    onNavigateToTemplates = { currentNav = NavDestination.TEMPLATES },
                    onNavigateToSettings = { currentNav = NavDestination.SETTINGS },
                    onNavigateToGroups = { currentNav = NavDestination.GROUPS },
                    onNavigateToProfile = { currentNav = NavDestination.PROFILE }
                )
                NavDestination.PROFILE -> BusinessProfileScreen(
                    profile = profile,
                    onSaveProfile = { name, owner, cat, phone, addr, code, sig ->
                        dashboardViewModel.saveBusinessProfile(name, owner, cat, phone, addr, code, sig)
                    },
                    onBack = { currentNav = NavDestination.SETTINGS }
                )
                NavDestination.CUSTOMERS -> CustomerDirectoryScreen(
                    customers = customers,
                    groups = groups,
                    onAddCustomer = { name, phone, tags, notes, consent, source, onSuccess ->
                        customersViewModel.addCustomer(name, phone, tags, notes, consent, source, onSuccess)
                    },
                    onUpdateCustomer = { cust, name, phone, tags, notes, consent, onSuccess ->
                        customersViewModel.updateCustomer(cust, name, phone, tags, notes, consent, onSuccess)
                    },
                    onDeleteCustomer = { customersViewModel.deleteCustomer(it) },
                    onOptOut = { cust, reason -> customersViewModel.recordOptOut(cust, reason) },
                    onNavigateToImport = { currentNav = NavDestination.IMPORT },
                    onNavigateToGroups = { currentNav = NavDestination.GROUPS }
                )
                NavDestination.IMPORT -> CustomerImportScreen(
                    preview = csvPreview,
                    onPickContact = { customersViewModel.importContact(it) },
                    onImportCsv = { customersViewModel.previewCsv(it) },
                    onExportCsv = { customersViewModel.exportCustomers(it) },
                    onClearPreview = { customersViewModel.clearCsvPreview() },
                    onBack = { currentNav = NavDestination.CUSTOMERS }
                )
                NavDestination.GROUPS -> CustomerGroupsScreen(
                    groups = groups,
                    customers = customers,
                    onCreateGroup = { name, desc -> customersViewModel.createGroup(name, desc) },
                    onUpdateGroup = { id, name, desc -> customersViewModel.updateGroup(id, name, desc) },
                    onDeleteGroup = { customersViewModel.deleteGroup(it) },
                    onBack = { currentNav = NavDestination.CUSTOMERS }
                )
                NavDestination.TEMPLATES -> TemplateLibraryScreen(
                    onSelectTemplateForCampaign = { tpl ->
                        stagedTitle = tpl.title
                        stagedTemplateId = tpl.id
                        stagedBody = tpl.bodyTemplate
                        stagedIsPromo = tpl.isPromotional
                        currentNav = NavDestination.CAMPAIGNS
                    }
                )
                NavDestination.CAMPAIGNS -> CampaignEditorScreen(
                    templates = templates,
                    profile = profile,
                    segmentCalculator = campaignsViewModel.segmentCalculator,
                    templateRenderer = campaignsViewModel.templateRenderer,
                    onContinueToRecipients = { title, tplId, body, appendSig, isPromo ->
                        stagedTitle = title
                        stagedTemplateId = tplId
                        stagedBody = body
                        stagedAppendSig = appendSig
                        stagedIsPromo = isPromo
                        currentNav = NavDestination.CAMPAIGN_RECIPIENTS
                    }
                )
                NavDestination.CAMPAIGN_RECIPIENTS -> RecipientSelectionScreen(
                    customers = customers,
                    groups = groups,
                    isPromotional = stagedIsPromo,
                    onContinueToReview = { selectedCustIds ->
                        campaignsViewModel.stageCampaign(
                            name = stagedTitle,
                            templateId = stagedTemplateId,
                            rawMessageTemplate = stagedBody,
                            appendSignature = stagedAppendSig,
                            isPromotional = stagedIsPromo,
                            transportType = appSettings?.preferredTransport ?: "COMPOSER_HANDOFF",
                            selectedSubscriptionId = appSettings?.selectedSimSubscriptionId,
                            selectedCustomerIds = selectedCustIds,
                            onSuccess = { currentNav = NavDestination.CAMPAIGN_REVIEW }
                        )
                    },
                    onBack = { currentNav = NavDestination.CAMPAIGNS }
                )
                NavDestination.CAMPAIGN_REVIEW -> CampaignReviewScreen(
                    campaignTitle = stagedTitle,
                    rawTemplate = stagedBody,
                    appendSignature = stagedAppendSig,
                    isPromotional = stagedIsPromo,
                    profile = profile,
                    recipients = activeRecipients,
                    traiAdvisor = campaignsViewModel.traiAdvisor,
                    onConfirmAndStartSending = {
                        val activeId = campaignsViewModel.activeCampaignId.value
                        if (activeId != null) {
                            campaignsViewModel.startSendingSession(activeId)
                            currentNav = NavDestination.CAMPAIGN_SENDING
                        }
                    },
                    onBack = { currentNav = NavDestination.CAMPAIGN_RECIPIENTS }
                )
                NavDestination.CAMPAIGN_SENDING -> CampaignSendingScreen(
                    campaignTitle = stagedTitle,
                    recipients = activeRecipients,
                    currentIndex = currentIndex,
                    statusMessage = campaignStatusMsg,
                    onDismissStatus = { campaignsViewModel.dismissMessage() },
                    onDispatchRecipient = { campaignsViewModel.dispatchCurrentRecipient(it) },
                    onMarkSent = { campaignsViewModel.markCurrentRecipientSent(it) },
                    onSkipRecipient = { campaignsViewModel.markCurrentRecipientSkipped(it) },
                    onCancelCampaign = {
                        val activeId = campaignsViewModel.activeCampaignId.value
                        if (activeId != null) {
                            campaignsViewModel.cancelCampaign(activeId)
                            currentNav = NavDestination.HISTORY
                        }
                    },
                    onDone = { currentNav = NavDestination.HISTORY }
                )
                NavDestination.HISTORY -> CampaignHistoryScreen(
                    onNavigateToSending = { id ->
                        campaignsViewModel.selectCampaign(id)
                        currentNav = NavDestination.CAMPAIGN_SENDING
                    }
                )
                NavDestination.SETTINGS -> SettingsScreen(
                    onNavigateToProfile = { currentNav = NavDestination.PROFILE }
                )
            }
        }
    }
}

@Composable
private fun BottomNavLabel(
    text: String,
    isSelected: Boolean
) {
    Text(
        text = text,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            letterSpacing = (-0.3).sp,
            lineHeight = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    )
}

