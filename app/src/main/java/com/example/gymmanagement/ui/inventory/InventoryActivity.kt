package com.example.gymmanagement.ui.inventory

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.entity.EquipmentEntity
import com.example.gymmanagement.data.local.entity.EquipmentStatus
import com.example.gymmanagement.ui.common.AppBottomBar
import com.example.gymmanagement.ui.common.BottomNavHelper
import com.example.gymmanagement.utils.DateUtils

class InventoryActivity : AppCompatActivity() {

    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var showAddDialog by rememberSaveable { mutableStateOf(false) }

            MaterialTheme {
                Scaffold(
                    containerColor = Color(0xFFECEDEE),
                    bottomBar = {
                        AppBottomBar(selectedItemId = R.id.navInventory) { itemId ->
                            BottomNavHelper.navigate(this@InventoryActivity, R.id.navInventory, itemId)
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showAddDialog = true },
                            containerColor = Color(0xFFF28C28),
                            contentColor = Color.Black
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.a11y_add_equipment)
                            )
                        }
                    }
                ) { paddingValues ->
                    InventoryScreen(
                        uiState = uiState,
                        contentPadding = paddingValues,
                        onSearchChanged = viewModel::onSearchChanged,
                        onFilterSelected = viewModel::onFilterSelected,
                        onPrimaryAction = viewModel::runPrimaryAction,
                        onStatusSelected = viewModel::updateStatus
                    )

                    if (showAddDialog) {
                        AddEquipmentDialog(
                            onDismiss = { showAddDialog = false },
                            onSave = { draft ->
                                viewModel.addEquipment(draft)
                                showAddDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryScreen(
    uiState: InventoryUiState,
    contentPadding: PaddingValues,
    onSearchChanged: (String) -> Unit,
    onFilterSelected: (EquipmentStatus?) -> Unit,
    onPrimaryAction: (EquipmentEntity) -> Unit,
    onStatusSelected: (EquipmentEntity, EquipmentStatus) -> Unit
) {
    var filterExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { InventoryHeader() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.inventory_label_operations),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFA54B00)
                )
                Text(
                    text = stringResource(R.string.inventory_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0A4642)
                )
                Text(
                    text = stringResource(R.string.inventory_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4E545A)
                )
            }
        }

        item {
            KpiCard(
                title = stringResource(R.string.inventory_total_assets),
                value = uiState.totalAssets.toString().padStart(2, '0'),
                footer = stringResource(R.string.inventory_total_assets_note),
                icon = Icons.Default.FitnessCenter,
                iconBg = Color(0xFFD9E2E2)
            )
        }
        item {
            KpiCard(
                title = stringResource(R.string.inventory_under_maintenance),
                value = uiState.underMaintenanceCount.toString().padStart(2, '0'),
                footer = stringResource(R.string.inventory_under_maintenance_note),
                icon = Icons.Default.Build,
                iconBg = Color(0xFFF4E7DB)
            )
        }
        item {
            KpiCard(
                title = stringResource(R.string.inventory_ready_status),
                value = "${uiState.readyPercent}%",
                footer = stringResource(R.string.inventory_ready_status_note),
                icon = Icons.Default.CheckCircle,
                iconBg = Color(0xFFDCE5E5)
            )
        }

        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.inventory_search_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.a11y_search_equipment)
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            Box {
                OutlinedButton(onClick = { filterExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = stringResource(R.string.a11y_filter_equipment)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val label = when (uiState.selectedFilter) {
                        null -> stringResource(R.string.inventory_filter)
                        EquipmentStatus.ACTIVE -> stringResource(R.string.inventory_status_active)
                        EquipmentStatus.IN_REPAIR -> stringResource(R.string.inventory_status_in_repair)
                        EquipmentStatus.MAINTENANCE_DUE -> stringResource(R.string.inventory_status_maintenance_due)
                    }
                    Text(label)
                }

                DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.inventory_filter_all)) },
                        onClick = {
                            onFilterSelected(null)
                            filterExpanded = false
                        }
                    )
                    EquipmentStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(statusLabel(status)) },
                            onClick = {
                                onFilterSelected(status)
                                filterExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (uiState.equipment.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.inventory_empty_state),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF4E545A)
                    )
                }
            }
        } else {
            items(uiState.equipment, key = { equipment -> equipment.id }) { equipment ->
                EquipmentCard(
                    equipment = equipment,
                    onPrimaryAction = { onPrimaryAction(equipment) },
                    onStatusSelected = { status -> onStatusSelected(equipment, status) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun InventoryHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(R.string.a11y_open_navigation),
            tint = Color(0xFF111316)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.inventory_brand_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111316)
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF0A4642)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.a11y_profile_avatar),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    footer: String,
    icon: ImageVector,
    iconBg: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF0A4642))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF111316))
            }
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF062B2A))
            Text(footer, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5F6368))
        }
    }
}

