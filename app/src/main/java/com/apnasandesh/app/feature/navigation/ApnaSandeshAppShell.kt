package com.apnasandesh.app.feature.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.apnasandesh.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apnasandesh.app.core.ui.theme.StatusSuccess
import com.apnasandesh.app.feature.campaigns.CampaignEditorScreen
import com.apnasandesh.app.feature.campaigns.CampaignReviewScreen
import com.apnasandesh.app.feature.campaigns.CampaignSendingScreen
import com.apnasandesh.app.feature.campaigns.CampaignsViewModel
import com.apnasandesh.app.feature.campaigns.RecipientSelectionScreen
import com.apnasandesh.app.feature.campaigns.SmsComposerHandoffScreen
import com.apnasandesh.app.feature.customers.CustomerDetailsScreen
import com.apnasandesh.app.feature.customers.CustomerDirectoryScreen
import com.apnasandesh.app.feature.customers.CustomerGroupsScreen
import com.apnasandesh.app.feature.customers.CustomerImportScreen
import com.apnasandesh.app.feature.customers.CustomersViewModel
import com.apnasandesh.app.feature.dashboard.BusinessProfileScreen
import com.apnasandesh.app.feature.dashboard.DashboardScreen
import com.apnasandesh.app.feature.dashboard.DashboardViewModel
import com.apnasandesh.app.feature.dashboard.OnboardingScreen
import com.apnasandesh.app.feature.history.CampaignHistoryScreen
import com.apnasandesh.app.feature.settings.SettingsScreen
import com.apnasandesh.app.feature.templates.TemplateEditorScreen
import com.apnasandesh.app.feature.templates.TemplateLibraryScreen
import com.apnasandesh.app.feature.templates.TemplatesViewModel

enum class NavDestination(val label: String) {
    DASHBOARD("Dashboard"),
    CAMPAIGNS("Campaigns"),
    CUSTOMERS("Customers"),
    CUSTOMER_DETAILS("Customer Details"),
    TEMPLATES("Templates"),
    TEMPLATE_EDITOR("Template Editor"),
    HISTORY("History"),
    SETTINGS("Settings"),
    PROFILE("Profile"),
    IMPORT("Import / Export"),
    GROUPS("Groups"),
    CAMPAIGN_RECIPIENTS("Recipients"),
    CAMPAIGN_REVIEW("Review"),
    CAMPAIGN_SENDING("Sending"),
    COMPOSER_HANDOFF("SMS Composer")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApnaSandeshAppShell(
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    customersViewModel: CustomersViewModel = hiltViewModel(),
    campaignsViewModel: CampaignsViewModel = hiltViewModel(),
    templatesViewModel: TemplatesViewModel = hiltViewModel()
) {
    val appSettings by dashboardViewModel.appSettings.collectAsStateWithLifecycle()
    val profile by dashboardViewModel.businessProfile.collectAsStateWithLifecycle()
    val customers by customersViewModel.customers.collectAsStateWithLifecycle()
    val groups by customersViewModel.groups.collectAsStateWithLifecycle()
    val suppressed by customersViewModel.suppressedList.collectAsStateWithLifecycle()
    val csvPreview by customersViewModel.csvPreview.collectAsStateWithLifecycle()
    val deviceContacts by customersViewModel.deviceContacts.collectAsStateWithLifecycle()
    val isLoadingDeviceContacts by customersViewModel.isLoadingDeviceContacts.collectAsStateWithLifecycle()

    val templates by campaignsViewModel.templates.collectAsStateWithLifecycle()
    val activeRecipients by campaignsViewModel.activeRecipients.collectAsStateWithLifecycle()
    val currentIndex by campaignsViewModel.currentRecipientIndex.collectAsStateWithLifecycle()
    val campaignStatusMsg by campaignsViewModel.statusMessage.collectAsStateWithLifecycle()
    val allCampaigns by dashboardViewModel.campaigns.collectAsStateWithLifecycle()

    var currentNav by remember { mutableStateOf(NavDestination.DASHBOARD) }

    // Staging parameters across customer details & template editor
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }
    var selectedTemplateForEditId by remember { mutableStateOf<String?>(null) }

    // Staging parameters across campaign creation steps
    var stagedTitle by remember { mutableStateOf("") }
    var stagedTemplateId by remember { mutableStateOf<String?>(null) }
    var stagedBody by remember { mutableStateOf("") }
    var stagedAppendSig by remember { mutableStateOf(true) }
    var stagedIsPromo by remember { mutableStateOf(false) }
    var stagedChannel by remember { mutableStateOf("DIRECT_SIM") }
    var stagedIntervalSeconds by remember { mutableIntStateOf(3) }
    var stagedSimId by remember { mutableStateOf<Int?>(null) }

    // Handle initial onboarding
    if (appSettings != null && !appSettings!!.onboardingCompleted) {
        OnboardingScreen(onFinished = { dashboardViewModel.completeOnboarding() })
        return
    }

