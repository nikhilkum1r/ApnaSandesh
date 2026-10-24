package com.apnasandesh.app.feature.customers

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.apnasandesh.app.data.customer.CustomerRepository
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnasandesh.app.core.database.CustomerEntity
import com.apnasandesh.app.core.database.CustomerGroupEntity
import com.apnasandesh.app.core.ui.theme.BrandAmberTertiaryLight
import com.apnasandesh.app.core.ui.theme.StatusError
import com.apnasandesh.app.core.ui.theme.StatusSuccess
import com.apnasandesh.app.domain.validation.CsvImportSummary
import com.apnasandesh.app.domain.validation.PhoneNormalizer
import com.apnasandesh.app.domain.validation.PhoneValidationResult
import com.apnasandesh.app.R
import com.apnasandesh.app.feature.common.CustomerAvatar
import com.apnasandesh.app.feature.common.EmptyStateView
import com.apnasandesh.app.feature.common.StatusBadge

/**
 * Screen 4: Customer Directory Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDirectoryScreen(
    customers: List<CustomerEntity>,
    groups: List<CustomerGroupEntity>,
    onAddCustomer: (String, String, String, String, String, String, String, String, String, () -> Unit) -> Unit,
    onUpdateCustomer: (CustomerEntity, String, String, String, String, String, String, String, String, () -> Unit) -> Unit,
    onDeleteCustomer: (String) -> Unit,
    onOptOut: (CustomerEntity, String) -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onCustomerClick: ((CustomerEntity) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedConsentFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var optOutTarget by remember { mutableStateOf<CustomerEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<CustomerEntity?>(null) }

    val filteredCustomers = remember(customers, searchQuery, selectedConsentFilter) {
        customers.filter { c ->
            val matchesQuery = searchQuery.isBlank() ||
                c.fullName.contains(searchQuery, ignoreCase = true) ||
                c.normalizedPhoneE164.contains(searchQuery) ||
                c.rawPhoneNumber.contains(searchQuery) ||
                c.company.contains(searchQuery, ignoreCase = true) ||
                c.email.contains(searchQuery, ignoreCase = true) ||
                c.tagsCsv.contains(searchQuery, ignoreCase = true) ||
                c.notes.contains(searchQuery, ignoreCase = true)

            val matchesConsent = when (selectedConsentFilter) {
                "GRANTED" -> c.consentStatus == "GRANTED" && !c.isOptedOut
                "UNKNOWN" -> c.consentStatus == "UNKNOWN" && !c.isOptedOut
                "OPTED_OUT" -> c.isOptedOut || c.consentStatus == "OPTED_OUT"
                else -> true
            }

            matchesQuery && matchesConsent
        }
    }

    val totalCount = customers.size
    val optedInCount = remember(customers) { customers.count { it.consentStatus == "GRANTED" && !it.isOptedOut } }
    val unknownCount = remember(customers) { customers.count { it.consentStatus == "UNKNOWN" && !it.isOptedOut } }
    val suppressedCount = remember(customers) { customers.count { it.isOptedOut || it.consentStatus == "OPTED_OUT" } }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Customer", fontWeight = FontWeight.SemiBold) },
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
            // Header Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val contactWord = if (totalCount == 1) "contact" else "contacts"
                Text(
                    text = "$totalCount $contactWord • $optedInCount eligible",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onNavigateToImport,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onNavigateToGroups,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Groups", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search contacts...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

            // Horizontally Scrollable Consent Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedConsentFilter == "ALL",
                    onClick = { selectedConsentFilter = "ALL" },
                    label = { Text("All ($totalCount)") },
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = selectedConsentFilter == "GRANTED",
                    onClick = { selectedConsentFilter = "GRANTED" },
                    label = { Text("Opted-In ($optedInCount)") },
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = selectedConsentFilter == "UNKNOWN",
                    onClick = { selectedConsentFilter = "UNKNOWN" },
                    label = { Text("Unknown ($unknownCount)") },
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = selectedConsentFilter == "OPTED_OUT",
                    onClick = { selectedConsentFilter = "OPTED_OUT" },
                    label = { Text("Opted-Out ($suppressedCount)") },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Directory List
            if (filteredCustomers.isEmpty()) {
                val isFilterActive = searchQuery.isNotBlank() || selectedConsentFilter != "ALL"
                EmptyStateView(
                    illustrationRes = if (isFilterActive) null else R.drawable.ic_empty_customers,
                    icon = if (isFilterActive) Icons.Default.SearchOff else Icons.Default.PeopleOutline,
                    title = if (isFilterActive) "No matching customers found" else "Your customer directory is empty",
                    description = if (isFilterActive) {
                        "Try adjusting your search query or switching the consent filter."
                    } else {
                        "Add your local customers manually or import a contact list / CSV to start messaging."
                    },
                    actionButtonLabel = if (isFilterActive) "Clear Filters" else "Add First Customer",
                    onActionButtonClick = {
                        if (isFilterActive) {
                            searchQuery = ""
                            selectedConsentFilter = "ALL"
                        } else {
                            showAddDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onEdit = { editingCustomer = customer },
                            onDelete = { deleteTarget = customer },
                            onOptOut = { optOutTarget = customer },
                            onClick = onCustomerClick?.let { { it(customer) } }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CustomerEditDialog(
            customer = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, tags, notes, consent, amount, orderId, apptDate ->
                onAddCustomer(name, phone, tags, notes, consent, "MANUAL_ENTRY", amount, orderId, apptDate) {
                    showAddDialog = false
                }
            }
        )
    }

    editingCustomer?.let { customer ->
        CustomerEditDialog(
            customer = customer,
            onDismiss = { editingCustomer = null },
            onConfirm = { name, phone, tags, notes, consent, amount, orderId, apptDate ->
                onUpdateCustomer(customer, name, phone, tags, notes, consent, amount, orderId, apptDate) {
                    editingCustomer = null
                }
            }
        )
    }

    deleteTarget?.let { customer ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Customer Record?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${customer.fullName} (${customer.normalizedPhoneE164})? Past campaign message records will be retained, but the customer will be removed from future recipient pickers.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomer(customer.id)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Customer")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    optOutTarget?.let { customer ->
        AlertDialog(
            onDismissRequest = { optOutTarget = null },
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Permanent Opt-Out Firewall",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Permanently suppress ${customer.fullName} (${customer.normalizedPhoneE164}) from future messaging?\n\nThis immediately adds their phone number to the local Suppression Firewall and automatically cancels any pending or queued campaign drafts for them.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onOptOut(customer, "Customer requested STOP / Opt-out")
                        optOutTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm Permanent Opt-Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { optOutTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Production-Quality Customer Card with initials avatar, tag pills, note box, and clean actions.
 */
