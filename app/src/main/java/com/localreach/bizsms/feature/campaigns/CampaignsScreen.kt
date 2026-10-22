package com.localreach.bizsms.feature.campaigns

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.localreach.bizsms.core.database.BusinessProfileEntity
import com.localreach.bizsms.core.database.CampaignRecipientEntity
import com.localreach.bizsms.core.database.CustomerEntity
import com.localreach.bizsms.core.database.CustomerGroupEntity
import com.localreach.bizsms.core.database.MessageTemplateEntity
import com.localreach.bizsms.domain.validation.SmsSegmentCalculator
import com.localreach.bizsms.domain.validation.TemplateRenderer
import com.localreach.bizsms.domain.validation.TraiDltComplianceAdvisor
import com.localreach.bizsms.feature.common.StatusBadge

/**
 * Screen 8: Campaign Editor Screen
 */
@Composable
fun CampaignEditorScreen(
    templates: List<MessageTemplateEntity>,
    profile: BusinessProfileEntity?,
    segmentCalculator: SmsSegmentCalculator,
    templateRenderer: TemplateRenderer,
    onContinueToRecipients: (String, String?, String, Boolean, Boolean) -> Unit
) {
    var campaignTitle by remember { mutableStateOf("Service Follow-Up") }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Campaign Message Editor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = campaignTitle,
            onValueChange = { campaignTitle = it },
            label = { Text("Campaign Name / Title *") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Select Template (or customize body below):", style = MaterialTheme.typography.labelMedium)
        ScrollableTabRow(
            selectedTabIndex = templates.indexOf(selectedTemplate).coerceAtLeast(0),
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            templates.forEach { tpl ->
                Tab(
                    selected = selectedTemplate?.id == tpl.id,
                    onClick = {
                        selectedTemplate = tpl
                        messageBody = tpl.bodyTemplate
                        isPromotional = tpl.isPromotional
                    },
                    text = { Text(tpl.title) }
                )
            }
        }

        OutlinedTextField(
            value = messageBody,
            onValueChange = { messageBody = it },
            label = { Text("Message Body Template *") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Insert Safe Placeholder Token:", style = MaterialTheme.typography.labelSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SuggestionChip(onClick = { messageBody += "{{customer_name}}" }, label = { Text("Customer Name") })
            SuggestionChip(onClick = { messageBody += "{{business_name}}" }, label = { Text("Business") })
            SuggestionChip(onClick = { messageBody += "{{business_phone}}" }, label = { Text("Phone") })
            SuggestionChip(onClick = { messageBody += "{{signature}}" }, label = { Text("Signature") })
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = appendSignature,
                onClick = { appendSignature = !appendSignature },
                label = { Text("Append Signature") }
            )
            FilterChip(
                selected = isPromotional,
                onClick = { isPromotional = !isPromotional },
                label = { Text("Promotional (+ STOP Footer)") }
            )
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Live Personalized Preview:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text(livePreview, style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
                Text(
                    "Encoding: ${if (metrics.isUcs2Unicode) "UCS-2 (Unicode)" else "GSM-7 (Standard)"} • Length: ${metrics.charCount} chars • Est. Segments: ${metrics.segmentCount} per SMS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Button(
            onClick = {
                if (campaignTitle.isNotBlank() && messageBody.isNotBlank()) {
                    onContinueToRecipients(
                        campaignTitle,
                        selectedTemplate?.id,
                        messageBody,
                        appendSignature,
                        isPromotional
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Next: Select Recipients")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

/**
 * Screen 9: Recipient Selection Screen
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
    var selectedGroup by remember { mutableStateOf<String?>("ALL") }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Select Recipients", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Total Eligible Recipients: ${eligibleCustomers.size} • Excluded / Suppressed: ${excludedCustomers.size}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isPromotional) {
                    Text(
                        "Promotional campaign: Only customers with explicit OPTED-IN consent are eligible.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = selectAllEligible,
                onCheckedChange = {
                    selectAllEligible = it
                    if (it) {
                        selectedCustomerIds.clear()
                    } else {
                        selectedCustomerIds.addAll(eligibleCustomers.map { c -> c.id })
                    }
                }
            )
            Text("Select All ${eligibleCustomers.size} Eligible Customers", fontWeight = FontWeight.Medium)
        }

        Text("Recipient Directory (${if (selectAllEligible) eligibleCustomers.size else selectedCustomerIds.size} Selected)", fontWeight = FontWeight.SemiBold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            items(customers, key = { it.id }) { customer ->
                val isEligible = customer in eligibleCustomers
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEligible) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEligible) {
                            Checkbox(
                                checked = selectAllEligible || customer.id in selectedCustomerIds,
                                enabled = !selectAllEligible,
                                onCheckedChange = { checked ->
                                    if (checked) selectedCustomerIds.add(customer.id)
                                    else selectedCustomerIds.remove(customer.id)
                                }
                            )
                        } else {
                            Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(customer.fullName, fontWeight = FontWeight.Bold)
                            Text(customer.normalizedPhoneE164, style = MaterialTheme.typography.bodySmall)
                        }
                        if (!isEligible) {
                            StatusBadge(status = if (customer.isOptedOut) "OPTED_OUT" else if (!customer.isPhoneValid) "INVALID" else "NOT OPTED IN")
                        } else {
                            StatusBadge(status = customer.consentStatus)
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val targets = if (selectAllEligible) null else selectedCustomerIds.toSet()
                onContinueToReview(targets)
            },
            enabled = (selectAllEligible && eligibleCustomers.isNotEmpty()) || (!selectAllEligible && selectedCustomerIds.isNotEmpty()),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Review Campaign (${if (selectAllEligible) eligibleCustomers.size else selectedCustomerIds.size} Recipients)")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

/**
 * Screen 10: Campaign Review & Confirmation Screen
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Campaign Review", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Campaign: $campaignTitle", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Exact Recipient Count: ${recipients.size} customer(s)")
                Text("Total Estimated SMS Segments: ${recipients.sumOf { it.estimatedSegments }}")
                Text("Transport Mode: Android SMS Composer (Default)")
            }
        }

        if (advisory.requiresWarningBanner) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(advisory.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    advisory.guidanceBullets.forEach { bullet ->
                        Text("• $bullet", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Text("Sample Personalized Previews (First 3 Recipients):", fontWeight = FontWeight.Bold)
        recipients.take(3).forEachIndexed { index, r ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recipient #${index + 1}: ${r.recipientNameSnapshot} (${r.normalizedPhoneSnapshot})", fontWeight = FontWeight.Bold)
                    Text(r.resolvedMessageBody, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${r.encodingType} • ${r.estimatedSegments} segment(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
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
                    "I confirm that all recipients have an existing relationship with my business and that I will observe carrier quotas and TRAI communication guidelines.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = onConfirmAndStartSending,
            enabled = confirmedCheckbox && recipients.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Sending Session (${recipients.size} Recipients)")
        }
    }
}

/**
 * Screen 11: Campaign Sending & Handoff Coordinator Screen
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
    val sentCount = recipients.count { it.state == "SENT" || it.state == "DELIVERED" }
    val failedCount = recipients.count { it.state == "FAILED" }
    val cancelledCount = recipients.count { it.state == "CANCELLED" }
    val isFinished = currentIndex >= recipients.size || currentRecipient == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Campaign Sending Session", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Campaign: $campaignTitle", style = MaterialTheme.typography.titleMedium)

        LinearProgressIndicator(
            progress = { if (recipients.isNotEmpty()) (currentIndex.toFloat() / recipients.size) else 1f },
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Progress: ${currentIndex.coerceAtMost(recipients.size)} of ${recipients.size} recipients", style = MaterialTheme.typography.bodyMedium)
            Text("Sent: $sentCount | Skipped: $cancelledCount | Failed: $failedCount", style = MaterialTheme.typography.labelSmall)
        }

        statusMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(msg, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onDismissStatus) { Text("OK") }
                }
            }
        }

        if (isFinished) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Campaign Completed", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("All recipients have been processed.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                        Text("View Campaign History")
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Recipient (${currentIndex + 1} of ${recipients.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Name: ${currentRecipient.recipientNameSnapshot}", fontWeight = FontWeight.SemiBold)
                    Text("Phone: ${currentRecipient.normalizedPhoneSnapshot}")

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Message Body Preview:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentRecipient.resolvedMessageBody, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Text("Status: ${currentRecipient.state}", style = MaterialTheme.typography.labelSmall)

                    Button(
                        onClick = { onDispatchRecipient(currentRecipient) },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open in Messaging App")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilledTonalButton(
                            onClick = { onMarkSent(currentRecipient) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mark Sent & Next")
                        }
                        OutlinedButton(
                            onClick = { onSkipRecipient(currentRecipient) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Skip")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onCancelCampaign,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel Remaining Campaign Queue")
            }
        }
    }
}
