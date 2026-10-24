package com.apnasandesh.app.feature.templates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnasandesh.app.core.database.BusinessProfileEntity
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.database.MessageTemplateEntity
import com.apnasandesh.app.core.model.TemplateCategory
import com.apnasandesh.app.core.ui.theme.BrandAmberTertiaryLight
import com.apnasandesh.app.core.ui.theme.StatusSuccess
import com.apnasandesh.app.domain.validation.SmsCostOptimizer
import com.apnasandesh.app.domain.validation.SmsSegmentCalculator
import com.apnasandesh.app.domain.validation.TemplateRenderer
import java.util.UUID

/**
 * Screen 9: Dedicated Message Template Editor Screen
 * Provides template creation & editing with dynamic variable chips,
 * real-time GSM-7/Unicode segment metrics, and live chat preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorScreen(
    template: MessageTemplateEntity?,
    profile: BusinessProfileEntity?,
    segmentCalculator: SmsSegmentCalculator,
    templateRenderer: TemplateRenderer,
    onBack: () -> Unit,
    onSaveTemplate: (MessageTemplateEntity) -> Unit
) {
    var title by remember { mutableStateOf(template?.title.orEmpty()) }
    var selectedCategory by remember { mutableStateOf(template?.category ?: TemplateCategory.ORDERS_PAYMENTS.name) }
    var body by remember { mutableStateOf(template?.bodyTemplate.orEmpty()) }
    var description by remember { mutableStateOf(template?.description.orEmpty()) }
    var isPromotional by remember { mutableStateOf(template?.isPromotional ?: false) }
    var isFavorite by remember { mutableStateOf(template?.isFavorite ?: false) }
    var titleError by remember { mutableStateOf(false) }
    var bodyError by remember { mutableStateOf(false) }

    val segmentMetrics = remember(body) { segmentCalculator.calculate(body) }
    val costOptimizer = remember { SmsCostOptimizer(segmentCalculator) }
    val canOptimize = remember(body) { costOptimizer.canOptimize(body) }

    val sampleCustomer = remember {
        CustomerEntity(
            fullName = "Customer Name",
            rawPhoneNumber = "",
            normalizedPhoneE164 = "",
            company = "Company",
            lastOrderId = "ORD-101",
            lastOrderAmount = "₹500",
            lastOrderDate = "Today",
            lastAppointmentDate = "Tomorrow",
            lastAppointmentTime = "10:00 AM",
            lastServiceName = "Service"
        )
    }

    val sampleProfile = remember(profile) {
        profile ?: BusinessProfileEntity(
            businessName = "Your Business",
            ownerDisplayName = "Your Name",
            businessCategory = "Retail",
            communicationPhone = "+91 98765 00000",
            defaultMessageSignature = "- Your Business"
        )
    }

    val previewText = remember(body, sampleProfile) {
        if (body.isBlank()) "Type message body above to preview here..."
        else templateRenderer.render(
            rawTemplate = body,
            customer = sampleCustomer,
            profile = sampleProfile,
            appendSignature = false
        )
    }

    val availableVariables = listOf(
        "{{first_name}}" to "First Name",
        "{{customer_name}}" to "Full Name",
        "{{business_name}}" to "Business Name",
        "{{order_id}}" to "Order ID",
        "{{invoice_amount}}" to "Amount",
        "{{order_date}}" to "Date",
        "{{appointment_time}}" to "Time",
        "{{business_phone}}" to "Phone",
        "{{business_signature}}" to "Signature"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (template == null) "Create New Template" else "Edit Template",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { isFavorite = !isFavorite }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) BrandAmberTertiaryLight else MaterialTheme.colorScheme.onSurface
                )
            }
        }
            // Template Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text("Template Title *") },
                placeholder = { Text("e.g. Order Ready for Pickup") },
                isError = titleError,
                supportingText = if (titleError) { { Text("Title cannot be empty") } } else null,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Category Selection Chips
            Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TemplateCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.name,
                        onClick = { selectedCategory = cat.name },
                        label = { Text(cat.displayName) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Message Body Field
            OutlinedTextField(
                value = body,
                onValueChange = {
                    body = it
                    bodyError = false
                },
                label = { Text("Message Body Template *") },
                placeholder = { Text("Namaste {{first_name}}, your order {{order_id}} is ready at {{business_name}}.") },
                isError = bodyError,
                supportingText = if (bodyError) { { Text("Message body cannot be empty") } } else null,
                minLines = 4,
                maxLines = 8,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Dynamic Variable Chips (tap to insert)
            Text(
                text = "Insert Personalization Variable (Tap to insert)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableVariables.forEach { (token, label) ->
                    AssistChip(
                        onClick = {
                            body = if (body.isEmpty()) token else "$body $token"
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Live SMS Segment Calculator Box with Auto-Shrink Quota Saver
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${segmentMetrics.charCount} characters • ${segmentMetrics.segmentCount} segment(s)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (segmentMetrics.isUcs2Unicode) "Unicode encoding (67-70 chars/segment)" else "Standard GSM-7 (153-160 chars/segment)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = if (segmentMetrics.isUcs2Unicode) BrandAmberTertiaryLight.copy(alpha = 0.15f) else StatusSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (segmentMetrics.isUcs2Unicode) "UNICODE" else "GSM-7",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (segmentMetrics.isUcs2Unicode) BrandAmberTertiaryLight else StatusSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (canOptimize) {
                        FilledTonalButton(
                            onClick = {
                                val opt = costOptimizer.optimize(body)
                                body = opt.optimizedText
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✨ Auto-Shrink to Fit 1 SMS (Save 50% Quota)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Live Preview Card
            Text("Live Message Preview", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Preview for: ${sampleCustomer.fullName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }

            // Promotional Flag Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isPromotional,
                    onCheckedChange = { isPromotional = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Promotional Message", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Promotional campaigns are strictly restricted to customers who granted active opt-in consent.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    if (body.isBlank()) {
                        bodyError = true
                        return@Button
                    }
                    val updated = MessageTemplateEntity(
                        id = template?.id ?: UUID.randomUUID().toString(),
                        title = title.trim(),
                        category = selectedCategory,
                        bodyTemplate = body.trim(),
                        description = description.trim(),
                        isPromotional = isPromotional,
                        isFavorite = isFavorite,
                        isSystemDefault = template?.isSystemDefault ?: false,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    onSaveTemplate(updated)
                    onBack()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Template", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
    }
}
