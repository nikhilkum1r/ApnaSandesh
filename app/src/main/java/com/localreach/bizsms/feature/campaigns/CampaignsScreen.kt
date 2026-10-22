package com.localreach.bizsms.feature.campaigns

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localreach.bizsms.core.database.BusinessProfileEntity
import com.localreach.bizsms.core.database.CampaignRecipientEntity
import com.localreach.bizsms.core.database.CustomerEntity
import com.localreach.bizsms.core.database.CustomerGroupEntity
import com.localreach.bizsms.core.database.MessageTemplateEntity
import com.localreach.bizsms.core.ui.theme.*
import com.localreach.bizsms.domain.validation.SmsSegmentCalculator
import com.localreach.bizsms.domain.validation.TemplateRenderer
import com.localreach.bizsms.domain.validation.TraiDltComplianceAdvisor
import com.localreach.bizsms.feature.common.CustomerAvatar
import com.localreach.bizsms.feature.common.EmptyStateView
import com.localreach.bizsms.feature.common.SmsBubblePreview
import com.localreach.bizsms.feature.common.StatusBadge
import com.localreach.bizsms.feature.common.StepWizardBar
import com.localreach.bizsms.feature.common.TokenChipTray

/**
 * Screen 8: Campaign Message Editor Screen (Step 1)
 */
