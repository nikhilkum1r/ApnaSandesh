package com.apnasandesh.app.feature.templates

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
import com.apnasandesh.app.core.database.BusinessProfileEntity
import com.apnasandesh.app.core.database.CustomerDao
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.database.DatabaseSeeder
import com.apnasandesh.app.core.database.MessageTemplateEntity
import com.apnasandesh.app.core.database.ProfileAndSettingsDao
import com.apnasandesh.app.core.database.TemplateDao
import com.apnasandesh.app.core.model.IndustryType
import com.apnasandesh.app.core.model.TemplateCategory
import com.apnasandesh.app.core.model.TemplateLanguage
import com.apnasandesh.app.core.model.TemplateTone
import com.apnasandesh.app.core.ui.theme.*
import com.apnasandesh.app.domain.validation.SmsSegmentCalculator
import com.apnasandesh.app.domain.validation.TemplateRenderer
import com.apnasandesh.app.R
import com.apnasandesh.app.feature.common.EmptyStateView
import com.apnasandesh.app.feature.common.SmsBubblePreview
import com.apnasandesh.app.feature.common.TokenChipTray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val templateDao: TemplateDao,
    private val customerDao: CustomerDao,
    private val profileDao: ProfileAndSettingsDao,
    private val databaseSeeder: DatabaseSeeder,
    val segmentCalculator: SmsSegmentCalculator,
    val templateRenderer: TemplateRenderer
) : ViewModel() {

    val templates = templateDao.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers = customerDao.observeAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessProfile = profileDao.observeBusinessProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveTemplate(
        id: String?,
        title: String,
        category: String,
        bodyTemplate: String,
        description: String = "",
        language: String = "English",
        tone: String = "Professional",
        industry: String = "General Business",
        isPromotional: Boolean
    ) {
        viewModelScope.launch {
            val entity = MessageTemplateEntity(
                id = id ?: UUID.randomUUID().toString(),
                title = title.trim(),
                category = category,
                bodyTemplate = bodyTemplate.trim(),
                description = description.trim(),
                language = language,
                tone = tone,
                industry = industry,
                isPromotional = isPromotional,
                isSystemDefault = false,
                isFavorite = false,
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
                isFavorite = false,
                updatedAtMillis = System.currentTimeMillis()
            )
            templateDao.upsertTemplate(copy)
        }
    }

    fun toggleFavorite(template: MessageTemplateEntity) {
        viewModelScope.launch {
            templateDao.setFavorite(template.id, !template.isFavorite)
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            templateDao.deleteCustomTemplate(id)
        }
    }

    fun restoreDefaultTemplates() {
        viewModelScope.launch {
            databaseSeeder.restoreDefaultTemplates()
        }
    }
}

