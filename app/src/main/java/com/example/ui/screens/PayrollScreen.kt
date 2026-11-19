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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.PayrollRecordEntity
import com.example.data.model.PaymentStatus
import com.example.domain.DateUtils
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.SaasCard
import com.example.ui.components.StatusBadge
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.LateAmber
import com.example.ui.theme.PresentGreen
import com.example.util.ReportExporter
import com.example.util.ReportTableData

@Composable
fun PayrollScreen(
    uiState: StaffTrackUiState,
    onGeneratePayroll: (String) -> Unit,
    onUpdatePayroll: (PayrollRecordEntity, EmployeeEntity, Double, Double, Double, Double, Double, Boolean, String) -> Unit,
    onApproveAll: (String) -> Unit,
    onOpenPayslip: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf(uiState.currentMonth) }
    var editingPair by remember { mutableStateOf<Pair<PayrollRecordEntity, EmployeeEntity>?>(null) }
    var slipPair by remember { mutableStateOf<Pair<PayrollRecordEntity, EmployeeEntity>?>(null) }
    var showApproveAllConfirm by remember { mutableStateOf(false) }

    val empMap = remember(uiState.employees) { uiState.employees.associateBy { it.id } }
    val monthRecords = remember(uiState.payrolls, selectedMonth) {
        uiState.payrolls.filter { it.yearMonth == selectedMonth }
    }
    val cur = uiState.settings.currency

    val totalNet = monthRecords.sumOf { it.netPayableSalary }
    val totalPaid = monthRecords.sumOf { it.paidAmount }
    val totalPending = (totalNet - totalPaid).coerceAtLeast(0.0)
    val approvedCount = monthRecords.count { it.isApproved }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month Navigator & Payroll Summary
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedMonth = DateUtils.shiftMonth(selectedMonth, -1) }) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = DateUtils.formatMonthTitle(selectedMonth),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Basis: ${uiState.settings.payrollBasis.label}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { selectedMonth = DateUtils.shiftMonth(selectedMonth, 1) }) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                        }
                    }

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        PayrollStatCol("Net Payroll", DateUtils.formatCurrency(totalNet, cur))
                        PayrollStatCol("Disbursed", DateUtils.formatCurrency(totalPaid, cur), PresentGreen)
                        PayrollStatCol("Pending", DateUtils.formatCurrency(totalPending, cur), AbsentRed)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onGeneratePayroll(selectedMonth) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calculate_payroll_button")
                        ) {
                            Icon(Icons.Rounded.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (monthRecords.isEmpty()) "Generate" else "Recalculate")
                        }
                        OutlinedButton(
                            onClick = { showApproveAllConfirm = true },
                            enabled = monthRecords.isNotEmpty() && approvedCount < monthRecords.size,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve All ($approvedCount/${monthRecords.size})")
                        }
                    }
                }
            }
        }

        if (monthRecords.isEmpty()) {
            item {
                SaasCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No payroll generated for ${DateUtils.formatMonthTitle(selectedMonth)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap Generate to automatically compute salaries from attendance, overtime, and leave records.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(monthRecords, key = { it.id }) { rec ->
                val emp = empMap[rec.employeeId] ?: return@items
                SaasCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EmployeeAvatar(fullName = emp.fullName, photoUri = emp.photoUri, size = 42.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(emp.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${emp.employeeCode} • ${rec.salaryType.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            PaymentStatusBadge(rec.paymentStatus)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Present: ${rec.presentDays}d  |  Late: ${rec.lateDays}d  |  Half: ${rec.halfDays}d  |  Absent: ${rec.absentDays}d  |  Unpaid LV: ${rec.unpaidLeaveDays}d  |  OT: ${rec.overtimeHours}h",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PayrollStatCol("Gross", DateUtils.formatCurrency(rec.grossSalary, cur))
                            PayrollStatCol("Earnings", DateUtils.formatCurrency(rec.totalEarnings, cur), PresentGreen)
                            PayrollStatCol("Deductions", DateUtils.formatCurrency(rec.totalDeductions, cur), AbsentRed)
                            PayrollStatCol("Net Pay", DateUtils.formatCurrency(rec.netPayableSalary, cur), MaterialTheme.colorScheme.primary)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(
                                text = if (rec.isApproved) "Approved" else "Pending Review",
                                fgColor = if (rec.isApproved) PresentGreen else LateAmber,
                                bgColor = (if (rec.isApproved) PresentGreen else LateAmber).copy(alpha = 0.14f)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { onOpenPayslip(emp.id, rec.yearMonth) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Payslip")
                                }
                                Button(
                                    onClick = { editingPair = rec to emp },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Adjust / Pay")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingPair?.let { (rec, emp) ->
        PayrollAdjustmentDialog(
            record = rec,
            employee = emp,
            currency = cur,
            onSave = { bonuses, allowances, advance, otherDed, paidAmt, approved, notes ->
                onUpdatePayroll(rec, emp, bonuses, allowances, advance, otherDed, paidAmt, approved, notes)
                editingPair = null
            },
            onDismiss = { editingPair = null }
        )
    }

    slipPair?.let { (rec, emp) ->
        SalarySlipDialog(
            record = rec,
            employee = emp,
            currency = cur,
            onSharePdf = { table ->
                val pdfBytes = ReportExporter.buildPdfBytes(table, uiState.settings)
                ReportExporter.shareFile(context, "SalarySlip_${emp.employeeCode}_${rec.yearMonth}.pdf", "application/pdf", pdfBytes)
            },
            onPrint = { table ->
                ReportExporter.printReport(context, table, uiState.settings)
            },
            onDismiss = { slipPair = null }
        )
    }

    if (showApproveAllConfirm) {
        ConfirmActionDialog(
            title = "Approve All Payroll?",
            message = "Finalize and approve ${monthRecords.size} payroll slips for ${DateUtils.formatMonthTitle(selectedMonth)}?",
            confirmLabel = "Approve All",
            onConfirm = {
                onApproveAll(selectedMonth)
                showApproveAllConfirm = false
            },
            onDismiss = { showApproveAllConfirm = false }
        )
    }
}

@Composable
private fun PayrollStatCol(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun PayrollAdjustmentDialog(
    record: PayrollRecordEntity,
    employee: EmployeeEntity,
    currency: String,
    onSave: (Double, Double, Double, Double, Double, Boolean, String) -> Unit,
    onDismiss: () -> Unit
) {
    var bonuses by remember(record) { mutableStateOf(record.bonuses.toInt().toString()) }
    var allowances by remember(record) { mutableStateOf(record.allowances.toInt().toString()) }
    var advance by remember(record) { mutableStateOf(record.advanceSalary.toInt().toString()) }
    var otherDed by remember(record) { mutableStateOf(record.otherDeductions.toInt().toString()) }
    var paidAmt by remember(record) { mutableStateOf(record.paidAmount.toInt().toString()) }
    var isApproved by remember(record) { mutableStateOf(record.isApproved) }
    var notes by remember(record) { mutableStateOf(record.notes) }

    val previewEarnings = record.grossSalary + record.overtimePay +
        (bonuses.toDoubleOrNull() ?: 0.0) + (allowances.toDoubleOrNull() ?: 0.0)
    val previewDed = record.absenceDeduction + record.halfDayDeduction +
        record.unpaidLeaveDeduction + record.lateDeduction +
        (advance.toDoubleOrNull() ?: 0.0) + (otherDed.toDoubleOrNull() ?: 0.0)
    val previewNet = (previewEarnings - previewDed).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Adjust Payroll • ${employee.fullName}", fontWeight = FontWeight.Bold)
                Text("Net Preview: ${DateUtils.formatCurrency(previewNet, currency)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bonuses,
                        onValueChange = { bonuses = it },
                        label = { Text("Bonuses") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = allowances,
                        onValueChange = { allowances = it },
                        label = { Text("Allowances") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = advance,
                        onValueChange = { advance = it },
                        label = { Text("Advance Salary") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = otherDed,
                        onValueChange = { otherDed = it },
                        label = { Text("Other Deduct") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = paidAmt,
                    onValueChange = { paidAmt = it },
                    label = { Text("Paid Amount ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { paidAmt = previewNet.toInt().toString() }, modifier = Modifier.weight(1f)) {
                        Text("Mark Full Paid")
                    }
                    OutlinedButton(onClick = { paidAmt = "0" }, modifier = Modifier.weight(1f)) {
                        Text("Reset Unpaid")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isApproved, onCheckedChange = { isApproved = it })
                    Text("Approve payroll for disbursement", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Payroll Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    bonuses.toDoubleOrNull() ?: 0.0,
                    allowances.toDoubleOrNull() ?: 0.0,
                    advance.toDoubleOrNull() ?: 0.0,
                    otherDed.toDoubleOrNull() ?: 0.0,
                    paidAmt.toDoubleOrNull() ?: 0.0,
                    isApproved,
                    notes.trim()
                )
            }) {
                Text("Save & Recalculate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SalarySlipDialog(
    record: PayrollRecordEntity,
    employee: EmployeeEntity,
    currency: String,
    onSharePdf: (ReportTableData) -> Unit,
    onPrint: (ReportTableData) -> Unit,
    onDismiss: () -> Unit
) {
    val slipTable = remember(record, employee, currency) {
        ReportTableData(
            title = "Employee Salary Slip",
            subtitle = "${employee.fullName} (${employee.employeeCode}) • ${DateUtils.formatMonthTitle(record.yearMonth)}",
            headers = listOf("Component", "Details", "Amount"),
            rows = listOf(
                listOf("Department / Role", "${employee.department} / ${employee.designation}", record.salaryType.label),
                listOf("Attendance", "P:${record.presentDays} L:${record.lateDays} H:${record.halfDays} A:${record.absentDays}", "OT: ${record.overtimeHours}h"),
                listOf("Basic / Gross Salary", "Working days: ${record.workingDaysInMonth}", DateUtils.formatCurrency(record.grossSalary, currency)),
                listOf("Overtime Pay", "${record.overtimeHours} hrs @ ${DateUtils.formatCurrency(record.overtimeRatePerHour, currency)}/h", DateUtils.formatCurrency(record.overtimePay, currency)),
                listOf("Bonuses & Allowances", "Additional earnings", DateUtils.formatCurrency(record.bonuses + record.allowances, currency)),
                listOf("Total Earnings", "Gross + OT + Bonus + Allowance", DateUtils.formatCurrency(record.totalEarnings, currency)),
                listOf("Absence & Half-Day Ded.", "${record.absentDays} absent, ${record.halfDays} half", "- ${DateUtils.formatCurrency(record.absenceDeduction + record.halfDayDeduction, currency)}"),
                listOf("Unpaid Leave & Late Ded.", "${record.unpaidLeaveDays} unpaid lv, ${record.lateDays} late", "- ${DateUtils.formatCurrency(record.unpaidLeaveDeduction + record.lateDeduction, currency)}"),
                listOf("Advance & Other Ded.", "Recoveries", "- ${DateUtils.formatCurrency(record.advanceSalary + record.otherDeductions, currency)}"),
                listOf("Total Deductions", "All deductions combined", DateUtils.formatCurrency(record.totalDeductions, currency)),
                listOf("NET PAYABLE SALARY", "Paid: ${DateUtils.formatCurrency(record.paidAmount, currency)}", DateUtils.formatCurrency(record.netPayableSalary, currency))
            ),
            summaryLines = listOf(
                "Payment Status: ${record.paymentStatus.label}  |  Admin Approval: ${if (record.isApproved) "Approved" else "Pending"}"
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text("Salary Slip", fontWeight = FontWeight.Bold)
                    Text("${employee.fullName} • ${DateUtils.formatMonthTitle(record.yearMonth)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                slipTable.rows.forEach { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r[0], style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(r[1], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(r[2], style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = { onPrint(slipTable) }) {
                    Icon(Icons.Rounded.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print")
                }
                Button(onClick = { onSharePdf(slipTable) }) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
