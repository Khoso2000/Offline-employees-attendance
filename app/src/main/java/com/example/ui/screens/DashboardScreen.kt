package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.LeaveStatus
import com.example.domain.DateUtils
import com.example.ui.components.KpiCard
import com.example.ui.components.ModuleShortcutCard
import com.example.ui.components.MonthlyAttendanceBarChart
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.SaasCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.TodayDonutChart
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.HalfDayOrange
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.LeavePurple
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBluePrimary
import java.io.File

@Composable
fun DashboardScreen(
    uiState: StaffTrackUiState,
    onNavigateTo: (String) -> Unit,
    onQuickAddEmployee: () -> Unit,
    onSwitchAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val metrics = uiState.dashboardMetrics
    val currency = uiState.settings.currency
    val displayToday = DateUtils.formatDisplayDate(uiState.todayDate, uiState.settings.dateFormat)
    var showNotifications by remember { mutableStateOf(false) }

    val pendingLeaves = remember(uiState.leaves) {
        uiState.leaves.filter { it.status == LeaveStatus.PENDING }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Royal Blue Gradient HRM Top Header with Admin Panel Logo
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = RoyalBluePrimary,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(RoyalBluePrimary, RoyalBlueDark)
                            )
                        )
                        .padding(18.dp)
                ) {
                    // Top bar row: Logo + Name, Notifications, Admin Panel Logo Avatar
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
                            val logoPath = uiState.settings.companyLogoUri
                            if (!logoPath.isNullOrBlank()) {
                                AsyncImage(
                                    model = if (logoPath.startsWith("/")) File(logoPath) else logoPath,
                                    contentDescription = "StaffTrack company logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_admin_panel),
                                    contentDescription = "StaffTrack Admin Panel logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                )
                            }
                            Column {
                                Text(
                                    text = "StaffTrack • Admin Panel",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = uiState.settings.companyName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { showNotifications = true },
                                modifier = Modifier.testTag("notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (pendingLeaves.isNotEmpty()) {
                                            Badge(containerColor = AbsentRed, contentColor = Color.White) {
                                                Text(pendingLeaves.size.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White
                                    )
                                }
                            }

                            Surface(
                                onClick = onSwitchAccount,
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("switch_account_avatar")
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_admin_panel),
                                    contentDescription = "Admin panel profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Welcome message + Today Donut Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome back, Admin",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = displayToday,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable { onNavigateTo("attendance") }
                            ) {
                                Text(
                                    text = "${metrics.presentToday + metrics.lateToday + metrics.halfDayToday} of ${metrics.totalActiveEmployees} staff checked in",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        TodayDonutChart(
                            present = metrics.presentToday,
                            late = metrics.lateToday,
                            halfDay = metrics.halfDayToday,
                            leave = metrics.onLeaveToday,
                            absent = metrics.absentToday,
                            totalActive = metrics.totalActiveEmployees
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = onQuickAddEmployee,
                    label = { Text("Add Employee", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = RoyalBluePrimary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("quick_add_employee")
                )
                AssistChip(
                    onClick = { onNavigateTo("attendance") },
                    label = { Text("Mark Attendance", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.FactCheck,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = PresentGreen
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("quick_mark_attendance")
                )
                AssistChip(
                    onClick = { onNavigateTo("payroll") },
                    label = { Text("Run Payroll", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = HalfDayOrange
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("quick_run_payroll")
                )
                AssistChip(
                    onClick = { onNavigateTo("reports") },
                    label = { Text("Export Reports", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Assessment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = InfoCyan
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("quick_export_reports")
                )
            }
        }

        // 6 Modular Navigation Cards
        item {
            SectionHeader(
                title = "HR Modules",
                subtitle = "Navigate directly to workforce, payroll & reports"
            )
            Spacer(modifier = Modifier.height(10.dp))
            BoxWithConstraints {
                val cols = if (maxWidth >= 700.dp) 3 else 2
                val modules = listOf(
                    ModuleSpec(
                        title = "Employees",
                        subtitle = "Directory & profiles",
                        count = "${metrics.totalActiveEmployees} staff",
                        icon = Icons.Rounded.Groups,
                        color = RoyalBluePrimary,
                        route = "employees"
                    ),
                    ModuleSpec(
                        title = "Attendance",
                        subtitle = "Daily & calendar",
                        count = "${metrics.presentToday} in",
                        icon = Icons.Rounded.FactCheck,
                        color = PresentGreen,
                        route = "attendance"
                    ),
                    ModuleSpec(
                        title = "Payroll",
                        subtitle = "Salaries & slips",
                        count = DateUtils.formatCurrency(metrics.currentMonthPayrollTotal, currency),
                        icon = Icons.Rounded.Payments,
                        color = HalfDayOrange,
                        route = "payroll"
                    ),
                    ModuleSpec(
                        title = "Leaves",
                        subtitle = "Requests & balances",
                        count = "${metrics.pendingLeaveCount} pending",
                        icon = Icons.Rounded.BeachAccess,
                        color = LeavePurple,
                        route = "leaves"
                    ),
                    ModuleSpec(
                        title = "Reports",
                        subtitle = "PDF, CSV & print",
                        count = "9 types",
                        icon = Icons.Rounded.Assessment,
                        color = InfoCyan,
                        route = "reports"
                    ),
                    ModuleSpec(
                        title = "Settings",
                        subtitle = "Rules, shifts & theme",
                        count = "Config",
                        icon = Icons.Rounded.Settings,
                        color = RoyalBlueDark,
                        route = "settings"
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    modules.chunked(cols).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { mod ->
                                ModuleShortcutCard(
                                    title = mod.title,
                                    subtitle = mod.subtitle,
                                    countText = mod.count,
                                    icon = mod.icon,
                                    accentColor = mod.color,
                                    onClick = { onNavigateTo(mod.route) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("module_card_${mod.route}")
                                )
                            }
                            repeat(cols - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Employee & Attendance Summary KPI Cards (all 8 core metrics)
        item {
            SectionHeader(
                title = "Today's Snapshot",
                subtitle = "Live statistics from local offline storage"
            )
            Spacer(modifier = Modifier.height(10.dp))
            BoxWithConstraints {
                val columns = if (maxWidth >= 720.dp) 4 else 2
                val itemsList = listOf(
                    KpiSpec("Total Employees", metrics.totalActiveEmployees.toString(), "Active workforce", Icons.Rounded.Groups, RoyalBluePrimary) { onNavigateTo("employees") },
                    KpiSpec("Present Today", metrics.presentToday.toString(), "Checked in on time", Icons.Rounded.CheckCircle, PresentGreen) { onNavigateTo("attendance") },
                    KpiSpec("Absent Today", metrics.absentToday.toString(), "Unscheduled absent", Icons.Rounded.Cancel, AbsentRed) { onNavigateTo("attendance") },
                    KpiSpec("On Leave", metrics.onLeaveToday.toString(), "${metrics.pendingLeaveCount} pending req.", Icons.Rounded.BeachAccess, LeavePurple) { onNavigateTo("leaves") },
                    KpiSpec("Late Arrivals", metrics.lateToday.toString(), "Past ${uiState.settings.lateGraceMinutes}m grace", Icons.Rounded.Schedule, HalfDayOrange) { onNavigateTo("attendance") },
                    KpiSpec("Half-Day Staff", metrics.halfDayToday.toString(), "< ${uiState.settings.halfDayHoursThreshold}h threshold", Icons.Rounded.HourglassTop, InfoCyan) { onNavigateTo("attendance") },
                    KpiSpec("Monthly Payroll", DateUtils.formatCurrency(metrics.currentMonthPayrollTotal, currency), DateUtils.formatMonthTitle(uiState.currentMonth), Icons.Rounded.AccountBalanceWallet, RoyalBluePrimary) { onNavigateTo("payroll") },
                    KpiSpec("Pending Salary", DateUtils.formatCurrency(metrics.pendingSalaryTotal, currency), "Unpaid & partial", Icons.Rounded.PendingActions, AbsentRed) { onNavigateTo("payroll") }
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsList.chunked(columns).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { spec ->
                                KpiCard(
                                    title = spec.title,
                                    value = spec.value,
                                    subtitle = spec.subtitle,
                                    icon = spec.icon,
                                    accentColor = spec.color,
                                    onClick = spec.onClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Attendance Bar Chart
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = "Monthly Attendance Overview",
                        subtitle = "${DateUtils.formatMonthTitle(uiState.currentMonth)} • Last ${uiState.monthlyChartPoints.size} days"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    MonthlyAttendanceBarChart(points = uiState.monthlyChartPoints)
                }
            }
        }

        // Recent Activity
        item {
            SectionHeader(
                title = "Recent Activity",
                subtitle = "Latest offline workspace updates"
            )
        }

        if (uiState.activityLogs.isEmpty()) {
            item {
                SaasCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No recent activity logged yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(uiState.activityLogs.take(8), key = { it.id }) { log ->
                SaasCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = log.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = DateUtils.formatTimestamp(log.timestamp).substringAfter(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showNotifications) {
        NotificationsDialog(
            pendingLeaves = pendingLeaves,
            employees = uiState.employees,
            recentLogs = uiState.activityLogs,
            onReviewLeaves = {
                showNotifications = false
                onNavigateTo("leaves")
            },
            onDismiss = { showNotifications = false }
        )
    }
}

private data class ModuleSpec(
    val title: String,
    val subtitle: String,
    val count: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

private data class KpiSpec(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)
