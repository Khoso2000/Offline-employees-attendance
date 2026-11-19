package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.LeaveRequestEntity
import com.example.data.model.EmploymentStatus
import com.example.data.model.LeaveCategory
import com.example.data.model.LeaveStatus
import com.example.domain.DateUtils
import com.example.domain.PayrollCalculator
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.LeaveStatusBadge
import com.example.ui.components.SaasCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.LeaveBlue
import com.example.ui.theme.PresentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(
    uiState: StaffTrackUiState,
    onSubmitLeave: (EmployeeEntity, LeaveCategory, String, String, String) -> Unit,
    onApproveLeave: (LeaveRequestEntity, String) -> Unit,
    onRejectLeave: (LeaveRequestEntity, String) -> Unit,
    onDeleteLeave: (LeaveRequestEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var statusFilter by remember { mutableStateOf<LeaveStatus?>(null) }
    var balanceEmpId by remember { mutableStateOf<Long?>(null) }
    var balanceDropdownOpen by remember { mutableStateOf(false) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var pendingConfirm by remember { mutableStateOf<Triple<LeaveRequestEntity, String, Boolean>?>(null) }

    val empMap = remember(uiState.employees) { uiState.employees.associateBy { it.id } }
    val activeEmployees = remember(uiState.employees) { uiState.employees.filter { it.status == EmploymentStatus.ACTIVE } }
    val selectedBalanceEmp = remember(balanceEmpId, activeEmployees) {
        balanceEmpId?.let { id -> activeEmployees.firstOrNull { it.id == id } } ?: activeEmployees.firstOrNull()
    }

    val filteredLeaves = remember(uiState.leaves, statusFilter) {
        uiState.leaves.filter { statusFilter == null || it.status == statusFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showApplyDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("apply_leave_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Apply Leave")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Leave Balances Card
            if (selectedBalanceEmp != null) {
                item {
                    val balances = PayrollCalculator.calculateLeaveBalances(selectedBalanceEmp, uiState.leaves)
                    SaasCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(
                                title = "Leave Balances",
                                subtitle = "Automatic annual/sick/casual calculation"
                            )
                            ExposedDropdownMenuBox(
                                expanded = balanceDropdownOpen,
                                onExpandedChange = { balanceDropdownOpen = it }
                            ) {
                                OutlinedTextField(
                                    value = "${selectedBalanceEmp.fullName} (${selectedBalanceEmp.employeeCode})",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Employee") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = balanceDropdownOpen) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                )
                                ExposedDropdownMenu(
                                    expanded = balanceDropdownOpen,
                                    onDismissRequest = { balanceDropdownOpen = false }
                                ) {
                                    activeEmployees.forEach { emp ->
                                        DropdownMenuItem(
                                            text = { Text("${emp.fullName} (${emp.employeeCode})") },
                                            onClick = {
                                                balanceEmpId = emp.id
                                                balanceDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                balances.forEach { bal ->
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = bal.category.label.replace(" Leave", ""),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${bal.remaining} / ${bal.allowance} left",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (bal.used.toFloat() / bal.allowance.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = statusFilter == null,
                            onClick = { statusFilter = null },
                            label = { Text("All (${uiState.leaves.size})") }
                        )
                    }
                    items(LeaveStatus.entries) { st ->
                        val cnt = uiState.leaves.count { it.status == st }
                        FilterChip(
                            selected = statusFilter == st,
                            onClick = { statusFilter = if (statusFilter == st) null else st },
                            label = { Text("${st.label} ($cnt)") }
                        )
                    }
                }
            }

            if (filteredLeaves.isEmpty()) {
                item {
                    SaasCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.BeachAccess, contentDescription = null, modifier = Modifier.size(44.dp), tint = LeaveBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No leave records in this view", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Tap + to apply for employee leave.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filteredLeaves, key = { it.id }) { leave ->
                    val emp = empMap[leave.employeeId]
                    val empName = emp?.fullName ?: "Employee #${leave.employeeId}"
                    SaasCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                EmployeeAvatar(fullName = empName, photoUri = emp?.photoUri, size = 40.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(empName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${leave.category.label} • ${leave.totalDays} day(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                LeaveStatusBadge(leave.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${DateUtils.formatDisplayDate(leave.startDate, uiState.settings.dateFormat)} → ${DateUtils.formatDisplayDate(leave.endDate, uiState.settings.dateFormat)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                StatusBadge(
                                    text = if (leave.isPaid && leave.category != LeaveCategory.UNPAID) "Paid" else "Unpaid (Deducts)",
                                    fgColor = if (leave.isPaid && leave.category != LeaveCategory.UNPAID) PresentGreen else AbsentRed,
                                    bgColor = (if (leave.isPaid && leave.category != LeaveCategory.UNPAID) PresentGreen else AbsentRed).copy(alpha = 0.14f)
                                )
                            }
                            if (leave.reason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = leave.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (leave.status == LeaveStatus.PENDING) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { pendingConfirm = Triple(leave, empName, true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PresentGreen, contentColor = Color.White),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("approve_leave_${leave.id}")
                                        ) {
                                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve")
                                        }
                                        OutlinedButton(
                                            onClick = { pendingConfirm = Triple(leave, empName, false) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = AbsentRed)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject", color = AbsentRed)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = if (leave.status == LeaveStatus.APPROVED) "Synced to Attendance" else "No attendance impact",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onDeleteLeave(leave) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete leave", tint = AbsentRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showApplyDialog && activeEmployees.isNotEmpty()) {
        ApplyLeaveDialog(
            employees = activeEmployees,
            defaultDate = uiState.todayDate,
            onSubmit = { emp, cat, start, end, reason ->
                onSubmitLeave(emp, cat, start, end, reason)
                showApplyDialog = false
            },
            onDismiss = { showApplyDialog = false }
        )
    }

    pendingConfirm?.let { (lv, name, isApprove) ->
        com.example.ui.components.ConfirmActionDialog(
            title = if (isApprove) "Approve Leave Request?" else "Reject Leave Request?",
            message = if (isApprove) {
                "Approve ${lv.totalDays} day(s) of ${lv.category.label} for $name? Attendance will update automatically."
            } else {
                "Reject ${lv.category.label} request from $name?"
            },
            confirmLabel = if (isApprove) "Approve" else "Reject",
            isDestructive = !isApprove,
            onConfirm = {
                if (isApprove) onApproveLeave(lv, name) else onRejectLeave(lv, name)
                pendingConfirm = null
            },
            onDismiss = { pendingConfirm = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplyLeaveDialog(
    employees: List<EmployeeEntity>,
    defaultDate: String,
    onSubmit: (EmployeeEntity, LeaveCategory, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedEmp by remember { mutableStateOf(employees.first()) }
    var empExpanded by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(LeaveCategory.CASUAL) }
    var catExpanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(defaultDate) }
    var endDate by remember { mutableStateOf(defaultDate) }
    var reason by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val computedDays = remember(startDate, endDate) {
        DateUtils.datesBetween(startDate, endDate).size
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply Employee Leave", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg != null) {
                    Text(errorMsg!!, color = AbsentRed, style = MaterialTheme.typography.bodySmall)
                }

                ExposedDropdownMenuBox(
                    expanded = empExpanded,
                    onExpandedChange = { empExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedEmp.fullName} (${selectedEmp.employeeCode})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Employee") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = empExpanded,
                        onDismissRequest = { empExpanded = false }
                    ) {
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text("${emp.fullName} (${emp.employeeCode})") },
                                onClick = { selectedEmp = emp; empExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Leave Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        LeaveCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.label} (${if (cat.defaultPaid) "Paid" else "Unpaid"})") },
                                onClick = { category = cat; catExpanded = false }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it; errorMsg = null },
                        label = { Text("Start (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it; errorMsg = null },
                        label = { Text("End (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = if (computedDays > 0) "Duration: $computedDays day(s)" else "Invalid date range",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (computedDays > 0) MaterialTheme.colorScheme.primary else AbsentRed
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (computedDays <= 0) {
                    errorMsg = "End date must be on or after start date (yyyy-MM-dd)."
                } else {
                    onSubmit(selectedEmp, category, startDate.trim(), endDate.trim(), reason.trim())
                }
            }) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