@Composable
fun CampaignEditorScreen(
    templates: List<MessageTemplateEntity>,
    profile: BusinessProfileEntity?,
    segmentCalculator: SmsSegmentCalculator,
    templateRenderer: TemplateRenderer,
    onContinueToRecipients: (String, String?, String, Boolean, Boolean) -> Unit
) {
    var campaignTitle by remember { mutableStateOf("Customer Service Broadcast") }
    var selectedTemplate by remember { mutableStateOf<MessageTemplateEntity?>(templates.firstOrNull()) }
    var messageBody by remember {
        mutableStateOf(
            templates.firstOrNull()?.bodyTemplate
                ?: "Hello {{customer_name}}, thank you for choosing {{business_name}}."
        )
    }
    var appendSignature by remember { mutableStateOf(true) }
    var isPromotional by remember { mutableStateOf(false) }

    val sampleCustomer = remember {
        CustomerEntity(
            fullName = "Aarav Sharma",
            rawPhoneNumber = "9876543210",
            normalizedPhoneE164 = "+919876543210"
        )
    }
    val safeProfile = profile ?: BusinessProfileEntity(
        businessName = "My Local Shop",
        ownerDisplayName = "Shop Owner",
        businessCategory = "Retail",
        communicationPhone = "+919876500000"
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

    val canProceed = campaignTitle.trim().isNotBlank() && messageBody.trim().isNotBlank()

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
            text = "Compose Campaign Message",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Campaign Name
        OutlinedTextField(
            value = campaignTitle,
            onValueChange = { campaignTitle = it },
            label = { Text("Campaign Name / Purpose *") },
            placeholder = { Text("e.g. Festival Offer / Weekend Service Notice") },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

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
            placeholder = { Text("Enter message text with variables like {{customer_name}}...") },
            shape = RoundedCornerShape(10.dp),
            minLines = 4,
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
            recipientName = "Aarav Sharma"
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
                        isPromotional
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
    var selectedGroupFilter by remember { mutableStateOf<String?>("ALL") }
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

    // Filter displayed list by search query
    val displayedCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.normalizedPhoneE164.contains(searchQuery) ||
                it.tagsCsv.contains(searchQuery, ignoreCase = true)
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
                    text = "Select Campaign Recipients",
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
                    Text("Suppressed / Ineligible", style = MaterialTheme.typography.labelSmall, color = StatusError)
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

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter recipients by name or phone...") },
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

        // Select All Eligible Master Control
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
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
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
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
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Select All ${eligibleCustomers.size} Eligible Customers",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Automatically excludes opted-out and invalid phone numbers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    profile: BusinessProfileEntity?,
    recipients: List<CampaignRecipientEntity>,
    traiAdvisor: TraiDltComplianceAdvisor,
    onConfirmAndStartSending: () -> Unit,
    onBack: () -> Unit
) {
    var confirmedCheckbox by remember { mutableStateOf(false) }

    val safeProfile = profile ?: BusinessProfileEntity(
        businessName = "My Local Shop",
        ownerDisplayName = "Shop Owner",
        businessCategory = "Retail",
        communicationPhone = "+919876500000"
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
                    text = "Pre-Flight Campaign Review",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Final verification before initiating dispatch session",
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
                    Text("Recipient Count", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${recipients.size} customer(s)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Est. Total SMS Segments", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalSegments segments", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = BrandAmberTertiaryLight)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dispatch Transport Mode", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("System SMS Composer", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
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

        // Sample Personalized Messages Preview
        Text(
            text = "Personalized Preview Samples (First ${minOf(3, recipients.size)} recipients)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        recipients.take(3).forEachIndexed { index, r ->
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
                    text = "I confirm that all recipients have an existing relationship with my business, and I will respect personal SIM daily quotas and telecom guidelines.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Confirm & Start Dispatch Button
        Button(
            onClick = onConfirmAndStartSending,
            enabled = confirmedCheckbox && recipients.isNotEmpty(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Sending Session (${recipients.size} Recipients)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Screen 11: Campaign Sending & Handoff Coordinator Screen (Step 4)
 */
@Composable
fun CampaignSendingScreen(
    campaignTitle: String,
    recipients: List<CampaignRecipientEntity>,
    currentIndex: Int,
    statusMessage: String?,
    onDismissStatus: () -> Unit,
    onDispatchRecipient: (CampaignRecipientEntity) -> Unit,
    onMarkSent: (CampaignRecipientEntity) -> Unit,
    onSkipRecipient: (CampaignRecipientEntity) -> Unit,
    onCancelCampaign: () -> Unit,
    onDone: () -> Unit
) {
    val currentRecipient = recipients.getOrNull(currentIndex)
    val sentCount = remember(recipients) { recipients.count { it.state == "SENT" || it.state == "DELIVERED" } }
    val failedCount = remember(recipients) { recipients.count { it.state == "FAILED" } }
    val cancelledCount = remember(recipients) { recipients.count { it.state == "CANCELLED" } }
    val isFinished = currentIndex >= recipients.size || currentRecipient == null

    var showCancelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Session Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dispatch Studio",
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
                color = if (isFinished) StatusSuccessContainer else BrandBluePrimaryContainerLight,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isFinished) "Completed" else "In Progress",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isFinished) StatusSuccess else BrandBluePrimaryLight,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Progress Section
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recipient ${currentIndex.coerceAtMost(recipients.size)} of ${recipients.size}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val percent = if (recipients.isNotEmpty()) (currentIndex.toFloat() / recipients.size * 100).toInt() else 100
                    Text(
                        text = "$percent%",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { if (recipients.isNotEmpty()) (currentIndex.toFloat() / recipients.size).coerceIn(0f, 1f) else 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Sent: $sentCount", style = MaterialTheme.typography.labelSmall, color = StatusSuccess, fontWeight = FontWeight.SemiBold)
                    Text("Skipped: $cancelledCount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Failed: $failedCount", style = MaterialTheme.typography.labelSmall, color = if (failedCount > 0) StatusError else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Status Feedback Banner
        statusMessage?.let { msg ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(msg, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onDismissStatus) { Text("OK") }
                }
            }
        }

        // Main Stage: Current Recipient or Finished View
        if (isFinished) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, StatusSuccess),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(StatusSuccess.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Campaign Session Finished!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All $sentCount message(s) have been processed. Detailed audit outcomes are recorded in History.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDone,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("View Campaign History", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            // Current Recipient Card
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
                        CustomerAvatar(name = currentRecipient.recipientNameSnapshot, size = 48)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentRecipient.recipientNameSnapshot,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = currentRecipient.normalizedPhoneSnapshot,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = currentRecipient.state)
                    }

                    // Message Content Box
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Personalized Message:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentRecipient.resolvedMessageBody,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Primary Action: Open SMS Composer
                    Button(
                        onClick = { onDispatchRecipient(currentRecipient) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open in SMS Messaging App",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Step advancement actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilledTonalButton(
                            onClick = { onMarkSent(currentRecipient) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Sent & Next", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { onSkipRecipient(currentRecipient) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Skip", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Pause / Cancel Campaign
            OutlinedButton(
                onClick = { showCancelDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancel Remaining Queue")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Cancel Remaining Queue?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to cancel the rest of this campaign? Already sent messages will remain logged, and unsent recipients will be marked as cancelled.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelCampaign()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Yes, Cancel Queue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Continue Campaign")
                }
            }
        )
    }
}
