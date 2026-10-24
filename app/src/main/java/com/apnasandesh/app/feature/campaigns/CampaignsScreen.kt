package com.apnasandesh.app.feature.campaigns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.pm.PackageManager
import android.os.Build
import android.Manifest
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnasandesh.app.core.database.BusinessProfileEntity
import com.apnasandesh.app.core.database.CampaignRecipientEntity
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.database.CustomerGroupEntity
import com.apnasandesh.app.core.database.MessageTemplateEntity
import com.apnasandesh.app.core.model.SimSubscriptionInfo
import com.apnasandesh.app.core.model.SmsEncoding
import com.apnasandesh.app.core.ui.theme.*
import com.apnasandesh.app.domain.campaign.LiveCampaignEngineState
import com.apnasandesh.app.domain.validation.SmsSegmentCalculator
import com.apnasandesh.app.domain.validation.TemplateRenderer
import com.apnasandesh.app.domain.validation.TraiDltComplianceAdvisor
import com.apnasandesh.app.feature.common.CustomerAvatar
import com.apnasandesh.app.feature.common.MetricStatCard
import com.apnasandesh.app.feature.common.SmsBubblePreview
import com.apnasandesh.app.feature.common.StatusBadge
import com.apnasandesh.app.feature.common.StepWizardBar
import com.apnasandesh.app.feature.common.TokenChipTray

/**
 * Screen 8: Campaign Message Editor Screen (Step 1)
 */
