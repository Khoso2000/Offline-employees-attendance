package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.EmployeeEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.LeaveCategory
import com.example.domain.DateUtils
import com.example.domain.PayrollCalculator
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.KpiCard
import com.example.ui.components.LeaveStatusBadge
import com.example.ui.components.MonthlyAttendanceBarChart
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.SaasCard
import com.example.ui.components.SectionHeader
import com.example.ui.state.DailyChartPoint
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.LeavePurple
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBluePrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(
    employee: EmployeeEntity,
    uiState: StaffTrackUiState,
    onCheckInOut: (EmployeeEntity, String, AttendanceStatus, String?, String?, Double?, String) -> Unit,
    onSubmitLeave: (EmployeeEntity, LeaveCategory, String, String, String) -> Unit,
    onOpenPayslip: (Long, String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showApplyLeave by remember { mutableStateOf(false) }
    var hazriConfirmMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(hazriConfirmMsg) {
        if (hazriConfirmMsg != null) {
            delay(3500)
            hazriConfirmMsg = null
        }
    }

    val todayRecord = remember(uiState.attendance, employee.id, uiState.todayDate) {
        uiState.attendance.firstOrNull { it.employeeId == employee.id && it.date == uiState.todayDate }
    }
    val monthAtt = remember(uiState.attendance, employee.id, uiState.currentMonth) {
        uiState.attendance.filter { it.employeeId == employee.id && it.date.startsWith(uiState.currentMonth) }
    }
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
    val myLeaves = remember(uiState.leaves, employee.id) {
        uiState.leaves.filter { it.employeeId == employee.id }.take(5)
    }
    val latestPayroll = remember(uiState.payrolls, employee.id, uiState.currentMonth) {
        uiState.payrolls.firstOrNull { it.employeeId == employee.id && it.yearMonth == uiState.currentMonth }
            ?: uiState.payrolls.firstOrNull { it.employeeId == employee.id }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Royal Blue Welcome Header
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            EmployeeAvatar(fullName = employee.fullName, photoUri = employee.photoUri, size = 54.dp)
                            Column {
                                Text(
                                    text = "Welcome, ${employee.fullName.substringBefore(" ")}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${employee.employeeCode} • ${employee.designation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.86f)
                                )
                            }
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Switch account", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = DateUtils.formatDisplayDate(uiState.todayDate, uiState.settings.dateFormat),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Today Check-In / Check-Out (Hazri) Action Card
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(14.dp),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .size(52.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_staff_hazri),
                                    contentDescription = "Staff Hazri attendance logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            }
                            Column {
                                Text("Today's Shift (Hazri)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Staff attendance check-in & out",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (todayRecord != null) {
                            AttendanceBadge(todayRecord.status, uiState.settings.themeMode)
                        }
                    }

                    AnimatedVisibility(
                        visible = hazriConfirmMsg != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            color = PresentGreen.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_staff_hazri),
                                    contentDescription = "Hazri marked confirmation",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                )
                                Text(
                                    text = hazriConfirmMsg.orEmpty(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PresentGreen
                                )
                            }
                        }
                    }

                    val worked = DateUtils.calculateWorkingHours(todayRecord?.checkInTime, todayRecord?.checkOutTime)
                    Text(
                        text = if (todayRecord == null) {
                            "Not checked in yet. Office start: ${uiState.settings.officeStartTime}"
                        } else {
                            "Check-In: ${todayRecord.checkInTime ?: "-"}  •  Check-Out: ${todayRecord.checkOutTime ?: "-"}  •  Hours: ${String.format("%.1f", worked)}h"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        val nowHHmm = SimpleDateFormat("HH:mm", Locale.US).format(Date())
                        Button(
                            onClick = {
                                onCheckInOut(
                                    employee,
                                    uiState.todayDate,
                                    AttendanceStatus.PRESENT,
                                    todayRecord?.checkInTime ?: nowHHmm,
                                    todayRecord?.checkOutTime,
                                    todayRecord?.overtimeHours,
                                    "Self Check-In"
                                )
                                hazriConfirmMsg = "Hazri Recorded! Checked in at $nowHHmm"
                            },
                            enabled = todayRecord?.checkInTime == null,
                            colors = ButtonDefaults.buttonColors(containerColor = PresentGreen, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check In")
                        }
                        OutlinedButton(
                            onClick = {
                                onCheckInOut(
                                    employee,
                                    uiState.todayDate,
                                    todayRecord?.status ?: AttendanceStatus.PRESENT,
                                    todayRecord?.checkInTime ?: uiState.settings.officeStartTime,
                                    nowHHmm,
                                    null,
                                    "Self Check-Out"
                                )
                                hazriConfirmMsg = "Shift Complete! Checked out at $nowHHmm"
                            },
                            enabled = todayRecord?.checkInTime != null && todayRecord.checkOutTime == null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check Out")
                        }
                    }
                }
            }
        }

        // Personal Monthly Stats
        item {
            val present = monthAtt.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }
            val absent = monthAtt.count { it.status == AttendanceStatus.ABSENT }
            val ot = monthAtt.sumOf { it.overtimeHours }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiCard("Present", present.toString(), "This month", Icons.Rounded.CheckCircle, PresentGreen, Modifier.weight(1f))
                KpiCard("Absent", absent.toString(), "This month", Icons.Rounded.Cancel, AbsentRed, Modifier.weight(1f))
                KpiCard("Overtime", "${String.format("%.1f", ot)}h", "Logged", Icons.Rounded.Schedule, InfoCyan, Modifier.weight(1f))
            }
        }

        // Monthly Graph
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "My Attendance Graph", subtitle = DateUtils.formatMonthTitle(uiState.currentMonth))
                    Spacer(modifier = Modifier.height(10.dp))
                    MonthlyAttendanceBarChart(points = chartPoints)
                }
            }
        }

        // Latest Payslip Card
        if (latestPayroll != null) {
            item {
                SaasCard(
                    onClick = { onOpenPayslip(employee.id, latestPayroll.yearMonth) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = RoyalBluePrimary, modifier = Modifier.size(30.dp))
                            Column {
                                Text("Latest Payslip (${DateUtils.formatMonthTitle(latestPayroll.yearMonth)})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "Net Payable: ${DateUtils.formatCurrency(latestPayroll.netPayableSalary, uiState.settings.currency)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RoyalBluePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        PaymentStatusBadge(latestPayroll.paymentStatus)
                    }
                }
            }
        }

        // Leave Balance & Request Button
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "My Leave Balances",
                        trailing = {
                            Button(onClick = { showApplyLeave = true }) {
                                Icon(Icons.Rounded.BeachAccess, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Request")
                            }
                        }
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        leaveBalances.forEach { bal ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bal.category.label.replace(" Leave", ""), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("${bal.remaining}/${bal.allowance}", style = MaterialTheme.typography.bodySmall, color = LeavePurple)
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

        if (myLeaves.isNotEmpty()) {
            item { SectionHeader(title = "My Recent Leave Requests") }
            items(myLeaves, key = { it.id }) { lv ->
                SaasCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${lv.category.label} (${lv.totalDays}d)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${lv.startDate} → ${lv.endDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LeaveStatusBadge(lv.status)
                    }
                }
            }
        }
    }

    if (showApplyLeave) {
        var category by remember { mutableStateOf(LeaveCategory.CASUAL) }
        var catOpen by remember { mutableStateOf(false) }
        var startDate by remember { mutableStateOf(uiState.todayDate) }
        var endDate by remember { mutableStateOf(uiState.todayDate) }
        var reason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showApplyLeave = false },
            title = { Text("Request Leave", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(expanded = catOpen, onExpandedChange = { catOpen = it }) {
                        OutlinedTextField(
                            value = category.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catOpen) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(expanded = catOpen, onDismissRequest = { catOpen = false }) {
                            LeaveCategory.entries.forEach { c ->
                                DropdownMenuItem(text = { Text(c.label) }, onClick = { category = c; catOpen = false })
                            }
                        }
                    }
                    OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start (yyyy-MM-dd)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End (yyyy-MM-dd)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSubmitLeave(employee, category, startDate.trim(), endDate.trim(), reason.trim())
                    showApplyLeave = false
                }) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showApplyLeave = false }) { Text("Cancel") }
            }
        )
    }
}
