package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.DateUtils
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.SaasCard
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBluePrimary
import com.example.util.ReportExporter
import com.example.util.ReportTableData

@Composable
fun PayslipScreen(
    employeeId: Long,
    yearMonth: String,
    uiState: StaffTrackUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val employee = uiState.employees.firstOrNull { it.id == employeeId }
    val record = uiState.payrolls.firstOrNull { it.employeeId == employeeId && it.yearMonth == yearMonth }

    if (employee == null || record == null) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Payslip not generated for $yearMonth.", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Back") }
        }
        return
    }

    val cur = uiState.settings.currency
    val slipTable = remember(record, employee, cur) {
        ReportTableData(
            title = "Official Employee Payslip",
            subtitle = "${employee.fullName} (${employee.employeeCode}) • ${DateUtils.formatMonthTitle(record.yearMonth)}",
            headers = listOf("Line Item", "Details", "Amount"),
            rows = listOf(
                listOf("Department / Role", "${employee.department} / ${employee.designation}", record.salaryType.label),
                listOf("Attendance Summary", "Present: ${record.presentDays}d  Late: ${record.lateDays}d  Half: ${record.halfDays}d  Absent: ${record.absentDays}d", "Unpaid Leave: ${record.unpaidLeaveDays}d"),
                listOf("Basic / Gross Salary", "Working days: ${record.workingDaysInMonth}", DateUtils.formatCurrency(record.grossSalary, cur)),
                listOf("Overtime Pay", "${record.overtimeHours} hrs @ ${DateUtils.formatCurrency(record.overtimeRatePerHour, cur)}/h", DateUtils.formatCurrency(record.overtimePay, cur)),
                listOf("Bonuses", "Performance / incentive", DateUtils.formatCurrency(record.bonuses, cur)),
                listOf("Allowances", "Transport / meal / housing", DateUtils.formatCurrency(record.allowances, cur)),
                listOf("Total Earnings", "Sum of earnings", DateUtils.formatCurrency(record.totalEarnings, cur)),
                listOf("Absence & Half-Day Deduction", "${record.absentDays} absent, ${record.halfDays} half", "- ${DateUtils.formatCurrency(record.absenceDeduction + record.halfDayDeduction, cur)}"),
                listOf("Unpaid Leave & Late Penalty", "${record.unpaidLeaveDays} unpaid lv, ${record.lateDays} late", "- ${DateUtils.formatCurrency(record.unpaidLeaveDeduction + record.lateDeduction, cur)}"),
                listOf("Advance & Other Deductions", "Recoveries", "- ${DateUtils.formatCurrency(record.advanceSalary + record.otherDeductions, cur)}"),
                listOf("Total Deductions", "All deductions combined", DateUtils.formatCurrency(record.totalDeductions, cur)),
                listOf("NET PAYABLE SALARY", "Paid: ${DateUtils.formatCurrency(record.paidAmount, cur)}", DateUtils.formatCurrency(record.netPayableSalary, cur))
            ),
            summaryLines = listOf(
                "Payment Status: ${record.paymentStatus.label}  |  Approval: ${if (record.isApproved) "Approved by Admin" else "Pending Review"}"
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = RoyalBluePrimary,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(RoyalBluePrimary, RoyalBlueDark)))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = DateUtils.formatMonthTitle(record.yearMonth),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        PaymentStatusBadge(record.paymentStatus)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EmployeeAvatar(fullName = employee.fullName, photoUri = employee.photoUri, size = 56.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(employee.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${employee.employeeCode} • ${employee.designation}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Net Payable Salary", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                    Text(
                        text = DateUtils.formatCurrency(record.netPayableSalary, cur),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Earnings Card
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Earnings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PresentGreen)
                    SlipLineRow("Basic / Gross Salary", DateUtils.formatCurrency(record.grossSalary, cur))
                    SlipLineRow("Overtime (${record.overtimeHours} hrs)", DateUtils.formatCurrency(record.overtimePay, cur))
                    SlipLineRow("Bonuses", DateUtils.formatCurrency(record.bonuses, cur))
                    SlipLineRow("Allowances", DateUtils.formatCurrency(record.allowances, cur))
                    HorizontalDivider()
                    SlipLineRow("Total Earnings", DateUtils.formatCurrency(record.totalEarnings, cur), bold = true, valueColor = PresentGreen)
                }
            }
        }

        // Deductions Card
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Deductions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AbsentRed)
                    SlipLineRow("Absence Deduction (${record.absentDays}d)", DateUtils.formatCurrency(record.absenceDeduction, cur))
                    SlipLineRow("Half-Day Deduction (${record.halfDays}d)", DateUtils.formatCurrency(record.halfDayDeduction, cur))
                    SlipLineRow("Unpaid Leave (${record.unpaidLeaveDays}d)", DateUtils.formatCurrency(record.unpaidLeaveDeduction, cur))
                    SlipLineRow("Late Penalty (${record.lateDays}d)", DateUtils.formatCurrency(record.lateDeduction, cur))
                    SlipLineRow("Advance & Other", DateUtils.formatCurrency(record.advanceSalary + record.otherDeductions, cur))
                    HorizontalDivider()
                    SlipLineRow("Total Deductions", DateUtils.formatCurrency(record.totalDeductions, cur), bold = true, valueColor = AbsentRed)
                }
            }
        }

        // Export & Print Buttons
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { ReportExporter.printReport(context, slipTable, uiState.settings) },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Rounded.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print Payslip")
                }
                Button(
                    onClick = {
                        val bytes = ReportExporter.buildPdfBytes(slipTable, uiState.settings)
                        ReportExporter.shareFile(context, "Payslip_${employee.employeeCode}_${record.yearMonth}.pdf", "application/pdf", bytes)
                    },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share PDF")
                }
            }
        }
    }
}

@Composable
private fun SlipLineRow(
    label: String,
    value: String,
    bold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}