@Composable
fun CustomerCard(
    customer: CustomerEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOptOut: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onClick?.invoke() ?: onEdit() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomerAvatar(name = customer.fullName, size = 42)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.fullName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = customer.normalizedPhoneE164,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(
                    status = if (customer.isOptedOut) "OPTED_OUT" else customer.consentStatus
                )
            }

            // Tags Row
            if (customer.tagsCsv.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    customer.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Pending Dues & Appointment Badges
            if (customer.lastOrderAmount.isNotBlank() || customer.lastAppointmentDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (customer.lastOrderAmount.isNotBlank()) {
                        Surface(
                            color = BrandAmberTertiaryLight.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, BrandAmberTertiaryLight.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = BrandAmberTertiaryLight,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Due: ₹${customer.lastOrderAmount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandAmberTertiaryLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (customer.lastAppointmentDate.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Appt: ${customer.lastAppointmentDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Customer Notes
            if (customer.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: ${customer.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!customer.isOptedOut && customer.consentStatus != "OPTED_OUT") {
                    TextButton(
                        onClick = onOptOut,
                        colors = ButtonDefaults.textButtonColors(contentColor = StatusError)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Opt-Out", style = MaterialTheme.typography.labelMedium)
                    }
                }

                val context = LocalContext.current
                TextButton(
                    onClick = {
                        val digitsOnly = customer.normalizedPhoneE164.filter { it.isDigit() }
                        val waIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?phone=$digitsOnly")
                        }
                        runCatching {
                            context.startActivity(waIntent)
                        }.onFailure {
                            android.widget.Toast.makeText(context, "WhatsApp is not installed", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusSuccess)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", style = MaterialTheme.typography.labelMedium)
                }

                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.labelMedium)
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Customer Edit Dialog with live E.164 normalization preview and tag recommendations.
 */
@Composable
fun CustomerEditDialog(
    customer: CustomerEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(customer?.fullName ?: "") }
    var phone by remember { mutableStateOf(customer?.rawPhoneNumber ?: "") }
    var tags by remember { mutableStateOf(customer?.tagsCsv ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }
    var consent by remember { mutableStateOf(customer?.consentStatus ?: "GRANTED") }
    var lastOrderAmount by remember { mutableStateOf(customer?.lastOrderAmount ?: "") }
    var lastOrderId by remember { mutableStateOf(customer?.lastOrderId ?: "") }
    var lastAppointmentDate by remember { mutableStateOf(customer?.lastAppointmentDate ?: "") }

    val phoneNormalizer = remember { PhoneNormalizer() }
    val normalizationResult = remember(phone) { phoneNormalizer.normalize(phone, "+91") }

    val isPhoneValid = normalizationResult is PhoneValidationResult.Valid
    val canSubmit = name.trim().isNotBlank() && phone.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = if (customer == null) "Add Customer" else "Edit Customer",
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("Enter full name") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number *") },
                        placeholder = { Text("e.g. 9876543210 or +91...") },
                        isError = phone.isNotBlank() && !isPhoneValid,
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Live E.164 normalization feedback
                    if (phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        when (normalizationResult) {
                            is PhoneValidationResult.Valid -> {
                                Text(
                                    text = "✓ E.164 Standard: ${normalizationResult.e164Phone}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusSuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            is PhoneValidationResult.Invalid -> {
                                Text(
                                    text = "⚠ ${normalizationResult.reason}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Tags
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma-separated)") },
                        placeholder = { Text("vip, retail, festival") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("vip", "retail", "wholesale", "frequent", "lead").forEach { suggestedTag ->
                            SuggestionChip(
                                onClick = {
                                    val currentTags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    if (!currentTags.contains(suggestedTag)) {
                                        tags = if (tags.isBlank()) suggestedTag else "$tags, $suggestedTag"
                                    }
                                },
                                label = { Text("+$suggestedTag", style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Add customer notes") },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Consent Status Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Consent Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = consent == "GRANTED",
                            onClick = { consent = "GRANTED" },
                            label = { Text("Opted-In") },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = consent == "UNKNOWN",
                            onClick = { consent = "UNKNOWN" },
                            label = { Text("Unknown") },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = consent == "OPTED_OUT",
                            onClick = { consent = "OPTED_OUT" },
                            label = { Text("Opted-Out") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Text(
                        text = when (consent) {
                            "GRANTED" -> "Eligible for transactional and promotional broadcasts."
                            "UNKNOWN" -> "Eligible for transactional updates only (e.g. order ready)."
                            else -> "Suppressed. Will never receive broadcasts."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text(
                    text = "Dues & Reminders (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = lastOrderAmount,
                    onValueChange = { lastOrderAmount = it },
                    label = { Text("Outstanding Due / Bill (₹)") },
                    placeholder = { Text("e.g. 1500") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lastOrderId,
                        onValueChange = { lastOrderId = it },
                        label = { Text("Bill / Order ID") },
                        placeholder = { Text("e.g. INV-101") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = lastAppointmentDate,
                        onValueChange = { lastAppointmentDate = it },
                        label = { Text("Appt Date") },
                        placeholder = { Text("e.g. 25 Oct") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSubmit) {
                        onConfirm(name, phone, tags, notes, consent, lastOrderAmount, lastOrderId, lastAppointmentDate)
                    }
                },
                enabled = canSubmit,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (customer == null) "Add Customer" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for browsing and multi-selecting device contacts for bulk import.
 */
@Composable
fun BulkContactSelectionDialog(
    contacts: List<CustomerRepository.DeviceContactItem>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirmImport: (List<CustomerRepository.DeviceContactItem>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateMapOf<String, Boolean>() }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery)
        }
    }

    val selectedList = remember(filteredContacts, selectedIds.toMap()) {
        filteredContacts.filter { selectedIds[it.id] == true }
    }

    val allFilteredSelected = filteredContacts.isNotEmpty() && filteredContacts.all { selectedIds[it.id] == true }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Device Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${selectedList.size}/${filteredContacts.size} selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name or phone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (filteredContacts.isEmpty()) "0 contacts" else "${filteredContacts.size} contacts found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            val targetState = !allFilteredSelected
                            filteredContacts.forEach { selectedIds[it.id] = targetState }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (allFilteredSelected) "Deselect All" else "Select All",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else if (filteredContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No contacts found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filteredContacts, key = { it.id + it.phoneNumber }) { contact ->
                            val isChecked = selectedIds[contact.id] == true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedIds[contact.id] = !isChecked }
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { selectedIds[contact.id] = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = contact.phoneNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmImport(selectedList) },
                enabled = selectedList.isNotEmpty(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Import (${selectedList.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Screen 5: Customer Import Screen (Contacts + CSV)
 */
@Composable
fun CustomerImportScreen(
    preview: CsvImportSummary?,
    deviceContacts: List<CustomerRepository.DeviceContactItem> = emptyList(),
    isLoadingDeviceContacts: Boolean = false,
    onLoadDeviceContacts: () -> Unit = {},
    onImportSelectedContacts: (List<CustomerRepository.DeviceContactItem>) -> Unit = {},
    onPickContact: (Uri) -> Unit,
    onImportCsv: (Uri) -> Unit,
    onExportCsv: (Uri) -> Unit,
    onClearPreview: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showBulkContactsDialog by remember { mutableStateOf(false) }

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onLoadDeviceContacts()
            showBulkContactsDialog = true
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri -> if (uri != null) onPickContact(uri) }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) onImportCsv(uri) }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> if (uri != null) onExportCsv(uri) }

    var showExportWarningDialog by remember { mutableStateOf(false) }

    if (showBulkContactsDialog) {
        BulkContactSelectionDialog(
            contacts = deviceContacts,
            isLoading = isLoadingDeviceContacts,
            onDismiss = { showBulkContactsDialog = false },
            onConfirmImport = { selected ->
                showBulkContactsDialog = false
                onImportSelectedContacts(selected)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    text = "Import & Export",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Easily load contacts or backup customer records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Method 1: Browse All Device Contacts (Multi-Select)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Browse All Device Contacts",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Searchable multi-select bulk import",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "Browse your entire contact book, search names/numbers, and import multiple contacts with one click.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_CONTACTS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            onLoadDeviceContacts()
                            showBulkContactsDialog = true
                        } else {
                            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse & Select Contacts", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Method 2: System Contact Picker
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "System Contact Picker",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Zero-Permission Private Single Contact Import",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusSuccess
                        )
                    }
                }

                Text(
                    text = "Pick individual contacts one by one directly from Android's system contact picker.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { contactPickerLauncher.launch(null) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick Single Contact", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Method 2: CSV File Import
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CSV Spreadsheet Import",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Bulk import with automatic phone normalization",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Load contacts from a CSV file (Name, Phone, Tags, Notes).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // CSV format guide snippet
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Expected CSV Header Format:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Name,Phone,Tags,Notes,Consent_Status\nCustomer Name,9876543210,retail,Weekly buyer,GRANTED",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = { csvImportLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*")) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose CSV File", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Live CSV Preview Results
        if (preview != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
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
                            text = "CSV Import Audit Summary",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onClearPreview) {
                            Icon(Icons.Default.Close, contentDescription = "Close preview")
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = StatusSuccess.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Valid Imported", style = MaterialTheme.typography.labelSmall, color = StatusSuccess)
                                Text("${preview.validCustomers.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusSuccess)
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Duplicate Phones", style = MaterialTheme.typography.labelSmall)
                                Text("${preview.duplicatePhonesInFile.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            color = StatusError.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Suppressed", style = MaterialTheme.typography.labelSmall, color = StatusError)
                                Text("${preview.suppressedSkippedCount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusError)
                            }
                        }
                    }

                    if (preview.invalidRows.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rejected Rows (${preview.invalidRows.size}):",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = StatusError
                        )
                        preview.invalidRows.take(5).forEach { (line, reason) ->
                            Text(
                                text = "• Line $line: $reason",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onClearPreview,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Acknowledge & Close")
                    }
                }
            }
        }

        // Method 3: Export CSV
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Export Contacts",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Save a CSV backup to device storage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Export your entire customer database to a CSV file.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { showExportWarningDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Contacts (CSV)", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showExportWarningDialog) {
        AlertDialog(
            onDismissRequest = { showExportWarningDialog = false },
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            },
            title = {
                Text(
                    text = "Customer Privacy & Export Warning",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Exported CSV files are unencrypted and contain your customers' full names, phone numbers, and notes.\n\nEnsure that you store the exported file securely on this device and avoid sharing it over insecure channels.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportWarningDialog = false
                        csvExportLauncher.launch("localreach_customers_export.csv")
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Proceed with Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportWarningDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Screen 6: Customer Groups Screen
 */
@Composable
fun CustomerGroupsScreen(
    groups: List<CustomerGroupEntity>,
    customers: List<CustomerEntity>,
    onCreateGroup: (String, String) -> Unit,
    onUpdateGroup: (String, String, String) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<CustomerGroupEntity?>(null) }
    var groupName by remember { mutableStateOf("") }
    var groupDesc by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Customer Groups",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${groups.size} groups configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    groupName = ""
                    groupDesc = ""
                    showCreateDialog = true
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Group", fontWeight = FontWeight.SemiBold)
            }
        }

        if (groups.isEmpty()) {
            EmptyStateView(
                illustrationRes = R.drawable.ic_empty_customers,
                icon = Icons.Default.GroupAdd,
                title = "No Customer Groups Yet",
                description = "Organize customers into groups (e.g. VIP Clients, Festival Buyers, Wholesale) for targeted, personalized campaigns.",
                actionButtonLabel = "Create First Group",
                onActionButtonClick = { showCreateDialog = true },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(groups, key = { it.id }) { group ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (group.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = group.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(onClick = {
                                editingGroup = group
                                groupName = group.name
                                groupDesc = group.description
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Group",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { onDeleteGroup(group.id) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Group",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Create Customer Group",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name *") },
                        placeholder = { Text("e.g. VIP Shoppers") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("e.g. Regular buyers") },
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            onCreateGroup(groupName, groupDesc)
                            groupName = ""
                            groupDesc = ""
                            showCreateDialog = false
                        }
                    },
                    enabled = groupName.trim().isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create Group")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    editingGroup?.let { group ->
        AlertDialog(
            onDismissRequest = { editingGroup = null },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Edit Group",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name *") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Description") },
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            onUpdateGroup(group.id, groupName, groupDesc)
                            editingGroup = null
                        }
                    },
                    enabled = groupName.trim().isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingGroup = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