/**
 * Screen 7: Comprehensive Message Template Library Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateLibraryScreen(
    viewModel: TemplatesViewModel = hiltViewModel(),
    onSelectTemplateForCampaign: ((MessageTemplateEntity) -> Unit)? = null,
    onEditTemplate: ((MessageTemplateEntity?) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedLanguageFilter by remember { mutableStateOf("ALL") }
    var onlyFavorites by remember { mutableStateOf(false) }
    var onlyCustom by remember { mutableStateOf(false) }

    var editingTemplate by remember { mutableStateOf<MessageTemplateEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<MessageTemplateEntity?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val filteredTemplates = remember(
        templates,
        selectedCategoryFilter,
        selectedLanguageFilter,
        onlyFavorites,
        onlyCustom,
        searchQuery
    ) {
        templates.filter { t ->
            val matchesCategory = selectedCategoryFilter == "ALL" || t.category == selectedCategoryFilter
            val matchesLanguage = selectedLanguageFilter == "ALL" || t.language.contains(selectedLanguageFilter, ignoreCase = true)
            val matchesFavorite = !onlyFavorites || t.isFavorite
            val matchesCustom = !onlyCustom || !t.isSystemDefault
            val matchesSearch = searchQuery.isBlank() ||
                t.title.contains(searchQuery, ignoreCase = true) ||
                t.bodyTemplate.contains(searchQuery, ignoreCase = true) ||
                t.description.contains(searchQuery, ignoreCase = true) ||
                t.industry.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesLanguage && matchesFavorite && matchesCustom && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (onEditTemplate != null) onEditTemplate(null) else showCreateDialog = true
                },
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
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val tCount = filteredTemplates.size
                        val tPlural = if (tCount == 1) "template" else "templates"
                        Text(
                            text = "$tCount $tPlural • Ready to personalize",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Restore Default Library") },
                            leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showRestoreConfirmDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (onlyFavorites) "Show All Templates" else "Show Favorites Only") },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                            onClick = {
                                onlyFavorites = !onlyFavorites
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (onlyCustom) "Show All Templates" else "Show My Custom Templates Only") },
                            leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                            onClick = {
                                onlyCustom = !onlyCustom
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search templates...") },
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
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            // Category Filter Scrollable Tab Row
            val categories = remember {
                listOf("ALL" to "All (${templates.size})") +
                    TemplateCategory.entries.map { it.name to it.displayName }
            }
            val selectedTabIndex = categories.indexOfFirst { it.first == selectedCategoryFilter }.coerceAtLeast(0)

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
                divider = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { _, (catKey, catLabel) ->
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

            // Secondary Quick Filter Bar (Language & Favorites)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = onlyFavorites,
                    onClick = { onlyFavorites = !onlyFavorites },
                    leadingIcon = {
                        Icon(
                            if (onlyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (onlyFavorites) BrandAmberTertiaryLight else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Favorites") },
                    shape = RoundedCornerShape(8.dp)
                )

                FilterChip(
                    selected = onlyCustom,
                    onClick = { onlyCustom = !onlyCustom },
                    label = { Text("Custom Only") },
                    shape = RoundedCornerShape(8.dp)
                )

                FilterChip(
                    selected = selectedLanguageFilter == "ALL",
                    onClick = { selectedLanguageFilter = "ALL" },
                    label = { Text("All Languages") },
                    shape = RoundedCornerShape(8.dp)
                )

                FilterChip(
                    selected = selectedLanguageFilter == "English",
                    onClick = { selectedLanguageFilter = "English" },
                    label = { Text("English") },
                    shape = RoundedCornerShape(8.dp)
                )

                FilterChip(
                    selected = selectedLanguageFilter == "Hinglish",
                    onClick = { selectedLanguageFilter = "Hinglish" },
                    label = { Text("Hinglish") },
                    shape = RoundedCornerShape(8.dp)
                )

                FilterChip(
                    selected = selectedLanguageFilter == "Hindi",
                    onClick = { selectedLanguageFilter = "Hindi" },
                    label = { Text("Hindi (हिंदी)") },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Template Cards List
            if (filteredTemplates.isEmpty()) {
                val isFilterActive = searchQuery.isNotBlank() || selectedCategoryFilter != "ALL" || onlyFavorites || onlyCustom || selectedLanguageFilter != "ALL"
                EmptyStateView(
                    illustrationRes = if (isFilterActive) null else R.drawable.ic_empty_templates,
                    icon = if (isFilterActive) Icons.Default.SearchOff else Icons.Default.Description,
                    title = if (isFilterActive) "No matching templates" else "No templates created yet",
                    description = if (isFilterActive) {
                        "Try a different keyword or reset active filters."
                    } else {
                        "Restore the pre-built library or build a new template with dynamic customer variables."
                    },
                    actionButtonLabel = if (isFilterActive) "Clear All Filters" else "Restore Library",
                    onActionButtonClick = {
                        if (isFilterActive) {
                            searchQuery = ""
                            selectedCategoryFilter = "ALL"
                            selectedLanguageFilter = "ALL"
                            onlyFavorites = false
                            onlyCustom = false
                        } else {
                            viewModel.restoreDefaultTemplates()
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
                            customers = customers,
                            segmentCalculator = viewModel.segmentCalculator,
                            templateRenderer = viewModel.templateRenderer,
                            onEdit = {
                                if (onEditTemplate != null) onEditTemplate(template) else editingTemplate = template
                            },
                            onDuplicate = { viewModel.duplicateTemplate(template) },
                            onToggleFavorite = { viewModel.toggleFavorite(template) },
                            onDelete = { deleteTarget = template },
                            onUseInCampaign = if (onSelectTemplateForCampaign != null) {
                                { onSelectTemplateForCampaign(template) }
                            } else null
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        TemplateEditorDialog(
            template = null,
            profile = profile,
            customers = customers,
            segmentCalculator = viewModel.segmentCalculator,
            templateRenderer = viewModel.templateRenderer,
            onDismiss = { showCreateDialog = false },
            onSave = { title, category, body, desc, lang, tone, industry, isPromotional ->
                viewModel.saveTemplate(
                    id = null,
                    title = title,
                    category = category,
                    bodyTemplate = body,
                    description = desc,
                    language = lang,
                    tone = tone,
                    industry = industry,
                    isPromotional = isPromotional
                )
                showCreateDialog = false
            }
        )
    }

    editingTemplate?.let { template ->
        TemplateEditorDialog(
            template = template,
            profile = profile,
            customers = customers,
            segmentCalculator = viewModel.segmentCalculator,
            templateRenderer = viewModel.templateRenderer,
            onDismiss = { editingTemplate = null },
            onSave = { title, category, body, desc, lang, tone, industry, isPromotional ->
                viewModel.saveTemplate(
                    id = template.id,
                    title = title,
                    category = category,
                    bodyTemplate = body,
                    description = desc,
                    language = lang,
                    tone = tone,
                    industry = industry,
                    isPromotional = isPromotional
                )
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

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Restore Default Library?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will refresh all built-in template categories (A to H). Any custom templates you created will be completely preserved.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreDefaultTemplates()
                        showRestoreConfirmDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Restore Defaults")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Enhanced Template Card with live preview, badges, favorite toggle, and segment estimate.
 */
