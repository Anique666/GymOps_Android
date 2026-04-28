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
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
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

            MaterialTheme(colorScheme = inventoryColorScheme()) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        AppBottomBar(selectedItemId = R.id.navInventory) { itemId ->
                            BottomNavHelper.navigate(this@InventoryActivity, R.id.navInventory, itemId)
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showAddDialog = true },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
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
    val colors = MaterialTheme.colorScheme

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
                    color = colors.tertiary
                )
                Text(
                    text = stringResource(R.string.inventory_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primary
                )
                Text(
                    text = stringResource(R.string.inventory_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
        item {
            KpiCard(
                title = stringResource(R.string.inventory_total_assets),
                value = uiState.totalAssets.toString().padStart(2, '0'),
                footer = stringResource(R.string.inventory_total_assets_note),
                icon = Icons.Default.FitnessCenter,
                iconBg = colors.secondaryContainer
            )
        }
        item {
            KpiCard(
                title = stringResource(R.string.inventory_under_maintenance),
                value = uiState.underMaintenanceCount.toString().padStart(2, '0'),
                footer = stringResource(R.string.inventory_under_maintenance_note),
                icon = Icons.Default.Build,
                iconBg = colors.errorContainer
            )
        }
        item {
            KpiCard(
                title = stringResource(R.string.inventory_ready_status),
                value = "${uiState.readyPercent}%",
                footer = stringResource(R.string.inventory_ready_status_note),
                icon = Icons.Default.CheckCircle,
                iconBg = colors.tertiaryContainer
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
                        .background(colors.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.inventory_empty_state),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant
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
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(R.string.a11y_open_navigation),
            tint = colors.onSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.inventory_brand_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.a11y_profile_avatar),
                tint = colors.onPrimary
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
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
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
                    Icon(imageVector = icon, contentDescription = null, tint = colors.primary)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
            }
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
            Text(footer, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
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
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(equipment.category),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = colors.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface,
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
                        color = colors.onSurfaceVariant
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
                    color = colors.onSurfaceVariant
                )
                equipment.usageHours?.let { hours ->
                    Text(
                        text = stringResource(R.string.inventory_usage_hours, hours),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
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
    val colors = MaterialTheme.colorScheme
    val (label, color, bg) = when (status) {
        EquipmentStatus.ACTIVE -> Triple(
            stringResource(R.string.inventory_status_active),
            colors.onSecondaryContainer,
            colors.secondaryContainer
        )

        EquipmentStatus.IN_REPAIR -> Triple(
            stringResource(R.string.inventory_status_in_repair),
            colors.onErrorContainer,
            colors.errorContainer
        )

        EquipmentStatus.MAINTENANCE_DUE -> Triple(
            stringResource(R.string.inventory_status_maintenance_due),
            colors.onTertiaryContainer,
            colors.tertiaryContainer
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
                        color = MaterialTheme.colorScheme.error
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

@Composable
private fun inventoryColorScheme() = if (isSystemInDarkTheme()) {
    darkColorScheme(
        primary = Color(0xFF7ED2C8),
        onPrimary = Color(0xFF062B2A),
        primaryContainer = Color(0xFF1C3B38),
        onPrimaryContainer = Color(0xFFBCEBE5),
        secondary = Color(0xFFFFB66C),
        onSecondary = Color(0xFF111316),
        secondaryContainer = Color(0xFF46301A),
        onSecondaryContainer = Color(0xFFFFDCB8),
        tertiary = Color(0xFF8DD3FF),
        onTertiary = Color(0xFF06202D),
        tertiaryContainer = Color(0xFF203B3B),
        onTertiaryContainer = Color(0xFFD9EFEF),
        background = Color(0xFF111316),
        onBackground = Color(0xFFECEFF2),
        surface = Color(0xFF1A1D20),
        onSurface = Color(0xFFECEFF2),
        surfaceVariant = Color(0xFF24282D),
        onSurfaceVariant = Color(0xFFB6BEC7),
        error = Color(0xFFF28B82),
        errorContainer = Color(0xFF4A2321),
        onErrorContainer = Color(0xFFFFDAD6)
    )
} else {
    lightColorScheme(
        primary = Color(0xFF0A4642),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD9E2E2),
        onPrimaryContainer = Color(0xFF062B2A),
        secondary = Color(0xFFF28C28),
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFFF4E7DB),
        onSecondaryContainer = Color(0xFF6F3C00),
        tertiary = Color(0xFFA54B00),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFDEAD9),
        onTertiaryContainer = Color(0xFF6E3600),
        background = Color(0xFFECEDEE),
        onBackground = Color(0xFF111316),
        surface = Color.White,
        onSurface = Color(0xFF111316),
        surfaceVariant = Color(0xFFE3E8EA),
        onSurfaceVariant = Color(0xFF5F6368),
        error = Color(0xFFB3261E),
        errorContainer = Color(0xFFF9DBD9),
        onErrorContainer = Color(0xFF9D1C1C)
    )
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
