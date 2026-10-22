package com.localreach.bizsms.feature.templates

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.localreach.bizsms.core.database.BusinessProfileEntity
import com.localreach.bizsms.core.database.CustomerEntity
import com.localreach.bizsms.core.database.MessageTemplateEntity
import com.localreach.bizsms.core.database.ProfileAndSettingsDao
import com.localreach.bizsms.core.database.TemplateDao
import com.localreach.bizsms.core.model.TemplateCategory
import com.localreach.bizsms.domain.validation.SmsSegmentCalculator
import com.localreach.bizsms.domain.validation.TemplateRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val templateDao: TemplateDao,
    private val profileDao: ProfileAndSettingsDao,
    val segmentCalculator: SmsSegmentCalculator,
    val templateRenderer: TemplateRenderer
) : ViewModel() {

    val templates = templateDao.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessProfile = profileDao.observeBusinessProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveTemplate(
        id: String?,
        title: String,
        category: String,
        bodyTemplate: String,
        isPromotional: Boolean
    ) {
        viewModelScope.launch {
            val entity = MessageTemplateEntity(
                id = id ?: UUID.randomUUID().toString(),
                title = title.trim(),
                category = category,
                bodyTemplate = bodyTemplate.trim(),
                isPromotional = isPromotional,
                isSystemDefault = false,
                updatedAtMillis = System.currentTimeMillis()
            )
            templateDao.upsertTemplate(entity)
        }
    }

    fun duplicateTemplate(template: MessageTemplateEntity) {
        viewModelScope.launch {
            val copy = template.copy(
                id = UUID.randomUUID().toString(),
                title = "${template.title} (Copy)",
                isSystemDefault = false,
                updatedAtMillis = System.currentTimeMillis()
            )
            templateDao.upsertTemplate(copy)
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            templateDao.deleteCustomTemplate(id)
        }
    }
}

/**
 * Screen 7: Message Template Library Screen
 */
@Composable
fun TemplateLibraryScreen(
    viewModel: TemplatesViewModel = hiltViewModel(),
    onSelectTemplateForCampaign: ((MessageTemplateEntity) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var editingTemplate by remember { mutableStateOf<MessageTemplateEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredTemplates = remember(templates, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") templates
        else templates.filter { it.category == selectedCategoryFilter }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Template")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
                Text("Message Templates", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            // Category Filter Scrollable Row
            ScrollableTabRow(
                selectedTabIndex = TemplateCategory.entries.indexOfFirst { it.name == selectedCategoryFilter }.let { if (it >= 0) it + 1 else 0 },
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedCategoryFilter == "ALL",
                    onClick = { selectedCategoryFilter = "ALL" },
                    text = { Text("All (${templates.size})") }
                )
                TemplateCategory.entries.forEach { cat ->
                    Tab(
                        selected = selectedCategoryFilter == cat.name,
                        onClick = { selectedCategoryFilter = cat.name },
                        text = { Text(cat.displayName) }
                    )
                }
            }

            if (filteredTemplates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No templates in this category.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredTemplates, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            profile = profile,
                            segmentCalculator = viewModel.segmentCalculator,
                            templateRenderer = viewModel.templateRenderer,
                            onEdit = { editingTemplate = template },
                            onDuplicate = { viewModel.duplicateTemplate(template) },
                            onDelete = { viewModel.deleteTemplate(template.id) },
                            onUseInCampaign = if (onSelectTemplateForCampaign != null) {
                                { onSelectTemplateForCampaign(template) }
                            } else null
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        TemplateEditorDialog(
            template = null,
            profile = profile,
            segmentCalculator = viewModel.segmentCalculator,
            templateRenderer = viewModel.templateRenderer,
            onDismiss = { showCreateDialog = false },
            onSave = { title, category, body, isPromotional ->
                viewModel.saveTemplate(null, title, category, body, isPromotional)
                showCreateDialog = false
            }
        )
    }

    editingTemplate?.let { template ->
        TemplateEditorDialog(
            template = template,
            profile = profile,
            segmentCalculator = viewModel.segmentCalculator,
            templateRenderer = viewModel.templateRenderer,
            onDismiss = { editingTemplate = null },
            onSave = { title, category, body, isPromotional ->
                viewModel.saveTemplate(template.id, title, category, body, isPromotional)
                editingTemplate = null
            }
        )
    }
}

@Composable
fun TemplateCard(
    template: MessageTemplateEntity,
    profile: BusinessProfileEntity?,
    segmentCalculator: SmsSegmentCalculator,
    templateRenderer: TemplateRenderer,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onUseInCampaign: (() -> Unit)?
) {
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

    val renderedPreview = remember(template.bodyTemplate, safeProfile) {
        templateRenderer.render(
            rawTemplate = template.bodyTemplate,
            customer = sampleCustomer,
            profile = safeProfile,
            appendSignature = true
        )
    }
    val metrics = remember(renderedPreview) { segmentCalculator.calculate(renderedPreview) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(template.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (template.isSystemDefault) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Default", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Text(
                text = "Template: ${template.bodyTemplate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Text("Live Preview (for Aarav Sharma):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(renderedPreview, style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${if (metrics.isUcs2Unicode) "UCS-2 (Unicode)" else "GSM-7"} • ${metrics.charCount} chars • ${metrics.segmentCount} segment(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onUseInCampaign != null) {
                    Button(onClick = onUseInCampaign) {
                        Text("Use in Campaign")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = onDuplicate) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                if (!template.isSystemDefault) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateEditorDialog(
    template: MessageTemplateEntity?,
    profile: BusinessProfileEntity?,
    segmentCalculator: SmsSegmentCalculator,
    templateRenderer: TemplateRenderer,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(template?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(template?.category ?: TemplateCategory.CUSTOM.name) }
    var body by remember { mutableStateOf(template?.bodyTemplate ?: "Hello {{customer_name}}, update from {{business_name}}: ") }
    var isPromotional by remember { mutableStateOf(template?.isPromotional ?: false) }

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

    val livePreview = remember(body, safeProfile) {
        templateRenderer.render(
            rawTemplate = body,
            customer = sampleCustomer,
            profile = safeProfile,
            appendSignature = true,
            appendOptOutFooter = isPromotional
        )
    }
    val metrics = remember(livePreview) { segmentCalculator.calculate(livePreview) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "Create Message Template" else "Edit Template") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Template Title *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = !isPromotional,
                        onClick = { isPromotional = false },
                        label = { Text("Transactional") }
                    )
                    FilterChip(
                        selected = isPromotional,
                        onClick = { isPromotional = true },
                        label = { Text("Promotional") }
                    )
                }

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message Body Template *") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Click token to insert:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { body += "{{customer_name}}" },
                        label = { Text("Name", style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = { body += "{{business_name}}" },
                        label = { Text("Shop", style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = { body += "{{business_phone}}" },
                        label = { Text("Phone", style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = { body += "{{signature}}" },
                        label = { Text("Signature", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Live Preview:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text(livePreview, style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider()
                        Text(
                            "Encoding: ${if (metrics.isUcs2Unicode) "UCS-2 (Unicode)" else "GSM-7"} • Chars: ${metrics.charCount} • Est. Segments: ${metrics.segmentCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        onSave(title, selectedCategory, body, isPromotional)
                    }
                }
            ) {
                Text("Save Template")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