@Composable
fun EquipmentCard(
    equipment: EquipmentEntity,
    onPrimaryAction: () -> Unit,
    onStatusSelected: (EquipmentStatus) -> Unit
) {
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE3E8EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(equipment.category),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = Color(0xFF0A4642)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF111316),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(
                            R.string.inventory_serial_and_category,
                            equipment.serialNumber,
                            equipment.category
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5F6368)
                    )
                    StatusBadge(status = equipment.status)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val dateLabel = if (equipment.status == EquipmentStatus.IN_REPAIR) {
                    stringResource(R.string.inventory_estimated_finish)
                } else {
                    stringResource(R.string.inventory_last_service)
                }
                Text(
                    text = "$dateLabel: ${DateUtils.formatCardDate(equipment.lastServiceDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4E545A)
                )
                equipment.usageHours?.let { hours ->
                    Text(
                        text = stringResource(R.string.inventory_usage_hours, hours),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4E545A)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(actionLabel(equipment.status))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.a11y_equipment_overflow),
                        modifier = Modifier.clickable { statusMenuExpanded = true }
                    )

                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        EquipmentStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(statusLabel(status)) },
                                onClick = {
                                    onStatusSelected(status)
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: EquipmentStatus) {
    val (label, color, bg) = when (status) {
        EquipmentStatus.ACTIVE -> Triple(
            stringResource(R.string.inventory_status_active),
            Color(0xFF0A7B58),
            Color(0xFFDDF4EA)
        )

        EquipmentStatus.IN_REPAIR -> Triple(
            stringResource(R.string.inventory_status_in_repair),
            Color(0xFFB3261E),
            Color(0xFFF9DBD9)
        )

        EquipmentStatus.MAINTENANCE_DUE -> Triple(
            stringResource(R.string.inventory_status_maintenance_due),
            Color(0xFFA54B00),
            Color(0xFFFDEAD9)
        )
    }

    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = bg,
            labelColor = color
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEquipmentDialog(
    onDismiss: () -> Unit,
    onSave: (AddEquipmentDraft) -> Unit
) {
    val categories = remember {
        listOf("Cardio", "Strength", "Mobility", "Functional", "Recovery")
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    var name by rememberSaveable { mutableStateOf("") }
    var serialNumber by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(categories.first()) }
    var selectedStatus by rememberSaveable { mutableStateOf(EquipmentStatus.ACTIVE) }
    var purchaseDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }

    val nameRequiredError = stringResource(R.string.inventory_error_name_required)
    val serialRequiredError = stringResource(R.string.inventory_error_serial_required)
    val categoryRequiredError = stringResource(R.string.inventory_error_category_required)
    val purchaseDateRequiredError = stringResource(R.string.inventory_error_purchase_date_required)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.inventory_add_equipment_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.inventory_equipment_name)) }
                )

                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.inventory_serial_number)) }
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true },
                        readOnly = true,
                        label = { Text(stringResource(R.string.inventory_category)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                modifier = Modifier.clickable { categoryExpanded = true }
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = purchaseDate?.let { DateUtils.formatDate(it) }
                        ?: stringResource(R.string.inventory_pick_purchase_date),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    readOnly = true,
                    label = { Text(stringResource(R.string.inventory_purchase_date)) }
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = statusLabel(selectedStatus),
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { statusExpanded = true },
                        readOnly = true,
                        label = { Text(stringResource(R.string.inventory_status)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                modifier = Modifier.clickable { statusExpanded = true }
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        EquipmentStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(statusLabel(status)) },
                                onClick = {
                                    selectedStatus = status
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    label = { Text(stringResource(R.string.inventory_notes_optional)) }
                )

                formError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB3261E)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val selectedDate = purchaseDate
                val error = when {
                    name.isBlank() -> nameRequiredError
                    serialNumber.isBlank() -> serialRequiredError
                    selectedCategory.isBlank() -> categoryRequiredError
                    selectedDate == null -> purchaseDateRequiredError
                    else -> null
                }

                if (error != null) {
                    formError = error
                } else {
                    val purchaseDateValue = selectedDate ?: return@TextButton
                    onSave(
                        AddEquipmentDraft(
                            name = name,
                            serialNumber = serialNumber,
                            category = selectedCategory,
                            purchaseDate = purchaseDateValue,
                            status = selectedStatus,
                            notes = notes
                        )
                    )
                }
            }) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = purchaseDate ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    purchaseDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun categoryIcon(category: String): ImageVector {
    val normalized = category.lowercase()
    return when {
        normalized.contains("cardio") -> Icons.Default.FitnessCenter
        normalized.contains("strength") -> Icons.Default.Build
        normalized.contains("functional") -> Icons.Default.WarningAmber
        else -> Icons.Default.FitnessCenter
    }
}

@Composable
private fun statusLabel(status: EquipmentStatus): String {
    return when (status) {
        EquipmentStatus.ACTIVE -> stringResource(R.string.inventory_status_active)
        EquipmentStatus.IN_REPAIR -> stringResource(R.string.inventory_status_in_repair)
        EquipmentStatus.MAINTENANCE_DUE -> stringResource(R.string.inventory_status_maintenance_due)
    }
}

@Composable
private fun actionLabel(status: EquipmentStatus): String {
    return when (status) {
        EquipmentStatus.ACTIVE -> stringResource(R.string.inventory_action_inspect)
        EquipmentStatus.IN_REPAIR -> stringResource(R.string.inventory_action_view_ticket)
        EquipmentStatus.MAINTENANCE_DUE -> stringResource(R.string.inventory_action_maintenance)
    }
}
