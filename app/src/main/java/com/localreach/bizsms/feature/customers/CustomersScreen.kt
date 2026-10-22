package com.localreach.bizsms.feature.customers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import com.localreach.bizsms.core.database.CustomerEntity
import com.localreach.bizsms.core.database.CustomerGroupEntity
import com.localreach.bizsms.core.database.SuppressionEntryEntity
import com.localreach.bizsms.domain.validation.CsvImportSummary
import com.localreach.bizsms.feature.common.StatusBadge

/**
 * Screen 4: Customer Directory Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDirectoryScreen(
    customers: List<CustomerEntity>,
    groups: List<CustomerGroupEntity>,
    onAddCustomer: (String, String, String, String, String, String, () -> Unit) -> Unit,
    onUpdateCustomer: (CustomerEntity, String, String, String, String, String, () -> Unit) -> Unit,
    onDeleteCustomer: (String) -> Unit,
    onOptOut: (CustomerEntity, String) -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToGroups: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedConsentFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var optOutTarget by remember { mutableStateOf<CustomerEntity?>(null) }

    val filteredCustomers = remember(customers, searchQuery, selectedConsentFilter) {
        customers.filter { c ->
            val matchesQuery = searchQuery.isBlank() ||
                c.fullName.contains(searchQuery, ignoreCase = true) ||
                c.normalizedPhoneE164.contains(searchQuery) ||
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Customer Directory",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onNavigateToImport) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import")
                    }
                    OutlinedButton(onClick = onNavigateToGroups) {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Groups")
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name, phone, tag, or notes") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Consent Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedConsentFilter == "ALL",
                    onClick = { selectedConsentFilter = "ALL" },
                    label = { Text("All (${customers.size})") }
                )
                FilterChip(
                    selected = selectedConsentFilter == "GRANTED",
                    onClick = { selectedConsentFilter = "GRANTED" },
                    label = { Text("Opted-In (${customers.count { it.consentStatus == "GRANTED" && !it.isOptedOut }})") }
                )
                FilterChip(
                    selected = selectedConsentFilter == "UNKNOWN",
                    onClick = { selectedConsentFilter = "UNKNOWN" },
                    label = { Text("Unknown (${customers.count { it.consentStatus == "UNKNOWN" && !it.isOptedOut }})") }
                )
                FilterChip(
                    selected = selectedConsentFilter == "OPTED_OUT",
                    onClick = { selectedConsentFilter = "OPTED_OUT" },
                    label = { Text("Suppressed (${customers.count { it.isOptedOut || it.consentStatus == "OPTED_OUT" }})") }
                )
            }

            if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No customers found", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onEdit = { editingCustomer = customer },
                            onDelete = { onDeleteCustomer(customer.id) },
                            onOptOut = { optOutTarget = customer }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CustomerEditDialog(
            customer = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, tags, notes, consent ->
                onAddCustomer(name, phone, tags, notes, consent, "MANUAL_ENTRY") {
                    showAddDialog = false
                }
            }
        )
    }

    editingCustomer?.let { customer ->
        CustomerEditDialog(
            customer = customer,
            onDismiss = { editingCustomer = null },
            onConfirm = { name, phone, tags, notes, consent ->
                onUpdateCustomer(customer, name, phone, tags, notes, consent) {
                    editingCustomer = null
                }
            }
        )
    }

    optOutTarget?.let { customer ->
        AlertDialog(
            onDismissRequest = { optOutTarget = null },
            title = { Text("Permanent Opt-Out & Suppression") },
            text = {
                Text(
                    "Permanently mark ${customer.fullName} (${customer.normalizedPhoneE164}) as OPTED-OUT? " +
                        "This will add their number to the active Suppression Firewall, cancel any pending campaign messages, and permanently block future SMS dispatches."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onOptOut(customer, "Customer requested opt-out")
                        optOutTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Permanent Opt-Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { optOutTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CustomerCard(
    customer: CustomerEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOptOut: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(customer.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(customer.normalizedPhoneE164, style = MaterialTheme.typography.bodyMedium)
                }
                StatusBadge(status = if (customer.isOptedOut) "OPTED_OUT" else customer.consentStatus)
            }

            if (customer.tagsCsv.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Tags: ${customer.tagsCsv}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }

            if (customer.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Note: ${customer.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!customer.isOptedOut && customer.consentStatus != "OPTED_OUT") {
                    TextButton(onClick = onOptOut) {
                        Text("Opt-Out", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun CustomerEditDialog(
    customer: CustomerEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(customer?.fullName ?: "") }
    var phone by remember { mutableStateOf(customer?.rawPhoneNumber ?: "") }
    var tags by remember { mutableStateOf(customer?.tagsCsv ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }
    var consent by remember { mutableStateOf(customer?.consentStatus ?: "GRANTED") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "Add Customer" else "Edit Customer") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number * (e.g. 9876543210 or +91...)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated, e.g. vip, retail)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Avoid sensitive financial/health PII)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Consent Status", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = consent == "GRANTED",
                        onClick = { consent = "GRANTED" },
                        label = { Text("Opted-In") }
                    )
                    FilterChip(
                        selected = consent == "UNKNOWN",
                        onClick = { consent = "UNKNOWN" },
                        label = { Text("Unknown") }
                    )
                    FilterChip(
                        selected = consent == "OPTED_OUT",
                        onClick = { consent = "OPTED_OUT" },
                        label = { Text("Opted-Out") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, phone, tags, notes, consent) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Screen 5: Customer Import Screen (Contacts + CSV)
 */