@Composable
fun TemplateCard(
    template: MessageTemplateEntity,
    profile: BusinessProfileEntity?,
    customers: List<CustomerEntity>,
    segmentCalculator: SmsSegmentCalculator,
    templateRenderer: TemplateRenderer,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onUseInCampaign: (() -> Unit)?
) {
    val sampleCustomer = remember(customers) {
        customers.firstOrNull { it.fullName.isNotBlank() } ?: CustomerEntity(
            fullName = "Aarav Sharma",
            rawPhoneNumber = "9876543210",
            normalizedPhoneE164 = "+919876543210"
        )
    }
    val safeProfile = profile ?: BusinessProfileEntity(
        businessName = "Apna General Store",
        ownerDisplayName = "Shop Owner",
        businessCategory = "Retail",
        communicationPhone = "+919876500000"
    )

    val renderedPreview = remember(template.bodyTemplate, safeProfile, sampleCustomer, template.isPromotional) {
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
            // Header Row: Title, Badges, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
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
                    if (template.description.isNotBlank()) {
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = template.category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() } +
                            if (template.industry.isNotBlank() && template.industry != "General Business") " • ${template.industry}" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (template.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (template.isFavorite) BrandAmberTertiaryLight else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

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

                    Surface(
                        color = if (template.isSystemDefault) MaterialTheme.colorScheme.surfaceVariant else BrandBluePrimaryContainerLight.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (template.isSystemDefault) "Built-in" else "Custom",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (template.isSystemDefault) MaterialTheme.colorScheme.onSurfaceVariant else BrandBluePrimaryLight,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Live SMS Chat Bubble Preview
            SmsBubblePreview(
                messageText = renderedPreview,
                encoding = if (metrics.isUcs2Unicode) "UCS-2 (Unicode)" else "GSM-7",
                charCount = metrics.charCount,
                segmentCount = metrics.segmentCount,
                recipientName = sampleCustomer.fullName.ifBlank { "Sample Recipient" }
            )

            // Segment & Encoding info banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = segmentCalculator.getEncodingExplanation(metrics),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (metrics.segmentCount > 1) BrandAmberTertiaryLight else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (template.language.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = template.language,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

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
 * Full Template Editor Dialog with real recipient picker, variable tray, encoding badges.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorDialog(
    template: MessageTemplateEntity?,
    profile: BusinessProfileEntity?,
    customers: List<CustomerEntity>,
    segmentCalculator: SmsSegmentCalculator,
    templateRenderer: TemplateRenderer,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(template?.title ?: "") }
    var description by remember { mutableStateOf(template?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(template?.category ?: TemplateCategory.CUSTOM.name) }
    var selectedIndustry by remember { mutableStateOf(template?.industry ?: IndustryType.GENERAL.displayName) }
    var selectedLanguage by remember { mutableStateOf(template?.language ?: TemplateLanguage.ENGLISH.displayName) }
    var selectedTone by remember { mutableStateOf(template?.tone ?: TemplateTone.PROFESSIONAL.displayName) }
    var body by remember {
        mutableStateOf(template?.bodyTemplate ?: "Hello {{first_name}}, thank you for choosing {{business_name}}! Reach us at {{business_phone}}.")
    }
    var isPromotional by remember { mutableStateOf(template?.isPromotional ?: false) }

    // Selected customer for live preview
    var previewCustomer by remember {
        mutableStateOf(customers.firstOrNull() ?: CustomerEntity(
            fullName = "Aarav Sharma",
            rawPhoneNumber = "9876543210",
            normalizedPhoneE164 = "+919876543210"
        ))
    }

    val safeProfile = profile ?: BusinessProfileEntity(
        businessName = "Apna General Store",
        ownerDisplayName = "Shop Owner",
        businessCategory = "Retail",
        communicationPhone = "+919876500000"
    )

    val livePreview = remember(body, safeProfile, previewCustomer, isPromotional) {
        templateRenderer.render(
            rawTemplate = body,
            customer = previewCustomer,
            profile = safeProfile,
            appendSignature = true,
            appendOptOutFooter = isPromotional
        )
    }
    val metrics = remember(livePreview) { segmentCalculator.calculate(livePreview) }
    val missingVars = remember(body, previewCustomer, safeProfile) {
        templateRenderer.findMissingVariables(body, previewCustomer, safeProfile)
    }

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

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes (Optional)") },
                    placeholder = { Text("When to send this message...") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category & Industry Dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var catExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = TemplateCategory.entries.firstOrNull { it.name == selectedCategory }?.displayName ?: selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            maxLines = 1,
                            label = { Text("Category", maxLines = 1) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false }
                        ) {
                            TemplateCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        selectedCategory = cat.name
                                        if (cat.isPromotional) isPromotional = true
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    var langExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = langExpanded,
                        onExpandedChange = { langExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedLanguage,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            maxLines = 1,
                            label = { Text("Language", maxLines = 1) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = langExpanded,
                            onDismissRequest = { langExpanded = false }
                        ) {
                            TemplateLanguage.entries.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.displayName) },
                                    onClick = {
                                        selectedLanguage = lang.displayName
                                        langExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Tone & Industry Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var toneExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = toneExpanded,
                        onExpandedChange = { toneExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedTone,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            maxLines = 1,
                            label = { Text("Tone", maxLines = 1) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toneExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = toneExpanded,
                            onDismissRequest = { toneExpanded = false }
                        ) {
                            TemplateTone.entries.forEach { tone ->
                                DropdownMenuItem(
                                    text = { Text(tone.displayName) },
                                    onClick = {
                                        selectedTone = tone.displayName
                                        toneExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    var indExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = indExpanded,
                        onExpandedChange = { indExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedIndustry,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            maxLines = 1,
                            label = { Text("Industry", maxLines = 1) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = indExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = indExpanded,
                            onDismissRequest = { indExpanded = false }
                        ) {
                            IndustryType.entries.forEach { ind ->
                                DropdownMenuItem(
                                    text = { Text(ind.displayName) },
                                    onClick = {
                                        selectedIndustry = ind.displayName
                                        indExpanded = false
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
                            text = "Appends opt-out instruction required for marketing broadcasts.",
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
                    placeholder = { Text("Hello {{first_name}}, update from {{business_name}}...") },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // Categorized Token Insertion Tray
                TokenChipTray(
                    onTokenSelected = { token ->
                        body = if (body.endsWith(" ") || body.isEmpty()) "$body$token " else "$body $token "
                    }
                )

                // Customer Preview Selector
                if (customers.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Preview Using Customer:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        var custExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = custExpanded,
                            onExpandedChange = { custExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = "${previewCustomer.fullName} (${previewCustomer.normalizedPhoneE164})",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = custExpanded) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = custExpanded,
                                onDismissRequest = { custExpanded = false }
                            ) {
                                customers.take(10).forEach { cust ->
                                    DropdownMenuItem(
                                        text = { Text("${cust.fullName} (${cust.normalizedPhoneE164})") },
                                        onClick = {
                                            previewCustomer = cust
                                            custExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Missing variables notice if any
                if (missingVars.isNotEmpty()) {
                    Surface(
                        color = StatusWarningContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notice: Selected customer has no value for ${missingVars.joinToString()}. Safe defaults will be applied.",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusWarning
                            )
                        }
                    }
                }

                // Live Preview Bubble
                SmsBubblePreview(
                    messageText = livePreview,
                    encoding = if (metrics.isUcs2Unicode) "UCS-2 (Unicode)" else "GSM-7",
                    charCount = metrics.charCount,
                    segmentCount = metrics.segmentCount,
                    recipientName = previewCustomer.fullName
                )

                // Segment Warning Alert if > 1 segment
                if (metrics.segmentCount > 1) {
                    Surface(
                        color = BrandAmberTertiaryLight.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BrandAmberTertiaryLight.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = BrandAmberTertiaryLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "This message will be sent as ${metrics.segmentCount} SMS parts per recipient (${metrics.charCount} characters).",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandAmberTertiaryLight
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSave) {
                        onSave(
                            title,
                            selectedCategory,
                            body,
                            description,
                            selectedLanguage,
                            selectedTone,
                            selectedIndustry,
                            isPromotional
                        )
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
