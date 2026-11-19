package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.EmployeeEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.EmploymentStatus
import com.example.data.model.SalaryType
import com.example.domain.DateUtils
import com.example.domain.PayrollCalculator
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.KpiCard
import com.example.ui.components.MonthlyAttendanceBarChart
import com.example.ui.components.SaasCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.state.DailyChartPoint
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.HalfDayOrange
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.LeavePurple
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBluePrimary

@Composable
fun EmployeeDetailsScreen(
    employeeId: Long,
    uiState: StaffTrackUiState,
    onBack: () -> Unit,
    onEditEmployee: (EmployeeEntity) -> Unit,
    onToggleStatus: (EmployeeEntity) -> Unit,
    onDeleteEmployee: (EmployeeEntity) -> Unit,
    onOpenPayslip: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val employee = uiState.employees.firstOrNull { it.id == employeeId }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (employee == null) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Employee not found.", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Go Back") }
        }
        return
    }

    val monthAtt = remember(uiState.attendance, employeeId, uiState.currentMonth) {
        uiState.attendance.filter { it.employeeId == employeeId && it.date.startsWith(uiState.currentMonth) }
            .sortedByDescending { it.date }
    }
    val presentDays = monthAtt.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }
    val absentDays = monthAtt.count { it.status == AttendanceStatus.ABSENT }
    val halfDays = monthAtt.count { it.status == AttendanceStatus.HALF_DAY }
    val leaveDays = monthAtt.count { it.status == AttendanceStatus.ON_LEAVE }
    val overtimeHours = monthAtt.sumOf { it.overtimeHours }
    val totalLoggedHours = monthAtt.sumOf { DateUtils.calculateWorkingHours(it.checkInTime, it.checkOutTime) }

    val chartPoints = remember(monthAtt) {
        monthAtt.sortedBy { it.date }.takeLast(14).map { att ->
            DailyChartPoint(
                dayLabel = att.date.takeLast(2),
                present = if (att.status == AttendanceStatus.PRESENT) 1 else 0,
                late = if (att.status == AttendanceStatus.LATE) 1 else 0,
                halfDay = if (att.status == AttendanceStatus.HALF_DAY) 1 else 0,
                leave = if (att.status == AttendanceStatus.ON_LEAVE) 1 else 0,
                absent = if (att.status == AttendanceStatus.ABSENT) 1 else 0
            )
        }
    }

    val leaveBalances = remember(employee, uiState.leaves) {
        PayrollCalculator.calculateLeaveBalances(employee, uiState.leaves)
    }
    val latestPayroll = remember(uiState.payrolls, employeeId, uiState.currentMonth) {
        uiState.payrolls.firstOrNull { it.employeeId == employeeId && it.yearMonth == uiState.currentMonth }
            ?: uiState.payrolls.firstOrNull { it.employeeId == employeeId }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Gradient Profile Hero
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
                        Row {
                            IconButton(onClick = { onEditEmployee(employee) }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit employee", tint = Color.White)
                            }
                            IconButton(onClick = { onToggleStatus(employee) }) {
                                Icon(Icons.Rounded.PowerSettingsNew, contentDescription = "Toggle status", tint = Color.White)
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete employee", tint = Color(0xFFFFB4B8))
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        EmployeeAvatar(fullName = employee.fullName, photoUri = employee.photoUri, size = 72.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = employee.fullName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${employee.designation} • ${employee.department}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusBadge(
                                    text = employee.employeeCode,
                                    fgColor = RoyalBlueDark,
                                    bgColor = Color.White
                                )
                                val isActive = employee.status == EmploymentStatus.ACTIVE
                                StatusBadge(
                                    text = employee.status.label,
                                    fgColor = Color.White,
                                    bgColor = if (isActive) PresentGreen else AbsentRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Working Schedule", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.78f))
                            Text("${employee.workingHoursPerDay} hrs/day • Off: ${employee.weeklyHoliday}", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Month Logged", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.78f))
                            Text("${String.format("%.1f", totalLoggedHours)} hrs", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 5 Attendance Breakdown KPIs + Salary
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard("Present Days", presentDays.toString(), "Incl. late", Icons.Rounded.CheckCircle, PresentGreen, Modifier.weight(1f))
                    KpiCard("Absent Days", absentDays.toString(), "This month", Icons.Rounded.Cancel, AbsentRed, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard("Half Days", halfDays.toString(), "Partial shift", Icons.Rounded.HourglassBottom, HalfDayOrange, Modifier.weight(1f))
                    KpiCard("Leave Days", leaveDays.toString(), "Approved", Icons.Rounded.BeachAccess, LeavePurple, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard("Overtime", "${String.format("%.1f", overtimeHours)} h", "Logged OT", Icons.Rounded.Schedule, InfoCyan, Modifier.weight(1f))
                    KpiCard(
                        "Basic Salary",
                        DateUtils.formatCurrency(employee.basicSalary, uiState.settings.currency),
                        if (employee.salaryType == SalaryType.MONTHLY) "Monthly Fixed" else "Daily Wage",
                        Icons.Rounded.Payments,
                        RoyalBluePrimary,
                        Modifier.weight(1f)
                    )
                }
            }
        }

        // Monthly Attendance Graph
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = "Monthly Attendance Graph",
                        subtitle = DateUtils.formatMonthTitle(uiState.currentMonth)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MonthlyAttendanceBarChart(points = chartPoints)
                }
            }
        }

        // Contact & Leave Balances
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Contact & Employment Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalBluePrimary)
                        Text(employee.phone, style = MaterialTheme.typography.bodySmall)
                    }
                    if (employee.email.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalBluePrimary)
                            Text(employee.email, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (employee.address.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalBluePrimary)
                            Text(employee.address, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(
                        "Joined: ${DateUtils.formatDisplayDate(employee.joiningDate, uiState.settings.dateFormat)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()
                    Text("Annual Leave Balances", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        leaveBalances.forEach { bal ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bal.category.label.replace(" Leave", ""), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("${bal.remaining}/${bal.allowance} left", style = MaterialTheme.typography.bodySmall, color = RoyalBluePrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (bal.used.toFloat() / bal.allowance.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (latestPayroll != null) {
                        HorizontalDivider()
                        OutlinedButton(
                            onClick = { onOpenPayslip(employee.id, latestPayroll.yearMonth) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Payslip (${DateUtils.formatMonthTitle(latestPayroll.yearMonth)})")
                        }
                    }
                }
            }
        }

        // Recent Attendance History
        item {
            SectionHeader(title = "Recent Attendance Log", subtitle = "Latest entries for ${employee.fullName}")
        }
        items(monthAtt.take(10), key = { it.id }) { att ->
            val hrs = DateUtils.calculateWorkingHours(att.checkInTime, att.checkOutTime)
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            DateUtils.formatDisplayDate(att.date, uiState.settings.dateFormat),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "In: ${att.checkInTime ?: "-"}  •  Out: ${att.checkOutTime ?: "-"}  •  Work: ${String.format("%.1f", hrs)}h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AttendanceBadge(att.status, uiState.settings.themeMode)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmActionDialog(
            title = "Delete Employee?",
            message = "Remove ${employee.fullName} (${employee.employeeCode}) and all associated records?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteEmployee(employee)
                showDeleteConfirm = false
                onBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