@Composable
fun CustomerImportScreen(
    preview: CsvImportSummary?,
    onPickContact: (Uri) -> Unit,
    onImportCsv: (Uri) -> Unit,
    onExportCsv: (Uri) -> Unit,
    onClearPreview: () -> Unit,
    onBack: () -> Unit
) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Customer Import & Export", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("System Contact Picker", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Import individual contacts directly through Android's zero-permission system picker. " +
                        "This app never reads your full address book.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { contactPickerLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Contact via System Picker")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("SAF CSV Import", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Import customer lists from a CSV file. Supported columns: Name, Phone, Tags, Consent_Status, Notes. " +
                        "Duplicates and permanently opted-out suppressed numbers will be automatically checked.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { csvImportLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose CSV File to Import")
                }
            }
        }

        if (preview != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Live CSV Import Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("• Valid Customers Added: ${preview.validCustomers.size}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text("• Duplicate Phones In File: ${preview.duplicatePhonesInFile.size}")
                    Text("• Opt-Out Suppressed Skipped: ${preview.suppressedSkippedCount}", color = MaterialTheme.colorScheme.error)
                    Text("• Invalid Rows Rejection: ${preview.invalidRows.size}")

                    if (preview.invalidRows.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Rejected Rows:", fontWeight = FontWeight.SemiBold)
                        preview.invalidRows.take(5).forEach { (line, reason) ->
                            Text("Line $line: $reason", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onClearPreview,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Acknowledge & Close Preview")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Customer Directory Export", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Export your customer directory to a CSV file on your device via Storage Access Framework.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { showExportWarningDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export All Customers to CSV")
                }
            }
        }
    }

    if (showExportWarningDialog) {
        AlertDialog(
            onDismissRequest = { showExportWarningDialog = false },
            title = { Text("CSV Export Privacy Warning") },
            text = {
                Text(
                    "Exported CSV files are unencrypted and contain your customers' personal contact details, notes, and consent history. " +
                        "Ensure you store the exported file securely on your device."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showExportWarningDialog = false
                    csvExportLauncher.launch("localreach_customers_export.csv")
                }) {
                    Text("Proceed with Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportWarningDialog = false }) { Text("Cancel") }
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Customer Groups", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Button(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Group")
            }
        }

        if (groups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No groups created yet. Group customers to send targeted campaigns.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(groups, key = { it.id }) { group ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(group.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                if (group.description.isNotBlank()) {
                                    Text(group.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = {
                                editingGroup = group
                                groupName = group.name
                                groupDesc = group.description
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { onDeleteGroup(group.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
            title = { Text("Create Customer Group") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (groupName.isNotBlank()) {
                        onCreateGroup(groupName, groupDesc)
                        groupName = ""; groupDesc = ""
                        showCreateDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    editingGroup?.let { group ->
        AlertDialog(
            onDismissRequest = { editingGroup = null },
            title = { Text("Edit Group") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (groupName.isNotBlank()) {
                        onUpdateGroup(group.id, groupName, groupDesc)
                        editingGroup = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingGroup = null }) { Text("Cancel") }
            }
        )
    }
}
