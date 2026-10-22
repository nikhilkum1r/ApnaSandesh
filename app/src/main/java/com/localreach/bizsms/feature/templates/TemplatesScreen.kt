package com.localreach.bizsms.feature.templates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.localreach.bizsms.feature.common.EmptyStateView
import com.localreach.bizsms.feature.common.SmsBubblePreview
import com.localreach.bizsms.feature.common.TokenChipTray
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var editingTemplate by remember { mutableStateOf<MessageTemplateEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<MessageTemplateEntity?>(null) }

    val filteredTemplates = remember(templates, selectedCategoryFilter, searchQuery) {
        templates.filter { t ->
            val matchesCategory = selectedCategoryFilter == "ALL" || t.category == selectedCategoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                t.title.contains(searchQuery, ignoreCase = true) ||
                t.bodyTemplate.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Template", fontWeight = FontWeight.SemiBold) },
                shape = RoundedCornerShape(14.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
                    Text(
                        text = "Message Templates",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${templates.size} available • Personalized variables supported",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search templates by name or content...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Category Filter Scrollable Tab Row
            val allCategories = remember { listOf("ALL" to "All (${templates.size})") + TemplateCategory.entries.map { it.name to it.displayName } }
            val selectedTabIndex = allCategories.indexOfFirst { it.first == selectedCategoryFilter }.coerceAtLeast(0)

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
                divider = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                allCategories.forEachIndexed { index, (catKey, catLabel) ->
                    val isSelected = selectedCategoryFilter == catKey
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = catKey },
                        text = {
                            Text(
                                text = catLabel,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            // Template Cards List
            if (filteredTemplates.isEmpty()) {
                val isFilterActive = searchQuery.isNotBlank() || selectedCategoryFilter != "ALL"
                EmptyStateView(
                    icon = Icons.Default.Description,
                    title = if (isFilterActive) "No matching templates" else "No templates created yet",
                    description = if (isFilterActive) {
                        "Try a different search word or select another category."
                    } else {
                        "Create reusable templates for order confirmations, reminders, and seasonal greetings."
                    },
                    actionButtonLabel = if (isFilterActive) "Clear Filters" else "Create Template",
                    onActionButtonClick = {
                        if (isFilterActive) {
                            searchQuery = ""
                            selectedCategoryFilter = "ALL"
                        } else {
                            showCreateDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTemplates, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            profile = profile,
                            segmentCalculator = viewModel.segmentCalculator,
                            templateRenderer = viewModel.templateRenderer,
                            onEdit = { editingTemplate = template },
                            onDuplicate = { viewModel.duplicateTemplate(template) },
                            onDelete = { deleteTarget = template },
                            onUseInCampaign = if (onSelectTemplateForCampaign != null) {
                                { onSelectTemplateForCampaign(template) }
                            } else null
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
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

    deleteTarget?.let { template ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Delete Custom Template?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete '${template.title}'? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTemplate(template.id)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Human-designed Template Card with live SMS chat preview, encoding badges, and campaign action.
 */
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

    val renderedPreview = remember(template.bodyTemplate, safeProfile, template.isPromotional) {
        templateRenderer.render(
            rawTemplate = template.bodyTemplate,
            customer = sampleCustomer,
            profile = safeProfile,
            appendSignature = true,
            appendOptOutFooter = template.isPromotional
        )
    }
    val metrics = remember(renderedPreview) { segmentCalculator.calculate(renderedPreview) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = template.category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (template.isPromotional) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Promo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (template.isSystemDefault) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Live SMS Chat Bubble Preview
            SmsBubblePreview(
                messageText = renderedPreview,
                encoding = if (metrics.isUcs2Unicode) "UCS-2" else "GSM-7",
                charCount = metrics.charCount,
                segmentCount = metrics.segmentCount,
                recipientName = "Aarav Sharma"
            )

            // Bottom Actions Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Duplicate template",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit template",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!template.isSystemDefault) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete template",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (onUseInCampaign != null) {
                    Button(
                        onClick = onUseInCampaign,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use in Campaign", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Template Editor Dialog with live placeholder tray and SMS encoding metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    val livePreview = remember(body, safeProfile, isPromotional) {
        templateRenderer.render(
            rawTemplate = body,
            customer = sampleCustomer,
            profile = safeProfile,
            appendSignature = true,
            appendOptOutFooter = isPromotional
        )
    }
    val metrics = remember(livePreview) { segmentCalculator.calculate(livePreview) }

    val canSave = title.trim().isNotBlank() && body.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = if (template == null) "Create Message Template" else "Edit Template",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Template Title *") },
                    placeholder = { Text("e.g. Order Ready Pickup") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Business Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    var categoryExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = TemplateCategory.entries.firstOrNull { it.name == selectedCategory }?.displayName ?: selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            TemplateCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        selectedCategory = cat.name
                                        if (cat.isPromotional) {
                                            isPromotional = true
                                        }
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Promotional Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Promotional Message (+ STOP footer)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Appends opt-out instructions required for marketing broadcasts.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPromotional,
                        onCheckedChange = { isPromotional = it }
                    )
                }

                // Message Body
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message Body Template *") },
                    placeholder = { Text("Hello {{customer_name}}, your order is ready...") },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // Token insertion tray
                TokenChipTray(
                    onTokenSelected = { token ->
                        body = if (body.endsWith(" ") || body.isEmpty()) "$body$token " else "$body $token "
                    }
                )

                // Live Preview Bubble
                SmsBubblePreview(
                    messageText = livePreview,
                    encoding = if (metrics.isUcs2Unicode) "UCS-2 (Unicode)" else "GSM-7",
                    charCount = metrics.charCount,
                    segmentCount = metrics.segmentCount
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSave) {
                        onSave(title, selectedCategory, body, isPromotional)
                    }
                },
                enabled = canSave,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Template")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
