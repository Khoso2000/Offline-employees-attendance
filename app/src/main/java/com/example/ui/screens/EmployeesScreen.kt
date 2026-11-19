package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.EmployeeEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.EmploymentStatus
import com.example.data.model.SalaryType
import com.example.domain.DateUtils
import com.example.domain.PayrollCalculator
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.SaasCard
import com.example.ui.components.StatusBadge
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.PresentGreen
import com.example.util.ReportExporter

@Composable
fun EmployeesScreen(
    uiState: StaffTrackUiState,
    openAddImmediately: Boolean,
    editEmployeeId: Long? = null,
    onEditHandled: () -> Unit = {},
    onAddHandled: () -> Unit,
    onOpenEmployeeDetails: (Long) -> Unit,
    onGenerateCode: suspend () -> String,
    onSaveEmployee: (EmployeeEntity) -> Unit,
    onToggleStatus: (EmployeeEntity) -> Unit,
    onDeleteEmployee: (EmployeeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("All") }
    var statusFilter by remember { mutableStateOf<EmploymentStatus?>(null) }

    var editingEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var generatedCode by remember { mutableStateOf("ST-1001") }
    var viewingProfile by remember { mutableStateOf<EmployeeEntity?>(null) }
    var deletingEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }

    LaunchedEffect(openAddImmediately) {
        if (openAddImmediately) {
            generatedCode = onGenerateCode()
            editingEmployee = null
            showFormDialog = true
            onAddHandled()
        }
    }

    LaunchedEffect(editEmployeeId, uiState.employees) {
        if (editEmployeeId != null) {
            uiState.employees.firstOrNull { it.id == editEmployeeId }?.let { target ->
                editingEmployee = target
                showFormDialog = true
            }
            onEditHandled()
        }
    }

    val departments = remember(uiState.employees) {
        listOf("All") + uiState.employees.map { it.department }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredEmployees = remember(uiState.employees, searchQuery, selectedDept, statusFilter) {
        uiState.employees.filter { emp ->
            val matchesQuery = searchQuery.isBlank() ||
                emp.fullName.contains(searchQuery, ignoreCase = true) ||
                emp.employeeCode.contains(searchQuery, ignoreCase = true) ||
                emp.designation.contains(searchQuery, ignoreCase = true) ||
                emp.phone.contains(searchQuery, ignoreCase = true)
            val matchesDept = selectedDept == "All" || emp.department == selectedDept
            val matchesStatus = statusFilter == null || emp.status == statusFilter
            matchesQuery && matchesDept && matchesStatus
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEmployee = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_employee_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Employee")
            }
        }
    ) { innerPadding ->
        LaunchedEffect(showFormDialog, editingEmployee) {
            if (showFormDialog && editingEmployee == null) {
                generatedCode = onGenerateCode()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Filter header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name, ID, role or phone...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_search_input")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(departments) { dept ->
                        FilterChip(
                            selected = selectedDept == dept,
                            onClick = { selectedDept = dept },
                            label = { Text(dept) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == EmploymentStatus.ACTIVE,
                            onClick = {
                                statusFilter = if (statusFilter == EmploymentStatus.ACTIVE) null else EmploymentStatus.ACTIVE
                            },
                            label = { Text("Active Only") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == EmploymentStatus.INACTIVE,
                            onClick = {
                                statusFilter = if (statusFilter == EmploymentStatus.INACTIVE) null else EmploymentStatus.INACTIVE
                            },
                            label = { Text("Inactive") }
                        )
                    }
                }
            }

            if (filteredEmployees.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No matching employees found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjust filters or tap + to register a new employee.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredEmployees, key = { it.id }) { emp ->
                        SaasCard(
                            onClick = { onOpenEmployeeDetails(emp.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("employee_card_${emp.employeeCode}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    EmployeeAvatar(
                                        fullName = emp.fullName,
                                        photoUri = emp.photoUri,
                                        size = 48.dp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = emp.fullName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Text(
                                                text = emp.employeeCode,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = "${emp.designation} • ${emp.department}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val isActive = emp.status == EmploymentStatus.ACTIVE
                                            StatusBadge(
                                                text = emp.status.label,
                                                fgColor = if (isActive) PresentGreen else AbsentRed,
                                                bgColor = (if (isActive) PresentGreen else AbsentRed).copy(alpha = 0.14f)
                                            )
                                            Text(
                                                text = "${DateUtils.formatCurrency(emp.basicSalary, uiState.settings.currency)} / ${if (emp.salaryType == SalaryType.MONTHLY) "mo" else "day"}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = emp.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(onClick = {
                                            editingEmployee = emp
                                            showFormDialog = true
                                        }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = "Edit employee")
                                        }
                                        IconButton(onClick = { onToggleStatus(emp) }) {
                                            Icon(
                                                Icons.Rounded.PowerSettingsNew,
                                                contentDescription = "Toggle active status",
                                                tint = if (emp.status == EmploymentStatus.ACTIVE) PresentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { deletingEmployee = emp }) {
                                            Icon(
                                                Icons.Rounded.Delete,
                                                contentDescription = "Delete employee",
                                                tint = AbsentRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        EmployeeFormDialog(
            initial = editingEmployee,
            autoEmployeeCode = generatedCode,
            defaultAnnualLeave = uiState.settings.defaultAnnualLeave,
            defaultSickLeave = uiState.settings.defaultSickLeave,
            defaultCasualLeave = uiState.settings.defaultCasualLeave,
            currency = uiState.settings.currency,
            onSave = {
                onSaveEmployee(it)
                showFormDialog = false
            },
            onDismiss = { showFormDialog = false }
        )
    }

    viewingProfile?.let { emp ->
        EmployeeProfileDialog(
            employee = emp,
            uiState = uiState,
            onEdit = {
                viewingProfile = null
                editingEmployee = emp
                showFormDialog = true
            },
            onDismiss = { viewingProfile = null }
        )
    }

    deletingEmployee?.let { emp ->
        ConfirmActionDialog(
            title = "Delete Employee?",
            message = "This will permanently remove ${emp.fullName} (${emp.employeeCode}) and all linked attendance, leave, and payroll records.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteEmployee(emp)
                deletingEmployee = null
            },
            onDismiss = { deletingEmployee = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeFormDialog(
    initial: EmployeeEntity?,
    autoEmployeeCode: String,
    defaultAnnualLeave: Int,
    defaultSickLeave: Int,
    defaultCasualLeave: Int,
    currency: String,
    onSave: (EmployeeEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var fullName by remember(initial) { mutableStateOf(initial?.fullName ?: "") }
    val employeeCode = remember(initial, autoEmployeeCode) { initial?.employeeCode ?: autoEmployeeCode }
    var photoUri by remember(initial) { mutableStateOf(initial?.photoUri) }
    var phone by remember(initial) { mutableStateOf(initial?.phone ?: "") }
    var email by remember(initial) { mutableStateOf(initial?.email ?: "") }
    var address by remember(initial) { mutableStateOf(initial?.address ?: "") }
    var department by remember(initial) { mutableStateOf(initial?.department ?: "Engineering") }
    var designation by remember(initial) { mutableStateOf(initial?.designation ?: "") }
    var joiningDate by remember(initial) { mutableStateOf(initial?.joiningDate ?: DateUtils.todayIso()) }
    var salaryType by remember(initial) { mutableStateOf(initial?.salaryType ?: SalaryType.MONTHLY) }
    var basicSalaryText by remember(initial) { mutableStateOf(initial?.basicSalary?.toInt()?.toString() ?: "") }
    var workingHoursText by remember(initial) { mutableStateOf((initial?.workingHoursPerDay ?: 8.0).toString()) }
    var weeklyHoliday by remember(initial) { mutableStateOf(initial?.weeklyHoliday ?: "SUNDAY") }
    var status by remember(initial) { mutableStateOf(initial?.status ?: EmploymentStatus.ACTIVE) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var deptExpanded by remember { mutableStateOf(false) }
    var holidayExpanded by remember { mutableStateOf(false) }
    val deptOptions = listOf("Engineering", "Sales", "Operations", "Finance", "Human Resources", "Marketing", "Administration")
    val holidayOptions = listOf("SUNDAY", "FRIDAY", "SATURDAY", "SATURDAY,SUNDAY", "NONE")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ReportExporter.copyUriToInternalFile(context, uri, "emp_${employeeCode}")
            if (savedPath != null) photoUri = savedPath
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Add Employee ($employeeCode)" else "Edit Employee ($employeeCode)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EmployeeAvatar(fullName = fullName.ifBlank { "New" }, photoUri = photoUri, size = 56.dp)
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (photoUri == null) "Upload Photo" else "Change Photo")
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = AbsentRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMsg = null },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emp_form_name")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = joiningDate,
                        onValueChange = { joiningDate = it },
                        label = { Text("Join (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = deptExpanded,
                    onExpandedChange = { deptExpanded = it }
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false }
                    ) {
                        deptOptions.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = { department = d; deptExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation / Role *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Salary Type", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SalaryType.entries.forEachIndexed { idx, type ->
                        SegmentedButton(
                            selected = salaryType == type,
                            onClick = { salaryType = type },
                            shape = SegmentedButtonDefaults.itemShape(idx, SalaryType.entries.size)
                        ) {
                            Text(type.label)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = basicSalaryText,
                        onValueChange = { basicSalaryText = it; errorMsg = null },
                        label = { Text("Basic Salary ($currency) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("emp_form_salary")
                    )
                    OutlinedTextField(
                        value = workingHoursText,
                        onValueChange = { workingHoursText = it },
                        label = { Text("Hours/Day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = holidayExpanded,
                    onExpandedChange = { holidayExpanded = it }
                ) {
                    OutlinedTextField(
                        value = weeklyHoliday,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Weekly Holiday") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = holidayExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = holidayExpanded,
                        onDismissRequest = { holidayExpanded = false }
                    ) {
                        holidayOptions.forEach { h ->
                            DropdownMenuItem(
                                text = { Text(h) },
                                onClick = { weeklyHoliday = h; holidayExpanded = false }
                            )
                        }
                    }
                }

                Text("Employment Status", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    EmploymentStatus.entries.forEachIndexed { idx, st ->
                        SegmentedButton(
                            selected = status == st,
                            onClick = { status = st },
                            shape = SegmentedButtonDefaults.itemShape(idx, EmploymentStatus.entries.size)
                        ) {
                            Text(st.label, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salaryVal = basicSalaryText.trim().toDoubleOrNull()
                    val hoursVal = workingHoursText.trim().toDoubleOrNull() ?: 8.0
                    when {
                        fullName.isBlank() -> errorMsg = "Full name is required."
                        designation.isBlank() -> errorMsg = "Designation is required."
                        salaryVal == null || salaryVal <= 0.0 -> errorMsg = "Enter a valid positive salary."
                        else -> {
                            onSave(
                                EmployeeEntity(
                                    id = initial?.id ?: 0,
                                    employeeCode = employeeCode,
                                    fullName = fullName.trim(),
                                    photoUri = photoUri,
                                    phone = phone.trim().ifBlank { "N/A" },
                                    email = email.trim(),
                                    address = address.trim(),
                                    department = department.trim().ifBlank { "General" },
                                    designation = designation.trim(),
                                    joiningDate = joiningDate.trim().ifBlank { DateUtils.todayIso() },
                                    salaryType = salaryType,
                                    basicSalary = salaryVal,
                                    workingHoursPerDay = hoursVal.coerceIn(1.0, 24.0),
                                    weeklyHoliday = weeklyHoliday,
                                    status = status,
                                    annualLeaveAllowance = initial?.annualLeaveAllowance ?: defaultAnnualLeave,
                                    sickLeaveAllowance = initial?.sickLeaveAllowance ?: defaultSickLeave,
                                    casualLeaveAllowance = initial?.casualLeaveAllowance ?: defaultCasualLeave,
                                    createdAt = initial?.createdAt ?: System.currentTimeMillis()
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.testTag("emp_form_save")
            ) {
                Text(if (initial == null) "Save Employee" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EmployeeProfileDialog(
    employee: EmployeeEntity,
    uiState: StaffTrackUiState,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    val monthAtt = uiState.attendance.filter {
        it.employeeId == employee.id && it.date.startsWith(uiState.currentMonth)
    }
    val balances = PayrollCalculator.calculateLeaveBalances(employee, uiState.leaves)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EmployeeAvatar(fullName = employee.fullName, photoUri = employee.photoUri, size = 52.dp)
                Column {
                    Text(employee.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${employee.employeeCode} • ${employee.designation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileInfoRow(Icons.Rounded.Badge, "Department", "${employee.department} (${employee.status.label})")
                ProfileInfoRow(Icons.Rounded.Phone, "Phone", employee.phone)
                if (employee.email.isNotBlank()) ProfileInfoRow(Icons.Rounded.Email, "Email", employee.email)
                if (employee.address.isNotBlank()) ProfileInfoRow(Icons.Rounded.LocationOn, "Address", employee.address)

                HorizontalDivider()
                Text("Compensation & Schedule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "${employee.salaryType.label}: ${DateUtils.formatCurrency(employee.basicSalary, uiState.settings.currency)} • ${employee.workingHoursPerDay} hrs/day • Off: ${employee.weeklyHoliday}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Joined: ${DateUtils.formatDisplayDate(employee.joiningDate, uiState.settings.dateFormat)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()
                Text("This Month Attendance (${DateUtils.formatMonthTitle(uiState.currentMonth)})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "Present: ${monthAtt.count { it.status == AttendanceStatus.PRESENT }}  |  Late: ${monthAtt.count { it.status == AttendanceStatus.LATE }}  |  Half: ${monthAtt.count { it.status == AttendanceStatus.HALF_DAY }}  |  Absent: ${monthAtt.count { it.status == AttendanceStatus.ABSENT }}  |  OT: ${String.format("%.1f", monthAtt.sumOf { it.overtimeHours })}h",
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider()
                Text("Leave Balances (This Year)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                balances.forEach { bal ->
                    Text(
                        text = "${bal.category.label}: ${bal.remaining} remaining (${bal.used} / ${bal.allowance} used)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Text("$label: ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