    // Global Snackbar state for error/success messages
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Notification granted or dismissed */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotif = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasNotif) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Show campaign status messages as Snackbar
    LaunchedEffect(campaignStatusMsg) {
        campaignStatusMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            campaignsViewModel.dismissMessage()
        }
    }

    val isTopLevel = currentNav in setOf(
        NavDestination.DASHBOARD,
        NavDestination.CAMPAIGNS,
        NavDestination.CUSTOMERS,
        NavDestination.TEMPLATES,
        NavDestination.HISTORY
    )

    // Back handler for sub-screens
    val onNavigateBack = {
        when (currentNav) {
            NavDestination.PROFILE -> currentNav = NavDestination.SETTINGS
            NavDestination.IMPORT -> currentNav = NavDestination.CUSTOMERS
            NavDestination.GROUPS -> currentNav = NavDestination.CUSTOMERS
            NavDestination.CUSTOMER_DETAILS -> currentNav = NavDestination.CUSTOMERS
            NavDestination.TEMPLATE_EDITOR -> currentNav = NavDestination.TEMPLATES
            NavDestination.CAMPAIGN_RECIPIENTS -> currentNav = NavDestination.CAMPAIGNS
            NavDestination.CAMPAIGN_REVIEW -> currentNav = NavDestination.CAMPAIGN_RECIPIENTS
            NavDestination.COMPOSER_HANDOFF -> currentNav = NavDestination.CAMPAIGN_REVIEW
            NavDestination.CAMPAIGN_SENDING -> currentNav = NavDestination.HISTORY
            NavDestination.SETTINGS -> currentNav = NavDestination.DASHBOARD
            else -> currentNav = NavDestination.DASHBOARD
        }
    }

    BackHandler(enabled = !isTopLevel) {
        onNavigateBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isTopLevel) "ApnaSandesh" else currentNav.label,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (isTopLevel) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = StatusSuccess.copy(alpha = 0.12f),
                                contentColor = StatusSuccess
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
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
                                painter = painterResource(id = R.drawable.ic_apnasandesh_mark),
                                contentDescription = "ApnaSandesh Logo",
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
                    onSaveProfileExtended = { name, owner, cat, phone, addr, code, sig, web, curr, lang, tone ->
                        dashboardViewModel.saveBusinessProfile(name, owner, cat, phone, addr, code, sig, web, curr, lang, tone)
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
                    onNavigateToGroups = { currentNav = NavDestination.GROUPS },
                    onCustomerClick = { cust ->
                        selectedCustomerId = cust.id
                        currentNav = NavDestination.CUSTOMER_DETAILS
                    }
                )
                NavDestination.CUSTOMER_DETAILS -> {
                    val activeCustomer = customers.find { it.id == selectedCustomerId }
                    CustomerDetailsScreen(
                        customer = activeCustomer,
                        onBack = { currentNav = NavDestination.CUSTOMERS },
                        onEditCustomer = { cust ->
                            currentNav = NavDestination.CUSTOMERS
                        },
                        onDeleteCustomer = { custId ->
                            customersViewModel.deleteCustomer(custId)
                            currentNav = NavDestination.CUSTOMERS
                        },
                        onToggleOptOut = { cust, reason ->
                            customersViewModel.recordOptOut(cust, reason)
                        },
                        onStartCampaignForCustomer = { cust ->
                            stagedTitle = "Direct SMS to ${cust.fullName}"
                            stagedBody = "Hello ${cust.fullName}, "
                            currentNav = NavDestination.CAMPAIGNS
                        }
                    )
                }
                NavDestination.IMPORT -> CustomerImportScreen(
                    preview = csvPreview,
                    deviceContacts = deviceContacts,
                    isLoadingDeviceContacts = isLoadingDeviceContacts,
                    onLoadDeviceContacts = { customersViewModel.loadDeviceContacts() },
                    onImportSelectedContacts = { customersViewModel.importSelectedDeviceContacts(it) },
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
                    },
                    onEditTemplate = { tpl ->
                        selectedTemplateForEditId = tpl?.id
                        currentNav = NavDestination.TEMPLATE_EDITOR
                    }
                )
                NavDestination.TEMPLATE_EDITOR -> {
                    val activeTemplate = templates.find { it.id == selectedTemplateForEditId }
                    TemplateEditorScreen(
                        template = activeTemplate,
                        profile = profile,
                        segmentCalculator = campaignsViewModel.segmentCalculator,
                        templateRenderer = campaignsViewModel.templateRenderer,
                        onBack = { currentNav = NavDestination.TEMPLATES },
                        onSaveTemplate = { tpl ->
                            templatesViewModel.saveTemplate(
                                id = tpl.id,
                                title = tpl.title,
                                category = tpl.category,
                                bodyTemplate = tpl.bodyTemplate,
                                description = tpl.description,
                                language = tpl.language,
                                tone = tpl.tone,
                                industry = tpl.industry,
                                isPromotional = tpl.isPromotional
                            )
                            currentNav = NavDestination.TEMPLATES
                        }
                    )
                }
                NavDestination.CAMPAIGNS -> {
                    val activeSims by campaignsViewModel.activeSimSubscriptions.collectAsStateWithLifecycle()
                    CampaignEditorScreen(
                        templates = templates,
                        profile = profile,
                        segmentCalculator = campaignsViewModel.segmentCalculator,
                        templateRenderer = campaignsViewModel.templateRenderer,
                        activeSims = activeSims,
                        selectedSimSubscriptionId = stagedSimId,
                        onSelectSim = { stagedSimId = it },
                        onContinueToRecipients = { title, tplId, body, appendSig, isPromo, channel, interval, simId ->
                            stagedTitle = title
                            stagedTemplateId = tplId
                            stagedBody = body
                            stagedAppendSig = appendSig
                            stagedIsPromo = isPromo
                            stagedChannel = channel
                            stagedIntervalSeconds = interval
                            stagedSimId = simId
                            currentNav = NavDestination.CAMPAIGN_RECIPIENTS
                        }
                    )
                }
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
                            transportType = stagedChannel,
                            selectedSubscriptionId = stagedSimId ?: appSettings?.selectedSimSubscriptionId,
                            selectedCustomerIds = selectedCustIds,
                            sendingIntervalSeconds = stagedIntervalSeconds,
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
                    transportType = stagedChannel,
                    sendingIntervalSeconds = stagedIntervalSeconds,
                    profile = profile,
                    recipients = activeRecipients,
                    traiAdvisor = campaignsViewModel.traiAdvisor,
                    segmentCalculator = campaignsViewModel.segmentCalculator,
                    onUpdateRecipientMessage = { id, text -> campaignsViewModel.updateRecipientMessage(id, text) },
                    onConfirmAndStartSending = { scheduledMillis ->
                        val activeId = campaignsViewModel.activeCampaignId.value
                        if (activeId != null) {
                            if (scheduledMillis != null) {
                                campaignsViewModel.scheduleCampaignWork(activeId, scheduledMillis)
                                currentNav = NavDestination.CAMPAIGNS
                            } else {
                                if (stagedChannel == "COMPOSER_HANDOFF") {
                                    currentNav = NavDestination.COMPOSER_HANDOFF
                                } else {
                                    campaignsViewModel.startSendingSession(activeId, stagedIntervalSeconds)
                                    currentNav = NavDestination.CAMPAIGN_SENDING
                                }
                            }
                        }
                    },
                    onBack = { currentNav = NavDestination.CAMPAIGN_RECIPIENTS }
                )
                NavDestination.COMPOSER_HANDOFF -> {
                    SmsComposerHandoffScreen(
                        campaignTitle = stagedTitle.ifBlank { "Composer Campaign" },
                        recipients = activeRecipients,
                        currentIndex = currentIndex,
                        onBack = {
                            currentNav = NavDestination.CAMPAIGN_REVIEW
                        },
                        onMarkSent = { recipient ->
                            campaignsViewModel.markCurrentRecipientSent(recipient)
                        },
                        onSkipRecipient = { recipient ->
                            campaignsViewModel.markCurrentRecipientSkipped(recipient)
                        },
                        onEditRecipientMessage = { id, text ->
                            campaignsViewModel.updateRecipientMessage(id, text)
                        },
                        onViewHistory = {
                            currentNav = NavDestination.HISTORY
                        }
                    )
                }
                NavDestination.CAMPAIGN_SENDING -> {
                    val activeId = campaignsViewModel.activeCampaignId.value
                    val engineState by campaignsViewModel.engineState.collectAsStateWithLifecycle()
                    CampaignSendingScreen(
                        campaignTitle = stagedTitle.ifBlank { engineState?.campaignName ?: "Active Campaign" },
                        campaignStatus = engineState?.status ?: "RUNNING",
                        transportType = engineState?.transportType ?: stagedChannel,
                        recipients = activeRecipients,
                        engineState = engineState,
                        statusMessage = campaignStatusMsg ?: engineState?.statusMessage,
                        segmentCalculator = campaignsViewModel.segmentCalculator,
                        onUpdateRecipientMessage = { id, text -> campaignsViewModel.updateRecipientMessage(id, text) },
                        onDismissStatus = { campaignsViewModel.dismissMessage() },
                        onStartOrResumeCampaign = {
                            if (activeId != null) campaignsViewModel.startSendingSession(activeId, stagedIntervalSeconds)
                        },
                        onPauseCampaign = {
                            if (activeId != null) campaignsViewModel.pauseCampaign(activeId)
                        },
                        onResumeCampaign = {
                            if (activeId != null) campaignsViewModel.resumeCampaign(activeId)
                        },
                        onStopCampaign = {
                            if (activeId != null) campaignsViewModel.stopCampaign(activeId)
                        },
                        onRetryFailed = {
                            if (activeId != null) campaignsViewModel.retryFailedRecipients(activeId)
                        },
                        onDispatchRecipient = { campaignsViewModel.dispatchCurrentRecipient(it) },
                        onMarkSent = { campaignsViewModel.markCurrentRecipientSent(it) },
                        onSkipRecipient = { campaignsViewModel.markCurrentRecipientSkipped(it) },
                        onDone = { currentNav = NavDestination.HISTORY }
                    )
                }
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