@Composable
fun CampaignEditorScreen(
    templates: List<MessageTemplateEntity>,
    profile: BusinessProfileEntity?,
    segmentCalculator: SmsSegmentCalculator,
    templateRenderer: TemplateRenderer,
    activeSims: List<SimSubscriptionInfo> = emptyList(),
    selectedSimSubscriptionId: Int? = null,
    initialTransportMode: String = "DIRECT_SIM",
    onSelectSim: ((Int?) -> Unit)? = null,
    onContinueToRecipients: (
        title: String,
        templateId: String?,
        messageBody: String,
        appendSignature: Boolean,
        isPromotional: Boolean,
        transportType: String,
        intervalSeconds: Int,
        simSubscriptionId: Int?
    ) -> Unit
) {
    var campaignTitle by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf<MessageTemplateEntity?>(null) }
    var messageBody by remember { mutableStateOf("") }
    var appendSignature by remember { mutableStateOf(true) }
    var isPromotional by remember { mutableStateOf(false) }

    // Channel Selection: DIRECT_SIM (Default for Direct SIM build), COMPOSER_HANDOFF
    var selectedChannel by remember { mutableStateOf(initialTransportMode) }
    // Sending interval in seconds (for sequential auto send)
    var sendingIntervalSeconds by remember { mutableIntStateOf(3) }
    var resolvedSimId by remember { mutableStateOf(selectedSimSubscriptionId ?: activeSims.firstOrNull()?.subscriptionId) }

    LaunchedEffect(activeSims, selectedSimSubscriptionId) {
        if (resolvedSimId == null && activeSims.isNotEmpty()) {
            val subId = selectedSimSubscriptionId ?: activeSims.first().subscriptionId
            resolvedSimId = subId
            onSelectSim?.invoke(subId)
        }
    }

    val context = LocalContext.current
    val isDirectSimDeclared = remember {
        runCatching {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            }
            packageInfo.requestedPermissions?.contains(Manifest.permission.SEND_SMS) == true
        }.getOrDefault(false)
    }
    val directSimPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onSelectSim?.invoke(resolvedSimId)
    }

    val sampleCustomer = remember {
        CustomerEntity(
            fullName = "Customer Name",
            rawPhoneNumber = "",
            normalizedPhoneE164 = ""
        )
    }
    val safeProfile = profile ?: BusinessProfileEntity(
        businessName = "Your Business",
        ownerDisplayName = "Your Name",
        businessCategory = "Retail",
        communicationPhone = ""
    )

    val livePreview = remember(messageBody, safeProfile, appendSignature, isPromotional) {
        templateRenderer.render(
            rawTemplate = messageBody,
            customer = sampleCustomer,
            profile = safeProfile,
            appendSignature = appendSignature,
            appendOptOutFooter = isPromotional
        )
    }
    val metrics = remember(livePreview) { segmentCalculator.calculate(livePreview) }

    val unknownTokens = remember(messageBody) { templateRenderer.validateTemplateTokens(messageBody) }
    val canProceed = campaignTitle.trim().isNotBlank() && messageBody.trim().isNotBlank() && unknownTokens.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Wizard Progress Indicator
        StepWizardBar(
            steps = listOf("1. Compose", "2. Recipients", "3. Review"),
            currentStepIndex = 0
        )

        Text(
            text = "Compose Campaign",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Campaign Name
        OutlinedTextField(
            value = campaignTitle,
            onValueChange = { campaignTitle = it },
            label = { Text("Campaign Name *") },
            placeholder = { Text("e.g. Festival Special Offer / VIP Notice") },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        // Channel Selection Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Messaging Channel",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Direct SIM (Automatic sequential dispatch)
                    ChannelChoiceCard(
                        title = "SMS (Direct SIM)",
                        subtitle = "Automatic sequential sending",
                        icon = Icons.Default.SimCard,
                        isSelected = selectedChannel == "DIRECT_SIM",
                        onClick = {
                            selectedChannel = "DIRECT_SIM"
                            val hasSend = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                            val hasPhone = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            if (!hasSend || !hasPhone) {
                                directSimPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE)
                                )
                            }
                        },
                        modifier = Modifier.width(160.dp)
                    )

                    // Default SMS App (Play Store Compliant & Handoff)
                    ChannelChoiceCard(
                        title = "SMS (Default App)",
                        subtitle = "Native composer handoff",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = selectedChannel == "COMPOSER_HANDOFF",
                        onClick = { selectedChannel = "COMPOSER_HANDOFF" },
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }

        // Send Settings Card (Sending Interval & SIM)
        if (selectedChannel == "DIRECT_SIM") {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Send Settings & Pacing",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Sequential sending interval between messages:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2 to "2s Fast", 3 to "3s Standard", 5 to "5s Safe", 10 to "10s Steady").forEach { (sec, label) ->
                            FilterChip(
                                selected = sendingIntervalSeconds == sec,
                                onClick = { sendingIntervalSeconds = sec },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Text(
                        text = "A 3-5s interval ensures safe carrier rate limits and avoids telecom spam flags.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val hasSendSms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                    val hasPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

                    if (!hasSendSms) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "SMS Permission Required",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Direct SIM requires SEND_SMS permission to dispatch messages automatically.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Button(
                                    onClick = {
                                        directSimPermissionLauncher.launch(
                                            arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Grant", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    // Dual SIM selection
                    if (activeSims.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text(
                            text = "Dispatch SIM Card:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeSims.forEach { sim ->
                                val isSelected = resolvedSimId == sim.subscriptionId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        resolvedSimId = sim.subscriptionId
                                        onSelectSim?.invoke(sim.subscriptionId)
                                    },
                                    label = {
                                        Text(
                                            "${sim.displayName} (${sim.carrierName})",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    } else if (!hasPhoneState) {
                        OutlinedButton(
                            onClick = {
                                directSimPermissionLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE))
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Detect Installed Dual SIM Slots", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Text(
                            text = "Using default cellular SIM connection for dispatch.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Template Selection Scrollable Row
        if (templates.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Load from Pre-made Template:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    templates.forEach { tpl ->
                        val isSelected = selectedTemplate?.id == tpl.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTemplate = tpl
                                messageBody = tpl.bodyTemplate
                                isPromotional = tpl.isPromotional
                            },
                            label = { Text(tpl.title, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Message Body Editor
        OutlinedTextField(
            value = messageBody,
            onValueChange = { messageBody = it },
            label = { Text("Message Body Template *") },
            placeholder = { Text("Enter message text with variables like {{first_name}} or {{customer_name}}...") },
            shape = RoundedCornerShape(10.dp),
            minLines = 4,
            isError = unknownTokens.isNotEmpty(),
            supportingText = {
                if (unknownTokens.isNotEmpty()) {
                    Text(
                        text = "Unsupported variable token(s): ${unknownTokens.joinToString()}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Variable Token Insertion Tray
        TokenChipTray(
            onTokenSelected = { token ->
                messageBody = if (messageBody.endsWith(" ") || messageBody.isEmpty()) "$messageBody$token " else "$messageBody $token "
            }
        )

        // Toggles Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Append Business Signature",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatically appends '${safeProfile.defaultMessageSignature}'",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = appendSignature,
                        onCheckedChange = { appendSignature = it }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Promotional Broadcast (+ STOP opt-out)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Appends opt-out instruction and filters only explicit Opted-In customers.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPromotional,
                        onCheckedChange = { isPromotional = it }
                    )
                }
            }
        }

        // Live Personalized SMS Chat Preview
        SmsBubblePreview(
            messageText = livePreview,
            encoding = if (metrics.isUcs2Unicode) "UCS-2 (Unicode)" else "GSM-7",
            charCount = metrics.charCount,
            segmentCount = metrics.segmentCount,
            recipientName = "Customer"
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Next Action Button
        Button(
            onClick = {
                if (canProceed) {
                    onContinueToRecipients(
                        campaignTitle,
                        selectedTemplate?.id,
                        messageBody,
                        appendSignature,
                        isPromotional,
                        selectedChannel,
                        sendingIntervalSeconds,
                        resolvedSimId
                    )
                }
            },
            enabled = canProceed,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Next: Select Recipients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ChannelChoiceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Screen 9: Recipient Selection Screen (Step 2)
 */
@Composable
fun RecipientSelectionScreen(
    customers: List<CustomerEntity>,
    groups: List<CustomerGroupEntity>,
    isPromotional: Boolean,
    onContinueToReview: (Set<String>?) -> Unit,
    onBack: () -> Unit
) {
    var selectAllEligible by remember { mutableStateOf(true) }
    var selectedGroupFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    val selectedCustomerIds = remember { mutableStateListOf<String>() }

    val eligibleCustomers = remember(customers, isPromotional) {
        customers.filter {
            !it.isOptedOut && it.consentStatus != "OPTED_OUT" && it.isPhoneValid &&
                (!isPromotional || it.consentStatus == "GRANTED")
        }
    }

    val excludedCustomers = remember(customers, isPromotional) {
        customers.filter {
            it.isOptedOut || it.consentStatus == "OPTED_OUT" || !it.isPhoneValid ||
                (isPromotional && it.consentStatus != "GRANTED")
        }
    }

    val duplicatePhones = remember(customers) {
        customers.groupBy { it.normalizedPhoneE164 }
            .filter { it.key.isNotBlank() && it.value.size > 1 }
    }

    // Filter displayed list by search query and group filter
    val displayedCustomers = remember(customers, searchQuery, selectedGroupFilter) {
        customers.filter { cust ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                cust.fullName.contains(searchQuery, ignoreCase = true) ||
                    cust.normalizedPhoneE164.contains(searchQuery) ||
                    cust.tagsCsv.contains(searchQuery, ignoreCase = true)
            }
            val matchesGroup = if (selectedGroupFilter == "ALL") true else {
                cust.tagsCsv.contains(selectedGroupFilter, ignoreCase = true)
            }
            matchesSearch && matchesGroup
        }
    }

    val selectedCount = if (selectAllEligible) eligibleCustomers.size else selectedCustomerIds.size
    val canProceed = selectedCount > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wizard Progress Indicator
        StepWizardBar(
            steps = listOf("1. Compose", "2. Recipients", "3. Review"),
            currentStepIndex = 1
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Select Campaign Clients",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$selectedCount recipients chosen for dispatch",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Eligible", style = MaterialTheme.typography.labelSmall, color = StatusSuccess)
                    Text("${eligibleCustomers.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusSuccess)
                }
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ineligible / Skipped", style = MaterialTheme.typography.labelSmall, color = StatusError)
                    Text("${excludedCustomers.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusError)
                }
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Selected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("$selectedCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Duplicate phone numbers warning
        if (duplicatePhones.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BrandAmberTertiaryLight.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, BrandAmberTertiaryLight.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = BrandAmberTertiaryLight, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${duplicatePhones.size} Duplicate Phone Number(s) Detected",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandAmberTertiaryLight
                        )
                        Text(
                            text = "Multiple customers share the same number. Each number will be messaged with their respective personalized record.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, phone, or tags...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Customer Group Filter Chips
        if (groups.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedGroupFilter == "ALL",
                    onClick = { selectedGroupFilter = "ALL" },
                    label = { Text("All (${customers.size})", style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(8.dp)
                )
                groups.forEach { grp ->
                    val isSelected = selectedGroupFilter == grp.name
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGroupFilter = if (isSelected) "ALL" else grp.name },
                        label = { Text(grp.name, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Master Selection Bar: Select All / Deselect All
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val newCheck = !selectAllEligible
                        selectAllEligible = newCheck
                        if (newCheck) {
                            selectedCustomerIds.clear()
                        } else {
                            selectedCustomerIds.clear()
                            selectedCustomerIds.addAll(eligibleCustomers.map { it.id })
                        }
                    }
                ) {
                    Checkbox(
                        checked = selectAllEligible,
                        onCheckedChange = { checked ->
                            selectAllEligible = checked
                            if (checked) {
                                selectedCustomerIds.clear()
                            } else {
                                selectedCustomerIds.clear()
                                selectedCustomerIds.addAll(eligibleCustomers.map { it.id })
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Select All ${eligibleCustomers.size} Eligible",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!selectAllEligible) {
                    TextButton(
                        onClick = { selectedCustomerIds.clear() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Deselect All", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Recipient Selection List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(displayedCustomers, key = { it.id }) { customer ->
                val isEligible = customer in eligibleCustomers
                val isChecked = selectAllEligible || customer.id in selectedCustomerIds

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEligible) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isChecked && isEligible) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEligible) {
                            Checkbox(
                                checked = isChecked,
                                enabled = !selectAllEligible,
                                onCheckedChange = { checked ->
                                    if (checked) selectedCustomerIds.add(customer.id)
                                    else selectedCustomerIds.remove(customer.id)
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(StatusError.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Ineligible",
                                    tint = StatusError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        CustomerAvatar(name = customer.fullName, size = 36)

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customer.fullName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = customer.normalizedPhoneE164,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!isEligible) {
                                val reason = when {
                                    customer.isOptedOut || customer.consentStatus == "OPTED_OUT" -> "Opted-out (Suppressed)"
                                    !customer.isPhoneValid -> "Invalid phone format"
                                    isPromotional && customer.consentStatus != "GRANTED" -> "No promotional opt-in"
                                    else -> "Ineligible"
                                }
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusError,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (!isEligible) {
                            StatusBadge(
                                status = when {
                                    customer.isOptedOut -> "OPTED_OUT"
                                    !customer.isPhoneValid -> "INVALID"
                                    isPromotional && customer.consentStatus != "GRANTED" -> "NOT OPTED IN"
                                    else -> "SUPPRESSED"
                                }
                            )
                        } else {
                            StatusBadge(status = customer.consentStatus)
                        }
                    }
                }
            }
        }

        // Continue Button
        Button(
            onClick = {
                val targets = if (selectAllEligible) null else selectedCustomerIds.toSet()
                onContinueToReview(targets)
            },
            enabled = canProceed,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Review Campaign ($selectedCount Recipients)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * Screen 10: Campaign Review & Confirmation Screen (Step 3)
 */
@Composable
fun CampaignReviewScreen(
    campaignTitle: String,
    rawTemplate: String,
    appendSignature: Boolean,
    isPromotional: Boolean,
    transportType: String,
    sendingIntervalSeconds: Int,
    profile: BusinessProfileEntity?,
    recipients: List<CampaignRecipientEntity>,
    traiAdvisor: TraiDltComplianceAdvisor,
    segmentCalculator: SmsSegmentCalculator? = null,
    onUpdateRecipientMessage: ((String, String) -> Unit)? = null,
    onConfirmAndStartSending: (Long?) -> Unit,
    onBack: () -> Unit
) {
    var confirmedCheckbox by remember { mutableStateOf(false) }
    var editingRecipient by remember { mutableStateOf<CampaignRecipientEntity?>(null) }
    var showAllRecipients by remember { mutableStateOf(false) }
    var isScheduled by remember { mutableStateOf(false) }
    var scheduleOption by remember { mutableStateOf("1_HOUR") }

    val scheduledAtMillis = remember(isScheduled, scheduleOption) {
        if (!isScheduled) null
        else {
            val now = System.currentTimeMillis()
            when (scheduleOption) {
                "1_HOUR" -> now + (60 * 60 * 1000L)
                "TOMORROW_10AM" -> {
                    val cal = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, 1)
                        set(java.util.Calendar.HOUR_OF_DAY, 10)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                    }
                    cal.timeInMillis
                }
                "TOMORROW_6PM" -> {
                    val cal = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, 1)
                        set(java.util.Calendar.HOUR_OF_DAY, 18)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                    }
                    cal.timeInMillis
                }
                else -> now + (60 * 60 * 1000L)
            }
        }
    }

    val safeCalculator = remember(segmentCalculator) { segmentCalculator ?: SmsSegmentCalculator() }

    val safeProfile = profile ?: BusinessProfileEntity(
        businessName = "Your Business",
        ownerDisplayName = "Your Name",
        businessCategory = "Retail",
        communicationPhone = ""
    )

    val advisory = remember(recipients.size, isPromotional, safeProfile.defaultCountryCallingCode) {
        traiAdvisor.evaluateCampaign(
            recipientCount = recipients.size,
            isPromotionalCategory = isPromotional,
            countryCode = safeProfile.defaultCountryCallingCode
        )
    }

    val totalSegments = remember(recipients) { recipients.sumOf { it.estimatedSegments } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Wizard Progress Indicator
        StepWizardBar(
            steps = listOf("1. Compose", "2. Recipients", "3. Review"),
            currentStepIndex = 2
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Pre-Flight Review",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Review campaign parameters before dispatch",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Campaign Overview Card
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
                    text = "Campaign Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Campaign Name", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(campaignTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Channel Mode", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val channelLabel = when (transportType) {
                        "DIRECT_SIM" -> "Direct SIM (Auto Sequential)"
                        else -> "Default SMS App (Composer)"
                    }
                    Text(channelLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }

                if (transportType == "DIRECT_SIM") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sending Interval", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$sendingIntervalSeconds seconds / message", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recipient Count", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${recipients.size} customer(s)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Est. SMS Segments", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalSegments segments", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = BrandAmberTertiaryLight)
                }
            }
        }

        // Regulatory / Telecom Advisory Card
        if (advisory.requiresWarningBanner) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BrandAmberTertiaryLight.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, BrandAmberTertiaryLight.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = BrandAmberTertiaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = advisory.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = BrandAmberTertiaryLight
                        )
                    }
                    advisory.guidanceBullets.forEach { bullet ->
                        Text(
                            text = "• $bullet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Personalized Messages Preview Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Individually Rendered Messages (${recipients.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            if (recipients.size > 5) {
                TextButton(onClick = { showAllRecipients = !showAllRecipients }) {
                    Text(if (showAllRecipients) "Show First 5" else "Show All (${recipients.size})")
                }
            }
        }

        val displayedRecipients = if (showAllRecipients) recipients else recipients.take(5)

        displayedRecipients.forEachIndexed { index, r ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "#${index + 1}: ${r.recipientNameSnapshot}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = r.normalizedPhoneSnapshot,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (onUpdateRecipientMessage != null) {
                            OutlinedButton(
                                onClick = { editingRecipient = r },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Message", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = r.resolvedMessageBody,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${r.characterCount} chars • ${r.estimatedSegments} segment(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = r.encodingType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Confirmation Checkbox Box
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { confirmedCheckbox = !confirmedCheckbox }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = confirmedCheckbox,
                    onCheckedChange = { confirmedCheckbox = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I confirm that all selected recipients have an existing relationship with my business, and I will respect personal SIM daily quotas and telecom guidelines.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Delivery Timing Card
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
                    text = "Delivery Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isScheduled,
                        onClick = { isScheduled = false },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Send Now") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = isScheduled,
                        onClick = { isScheduled = true },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Schedule for Later") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                if (isScheduled) {
                    Text(
                        text = "Choose send timing:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "1_HOUR" to "In 1 Hour",
                            "TOMORROW_10AM" to "Tomorrow 10:00 AM",
                            "TOMORROW_6PM" to "Tomorrow 6:00 PM"
                        ).forEach { (optKey, optLabel) ->
                            FilterChip(
                                selected = scheduleOption == optKey,
                                onClick = { scheduleOption = optKey },
                                label = { Text(optLabel) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Confirm & Start / Schedule Dispatch Button
        Button(
            onClick = { onConfirmAndStartSending(scheduledAtMillis) },
            enabled = confirmedCheckbox && recipients.isNotEmpty(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                if (isScheduled) Icons.Default.Schedule else Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isScheduled) {
                    when (scheduleOption) {
                        "1_HOUR" -> "Schedule for +1 Hour (${recipients.size} Recipients)"
                        "TOMORROW_10AM" -> "Schedule for Tomorrow 10 AM (${recipients.size} Recipients)"
                        else -> "Schedule Campaign (${recipients.size} Recipients)"
                    }
                } else {
                    "Start Campaign (${recipients.size} Recipients)"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (editingRecipient != null) {
        EditRecipientMessageDialog(
            recipient = editingRecipient!!,
            segmentCalculator = safeCalculator,
            onDismiss = { editingRecipient = null },
            onSave = { updatedText ->
                onUpdateRecipientMessage?.invoke(editingRecipient!!.id, updatedText)
                editingRecipient = null
            }
        )
    }
}

/**
 * Screen 11: Live Campaign Dashboard & Sending Studio (Step 4)
 */
@Composable
fun CampaignSendingScreen(
    campaignTitle: String,
    campaignStatus: String,
    transportType: String,
    recipients: List<CampaignRecipientEntity>,
    engineState: LiveCampaignEngineState?,
    statusMessage: String?,
    segmentCalculator: SmsSegmentCalculator? = null,
    onUpdateRecipientMessage: ((String, String) -> Unit)? = null,
    onDismissStatus: () -> Unit,
    onStartOrResumeCampaign: () -> Unit,
    onPauseCampaign: () -> Unit,
    onResumeCampaign: () -> Unit,
    onStopCampaign: () -> Unit,
    onRetryFailed: () -> Unit,
    onDispatchRecipient: (CampaignRecipientEntity) -> Unit,
    onMarkSent: (CampaignRecipientEntity) -> Unit,
    onSkipRecipient: (CampaignRecipientEntity) -> Unit,
    onDone: () -> Unit
) {
    var showStopDialog by remember { mutableStateOf(false) }
    var editingRecipient by remember { mutableStateOf<CampaignRecipientEntity?>(null) }
    val safeCalculator = remember(segmentCalculator) { segmentCalculator ?: SmsSegmentCalculator() }
    val sendingContext = LocalContext.current
    val sendingPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val sendGranted = grants[Manifest.permission.SEND_SMS] == true ||
            ContextCompat.checkSelfPermission(sendingContext, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        if (sendGranted) {
            onDismissStatus()
            onResumeCampaign()
        }
    }

    // Live counts from engineState if available, fallback to recipients list
    val totalCount = engineState?.totalRecipients?.takeIf { it > 0 } ?: recipients.size
    val sentCount = engineState?.sentCount ?: recipients.count { it.state == "SENT" || it.state == "DELIVERED" }
    val deliveredCount = engineState?.deliveredCount ?: recipients.count { it.state == "DELIVERED" }
    val failedCount = engineState?.failedCount ?: recipients.count { it.state == "FAILED" }
    val skippedCount = engineState?.skippedCount ?: recipients.count { it.state == "CANCELLED" || it.state == "SUPPRESSED_OPT_OUT" }
    val processedCount = engineState?.processedCount ?: (sentCount + failedCount + skippedCount)
    val remainingCount = engineState?.remainingCount ?: (totalCount - processedCount).coerceAtLeast(0)
    val progressPercent = engineState?.progressPercentage ?: if (totalCount > 0) ((processedCount.toFloat() / totalCount) * 100).toInt().coerceIn(0, 100) else 100

    val currentStatus = engineState?.status ?: campaignStatus
    val isRunning = currentStatus in setOf("RUNNING", "IN_PROGRESS")
    val isPaused = currentStatus == "PAUSED"
    val isStopped = currentStatus == "STOPPED"
    val isCompleted = currentStatus in setOf("COMPLETED", "COMPLETED_WITH_ERRORS")
    val isFinished = isCompleted || isStopped || currentStatus == "FAILED"

    // Active pending recipient for current stage
    val currentRecipient = recipients.firstOrNull { it.state in setOf("QUEUED", "SUBMITTED", "DRAFT", "VALIDATED", "AWAITING_CONFIRMATION", "HANDED_OFF_TO_MESSAGING_APP") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Session Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Campaign Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = campaignTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = when {
                        isRunning -> StatusSuccessContainer
                        isPaused -> BrandAmberTertiaryLight.copy(alpha = 0.2f)
                        isFinished -> StatusSuccessContainer
                        else -> BrandBluePrimaryContainerLight
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            isRunning -> "Running"
                            isPaused -> "Paused"
                            currentStatus == "COMPLETED_WITH_ERRORS" -> "Completed with Errors"
                            isCompleted -> "Completed"
                            isStopped -> "Stopped"
                            currentStatus == "FAILED" -> "Failed"
                            else -> currentStatus.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isRunning -> StatusSuccess
                            isPaused -> BrandAmberTertiaryLight
                            isFinished -> StatusSuccess
                            else -> BrandBluePrimaryLight
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Live Progress Card
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Processed $processedCount of $totalCount Clients",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "$progressPercent%",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (progressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                }
            }

            // 7 Live Metric Stat Cards Grid
            Text(
                text = "Live Dispatch Metrics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatCard(
                    label = "Total",
                    value = "$totalCount",
                    subtext = "Selected",
                    modifier = Modifier.width(96.dp)
                )
                MetricStatCard(
                    label = "Processed",
                    value = "$processedCount",
                    subtext = "Done",
                    modifier = Modifier.width(96.dp)
                )
                MetricStatCard(
                    label = "Sent",
                    value = "$sentCount",
                    subtext = "Accepted",
                    accentColor = StatusSuccess,
                    modifier = Modifier.width(96.dp)
                )
                MetricStatCard(
                    label = "Delivered",
                    value = "$deliveredCount",
                    subtext = "Receipts",
                    accentColor = StatusSuccess,
                    modifier = Modifier.width(96.dp)
                )
                MetricStatCard(
                    label = "Failed",
                    value = "$failedCount",
                    subtext = "Errors",
                    accentColor = if (failedCount > 0) StatusError else null,
                    modifier = Modifier.width(96.dp)
                )
                MetricStatCard(
                    label = "Skipped",
                    value = "$skippedCount",
                    subtext = "Opt-outs",
                    modifier = Modifier.width(96.dp)
                )
                MetricStatCard(
                    label = "Remaining",
                    value = "$remainingCount",
                    subtext = "In queue",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(96.dp)
                )
            }

            // Status Feedback Alert Banner
            statusMessage?.let { msg ->
                val isPermissionIssue = msg.contains("permission", ignoreCase = true) || msg.contains("SEND_SMS", ignoreCase = true)
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPermissionIssue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPermissionIssue) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isPermissionIssue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPermissionIssue) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (isPermissionIssue) {
                            Button(
                                onClick = {
                                    sendingPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Grant & Resume", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            TextButton(onClick = onDismissStatus) { Text("OK") }
                        }
                    }
                }
            }

            // Current Recipient Stage or Finished View
            if (isFinished) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, if (failedCount == 0) StatusSuccess else BrandAmberTertiaryLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(if (failedCount == 0) StatusSuccess.copy(alpha = 0.12f) else BrandAmberTertiaryLight.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (failedCount == 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (failedCount == 0) StatusSuccess else BrandAmberTertiaryLight,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isStopped) "Campaign Stopped" else if (failedCount > 0) "Completed with Errors" else "Campaign Completed Successfully!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$sentCount sent successfully, $failedCount failed, $skippedCount skipped. Delivery events are logged.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (currentRecipient != null) {
                // Live Stage: Current Client
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomerAvatar(name = currentRecipient.recipientNameSnapshot, size = 44)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentRecipient.recipientNameSnapshot,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = currentRecipient.normalizedPhoneSnapshot,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = currentRecipient.state)
                        }

                        // Message Content Box
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Personalized Message",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (onUpdateRecipientMessage != null) {
                                        TextButton(
                                            onClick = { editingRecipient = currentRecipient },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentRecipient.resolvedMessageBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // Manual handoff action buttons if in manual mode
                        if (transportType == "COMPOSER_HANDOFF") {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )

                            Text(
                                text = "Manual Dispatch Actions",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = {
                                    onDispatchRecipient(currentRecipient)
                                    onMarkSent(currentRecipient)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send via SMS App & Next",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { onDispatchRecipient(currentRecipient) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Only", fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }

                                Button(
                                    onClick = { onMarkSent(currentRecipient) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StatusSuccess,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Sent", fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = { onSkipRecipient(currentRecipient) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .height(42.dp)
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Skip", fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // Real-time Recipient Audit Feed
            Text(
                text = "Recipients Delivery Audit (${recipients.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recipients.forEachIndexed { idx, r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${idx + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(28.dp)
                            )

                            CustomerAvatar(name = r.recipientNameSnapshot, size = 30)

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = r.recipientNameSnapshot,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = r.normalizedPhoneSnapshot,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!r.lastErrorMessage.isNullOrBlank()) {
                                    Text(
                                        text = r.lastErrorMessage,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = if (r.state == "FAILED") StatusError else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            StatusBadge(status = r.state)
                        }

                        if (idx < recipients.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Dedicated Bottom Sticky Controls Surface
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRunning) {
                    Button(
                        onClick = onPauseCampaign,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandAmberTertiaryLight,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause", fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { showStopDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop", fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                } else if (isPaused) {
                    Button(
                        onClick = onResumeCampaign,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusSuccess,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resume", fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { showStopDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop", fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                } else if (isFinished) {
                    if (failedCount > 0) {
                        OutlinedButton(
                            onClick = onDone,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("History", fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }

                        Button(
                            onClick = onRetryFailed,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry ($failedCount)", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    } else {
                        Button(
                            onClick = onDone,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Done • View History", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                } else {
                    Button(
                        onClick = onStartOrResumeCampaign,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Sending Campaign", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Stop Running Campaign?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to stop this campaign? Any already sent messages will remain logged, and unsent recipients in the queue will be marked as cancelled.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopDialog = false
                        onStopCampaign()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Yes, Stop Campaign")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("Keep Running")
                }
            }
        )
    }

    if (editingRecipient != null) {
        EditRecipientMessageDialog(
            recipient = editingRecipient!!,
            segmentCalculator = safeCalculator,
            onDismiss = { editingRecipient = null },
            onSave = { updatedText ->
                onUpdateRecipientMessage?.invoke(editingRecipient!!.id, updatedText)
                editingRecipient = null
            }
        )
    }
}

/**
 * Dialog enabling a user to independently customize the rendered message for a single recipient.
 * Live SMS segments and character encoding limits are calculated as the user types.
 */
@Composable
fun EditRecipientMessageDialog(
    recipient: CampaignRecipientEntity,
    segmentCalculator: SmsSegmentCalculator,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var messageText by remember(recipient.resolvedMessageBody) { mutableStateOf(recipient.resolvedMessageBody) }
    val calc = remember(messageText) { segmentCalculator.calculate(messageText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Edit Recipient Message",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${recipient.recipientNameSnapshot} (${recipient.normalizedPhoneSnapshot})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Personalized Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${calc.charCount} chars • ${calc.segmentCount} segment(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = calc.encoding.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (calc.segmentCount > 1) {
                    Text(
                        text = "Multi-part SMS: each segment carries up to ${if (calc.encoding == SmsEncoding.GSM_7BIT) 153 else 67} characters.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = BrandAmberTertiaryLight
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(messageText) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Message")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

