package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.EmploymentStatus
import com.example.domain.DateUtils
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.EmployeeAvatar
import com.example.ui.components.SaasCard
import com.example.ui.components.attendanceStatusColors
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.LateAmber
import com.example.ui.theme.LeaveBlue
import com.example.ui.theme.PresentGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    uiState: StaffTrackUiState,
    onMarkAttendance: (EmployeeEntity, String, AttendanceStatus, String?, String?, Double?, String) -> Unit,
    onBulkMarkAttendance: (List<EmployeeEntity>, String, AttendanceStatus, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDate by remember { mutableStateOf(uiState.todayDate) }
    var selectedMonth by remember { mutableStateOf(uiState.currentMonth) }
    var searchQuery by remember { mutableStateOf("") }
    var showBulkDialog by remember { mutableStateOf(false) }
    var editingPair by remember { mutableStateOf<Pair<EmployeeEntity, AttendanceEntity?>?>(null) }
    var hazriBanner by remember { mutableStateOf<Pair<String, AttendanceStatus>?>(null) }

    LaunchedEffect(hazriBanner) {
        if (hazriBanner != null) {
            delay(3500)
            hazriBanner = null
        }
    }

    fun markWithHazriBanner(
        emp: EmployeeEntity,
        date: String,
        status: AttendanceStatus,
        checkIn: String?,
        checkOut: String?,
        otHours: Double?,
        notes: String
    ) {
        onMarkAttendance(emp, date, status, checkIn, checkOut, otHours, notes)
        hazriBanner = emp.fullName to status
    }

    val activeEmployees = remember(uiState.employees, searchQuery) {
        uiState.employees.filter { emp ->
            emp.status == EmploymentStatus.ACTIVE && (
                searchQuery.isBlank() ||
                    emp.fullName.contains(searchQuery, ignoreCase = true) ||
                    emp.employeeCode.contains(searchQuery, ignoreCase = true) ||
                    emp.department.contains(searchQuery, ignoreCase = true)
                )
        }
    }
    val empMap = remember(uiState.employees) { uiState.employees.associateBy { it.id } }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Daily Register") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Calendar") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("History") })
        }

        when (selectedTab) {
            0 -> {
                val dateAttendanceMap = remember(uiState.attendance, selectedDate) {
                    uiState.attendance.filter { it.date == selectedDate }.associateBy { it.employeeId }
                }
                var quickEmpId by remember(activeEmployees) { mutableStateOf(activeEmployees.firstOrNull()?.id) }
                var quickEmpDropdown by remember { mutableStateOf(false) }
                var confirmOverwrite by remember { mutableStateOf<Pair<EmployeeEntity, AttendanceStatus>?>(null) }
                val quickEmp = activeEmployees.firstOrNull { it.id == quickEmpId } ?: activeEmployees.firstOrNull()

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Staff Attendance (Hazri) Visual Banner
                    item {
                        SaasCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(14.dp),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .size(56.dp)
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Staff Attendance (Hazri)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${dateAttendanceMap.size} of ${activeEmployees.size} staff marked for ${DateUtils.formatDisplayDate(selectedDate, uiState.settings.dateFormat)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Celebratory Hazri Confirmation Banner when marking attendance
                    item {
                        AnimatedVisibility(
                            visible = hazriBanner != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            hazriBanner?.let { (staffName, markedStatus) ->
                                val (fg, bg) = attendanceStatusColors(markedStatus, uiState.settings.themeMode)
                                Surface(
                                    color = bg,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.logo_staff_hazri),
                                            contentDescription = "Hazri marked staff illustration",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Hazri Marked!",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = fg
                                            )
                                            Text(
                                                text = "$staffName • ${markedStatus.label}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        AttendanceBadge(markedStatus, uiState.settings.themeMode)
                                    }
                                }
                            }
                        }
                    }

                    // Date Switcher & Bulk Button
                    item {
                        SaasCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { selectedDate = DateUtils.shiftDate(selectedDate, -1) }) {
                                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous day")
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = DateUtils.formatDisplayDate(selectedDate, uiState.settings.dateFormat),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = DateUtils.dayOfWeekName(selectedDate).lowercase().replaceFirstChar { it.uppercase() } +
                                                if (selectedDate == uiState.todayDate) " (Today)" else "",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { selectedDate = DateUtils.shiftDate(selectedDate, 1) }) {
                                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next day")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedDate = uiState.todayDate },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Jump to Today")
                                    }
                                    Button(
                                        onClick = { showBulkDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("bulk_attendance_button")
                                    ) {
                                        Icon(Icons.Rounded.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Bulk Mark")
                                    }
                                }
                            }
                        }
                    }

                    // 6 Large Colored Attendance Buttons Console
                    if (quickEmp != null) {
                        item {
                            val quickRec = dateAttendanceMap[quickEmp.id]
                            SaasCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Quick Attendance Pad",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    ExposedDropdownMenuBox(
                                        expanded = quickEmpDropdown,
                                        onExpandedChange = { quickEmpDropdown = it }
                                    ) {
                                        OutlinedTextField(
                                            value = "${quickEmp.fullName} (${quickEmp.employeeCode})",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Selected Employee") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quickEmpDropdown) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = quickEmpDropdown,
                                            onDismissRequest = { quickEmpDropdown = false }
                                        ) {
                                            activeEmployees.forEach { e ->
                                                DropdownMenuItem(
                                                    text = { Text("${e.fullName} (${e.employeeCode})") },
                                                    onClick = {
                                                        quickEmpId = e.id
                                                        quickEmpDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    listOf(
                                        listOf(AttendanceStatus.PRESENT, AttendanceStatus.ABSENT, AttendanceStatus.HALF_DAY),
                                        listOf(AttendanceStatus.LATE, AttendanceStatus.HOLIDAY, AttendanceStatus.ON_LEAVE)
                                    ).forEach { rowStatuses ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowStatuses.forEach { st ->
                                                val (fg, bg) = attendanceStatusColors(st, uiState.settings.themeMode)
                                                val isActive = quickRec?.status == st
                                                Surface(
                                                    onClick = {
                                                        if (quickRec != null && quickRec.status != st) {
                                                            confirmOverwrite = quickEmp to st
                                                        } else {
                                                            val defIn = if (st == AttendanceStatus.PRESENT || st == AttendanceStatus.LATE || st == AttendanceStatus.HALF_DAY) uiState.settings.officeStartTime else null
                                                            val defOut = when (st) {
                                                                AttendanceStatus.PRESENT, AttendanceStatus.LATE -> uiState.settings.officeEndTime
                                                                AttendanceStatus.HALF_DAY -> "13:00"
                                                                else -> null
                                                            }
                                                            markWithHazriBanner(quickEmp, selectedDate, st, defIn, defOut, quickRec?.overtimeHours, quickRec?.notes ?: "")
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = if (isActive) fg else bg,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = st.label,
                                                            color = if (isActive) Color.White else fg,
                                                            style = MaterialTheme.typography.labelLarge,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            confirmOverwrite?.let { (empToUpdate, newStatus) ->
                                com.example.ui.components.ConfirmActionDialog(
                                    title = "Update Attendance Status?",
                                    message = "Change ${empToUpdate.fullName}'s attendance on $selectedDate to ${newStatus.label}?",
                                    confirmLabel = "Update",
                                    onConfirm = {
                                        val existing = dateAttendanceMap[empToUpdate.id]
                                        val defIn = if (newStatus == AttendanceStatus.PRESENT || newStatus == AttendanceStatus.LATE || newStatus == AttendanceStatus.HALF_DAY) {
                                            existing?.checkInTime ?: uiState.settings.officeStartTime
                                        } else null
                                        val defOut = when (newStatus) {
                                            AttendanceStatus.PRESENT, AttendanceStatus.LATE -> existing?.checkOutTime ?: uiState.settings.officeEndTime
                                            AttendanceStatus.HALF_DAY -> existing?.checkOutTime ?: "13:00"
                                            else -> null
                                        }
                                        markWithHazriBanner(empToUpdate, selectedDate, newStatus, defIn, defOut, existing?.overtimeHours, existing?.notes ?: "")
                                        confirmOverwrite = null
                                    },
                                    onDismiss = { confirmOverwrite = null }
                                )
                            }
                        }
                    }

                    // Search bar
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Filter staff by name, ID or dept...") },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Summary counts for selectedDate
                    item {
                        val records = dateAttendanceMap.values
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AttendanceStatus.entries) { st ->
                                val count = records.count { it.status == st }
                                val (fg, bg) = attendanceStatusColors(st, uiState.settings.themeMode)
                                Surface(color = bg, shape = RoundedCornerShape(10.dp)) {
                                    Text(
                                        text = "${st.label}: $count",
                                        color = fg,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(activeEmployees, key = { it.id }) { emp ->
                        val record = dateAttendanceMap[emp.id]
                        SaasCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    EmployeeAvatar(fullName = emp.fullName, photoUri = emp.photoUri, size = 40.dp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = emp.fullName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${emp.employeeCode} • ${emp.department}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (record != null) {
                                        AttendanceBadge(record.status, uiState.settings.themeMode)
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "Unmarked",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { editingPair = emp to record }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit check-in/out & overtime")
                                    }
                                }

                                if (record != null && (!record.checkInTime.isNullOrBlank() || record.overtimeHours > 0 || record.lateMinutes > 0)) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val workedHrs = DateUtils.calculateWorkingHours(record.checkInTime, record.checkOutTime)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "In: ${record.checkInTime ?: "-"}  |  Out: ${record.checkOutTime ?: "-"}" +
                                                (if (workedHrs > 0) "  |  Work: ${String.format("%.1f", workedHrs)}h" else "") +
                                                (if (record.lateMinutes > 0) "  |  Late: ${record.lateMinutes}m" else "") +
                                                (if (record.overtimeHours > 0) "  |  OT: ${record.overtimeHours}h" else ""),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                // One-Click Status Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AttendanceStatus.entries.forEach { st ->
                                        val isSelected = record?.status == st
                                        val (fg, bg) = attendanceStatusColors(st, uiState.settings.themeMode)
                                        Surface(
                                            onClick = {
                                                val defaultIn = if (st == AttendanceStatus.PRESENT || st == AttendanceStatus.LATE || st == AttendanceStatus.HALF_DAY) {
                                                    record?.checkInTime ?: uiState.settings.officeStartTime
                                                } else null
                                                val defaultOut = when (st) {
                                                    AttendanceStatus.PRESENT, AttendanceStatus.LATE -> record?.checkOutTime ?: uiState.settings.officeEndTime
                                                    AttendanceStatus.HALF_DAY -> record?.checkOutTime ?: "13:00"
                                                    else -> null
                                                }
                                                markWithHazriBanner(
                                                    emp,
                                                    selectedDate,
                                                    st,
                                                    defaultIn,
                                                    defaultOut,
                                                    record?.overtimeHours,
                                                    record?.notes ?: ""
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) fg else bg.copy(alpha = 0.65f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .testTag("mark_${emp.employeeCode}_${st.shortCode}")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = st.shortCode,
                                                    color = if (isSelected) Color.White else fg,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
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

            1 -> {
                AttendanceCalendarTab(
                    uiState = uiState,
                    selectedMonth = selectedMonth,
                    selectedDate = selectedDate,
                    onMonthChange = { selectedMonth = it },
                    onSelectDate = {
                        selectedDate = it
                        selectedTab = 0
                    }
                )
            }

            2 -> {
                AttendanceHistoryTab(
                    uiState = uiState,
                    empMap = empMap,
                    onEditRecord = { emp, rec -> editingPair = emp to rec }
                )
            }
        }
    }

    if (showBulkDialog) {
        BulkAttendanceDialog(
            dateDisplay = DateUtils.formatDisplayDate(selectedDate, uiState.settings.dateFormat),
            activeCount = activeEmployees.size,
            onApply = { status, overwrite ->
                onBulkMarkAttendance(uiState.employees, selectedDate, status, overwrite)
                hazriBanner = "All Active Staff (${activeEmployees.size})" to status
                showBulkDialog = false
            },
            onDismiss = { showBulkDialog = false }
        )
    }

    editingPair?.let { (emp, existing) ->
        AttendanceDetailDialog(
            employee = emp,
            date = existing?.date ?: selectedDate,
            existing = existing,
            defaultStart = uiState.settings.officeStartTime,
            defaultEnd = uiState.settings.officeEndTime,
            onSave = { status, checkIn, checkOut, otHours, notes ->
                markWithHazriBanner(emp, existing?.date ?: selectedDate, status, checkIn, checkOut, otHours, notes)
                editingPair = null
            },
            onDismiss = { editingPair = null }
        )
    }
}

@Composable
private fun AttendanceCalendarTab(
    uiState: StaffTrackUiState,
    selectedMonth: String,
    selectedDate: String,
    onMonthChange: (String) -> Unit,
    onSelectDate: (String) -> Unit
) {
    val dates = remember(selectedMonth) { DateUtils.datesInMonth(selectedMonth) }
    val offset = remember(selectedMonth) { DateUtils.firstDayOffsetMondayStart(selectedMonth) }
    val monthAttByDate = remember(uiState.attendance, selectedMonth) {
        uiState.attendance.filter { it.date.startsWith(selectedMonth) }.groupBy { it.date }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onMonthChange(DateUtils.shiftMonth(selectedMonth, -1)) }) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                    }
                    Text(
                        text = DateUtils.formatMonthTitle(selectedMonth),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onMonthChange(DateUtils.shiftMonth(selectedMonth, 1)) }) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                    }
                }
            }
        }

        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val cells = List(offset) { null } + dates
                    cells.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            week.forEach { date ->
                                if (date == null) {
                                    Spacer(modifier = Modifier.weight(1f).height(54.dp))
                                } else {
                                    val dayNum = date.takeLast(2).toIntOrNull()?.toString() ?: date
                                    val records = monthAttByDate[date].orEmpty()
                                    val presentCount = records.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }
                                    val absentCount = records.count { it.status == AttendanceStatus.ABSENT }
                                    val leaveCount = records.count { it.status == AttendanceStatus.ON_LEAVE }
                                    val isToday = date == uiState.todayDate
                                    val isSelected = date == selectedDate

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                            )
                                            .then(
                                                if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                                else Modifier
                                            )
                                            .clickable { onSelectDate(date) }
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = dayNum,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.align(Alignment.TopStart)
                                        )
                                        if (records.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.align(Alignment.BottomCenter),
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                if (presentCount > 0) Box(Modifier.size(6.dp).clip(CircleShape).background(PresentGreen))
                                                if (leaveCount > 0) Box(Modifier.size(6.dp).clip(CircleShape).background(LeaveBlue))
                                                if (absentCount > 0) Box(Modifier.size(6.dp).clip(CircleShape).background(AbsentRed))
                                            }
                                        }
                                    }
                                }
                            }
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier.weight(1f).height(54.dp))
                            }
                        }
                    }

                    Text(
                        text = "Tap any date to inspect or edit daily attendance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceHistoryTab(
    uiState: StaffTrackUiState,
    empMap: Map<Long, EmployeeEntity>,
    onEditRecord: (EmployeeEntity, AttendanceEntity) -> Unit
) {
    var periodIndex by remember { mutableIntStateOf(2) } // 0=Daily, 1=Weekly, 2=Monthly
    var selectedEmpId by remember { mutableStateOf<Long?>(null) }
    var empDropdownExpanded by remember { mutableStateOf(false) }

    val startDate = remember(periodIndex, uiState.todayDate, uiState.currentMonth) {
        when (periodIndex) {
            0 -> uiState.todayDate
            1 -> DateUtils.shiftDate(uiState.todayDate, -6)
            else -> "${uiState.currentMonth}-01"
        }
    }

    val filtered = remember(uiState.attendance, startDate, selectedEmpId) {
        uiState.attendance.filter { rec ->
            rec.date >= startDate && (selectedEmpId == null || rec.employeeId == selectedEmpId)
        }.sortedByDescending { it.date }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("Daily", "Weekly", "Monthly").forEachIndexed { idx, label ->
                    SegmentedButton(
                        selected = periodIndex == idx,
                        onClick = { periodIndex = idx },
                        shape = SegmentedButtonDefaults.itemShape(idx, 3)
                    ) { Text(label) }
                }
            }

            ExposedDropdownMenuBox(
                expanded = empDropdownExpanded,
                onExpandedChange = { empDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedEmpId?.let { empMap[it]?.fullName } ?: "All Employees",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter by Employee") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = empDropdownExpanded,
                    onDismissRequest = { empDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Employees") },
                        onClick = { selectedEmpId = null; empDropdownExpanded = false }
                    )
                    uiState.employees.forEach { emp ->
                        DropdownMenuItem(
                            text = { Text("${emp.fullName} (${emp.employeeCode})") },
                            onClick = { selectedEmpId = emp.id; empDropdownExpanded = false }
                        )
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { rec ->
                val emp = empMap[rec.employeeId]
                SaasCard(
                    onClick = { if (emp != null) onEditRecord(emp, rec) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = emp?.fullName ?: "Employee #${rec.employeeId}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${DateUtils.formatDisplayDate(rec.date, uiState.settings.dateFormat)} • In ${rec.checkInTime ?: "-"} / Out ${rec.checkOutTime ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (rec.overtimeHours > 0 || rec.lateMinutes > 0) {
                                Text(
                                    text = buildString {
                                        if (rec.lateMinutes > 0) append("Late: ${rec.lateMinutes}m  ")
                                        if (rec.overtimeHours > 0) append("Overtime: ${rec.overtimeHours}h")
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LateAmber
                                )
                            }
                        }
                        AttendanceBadge(rec.status, uiState.settings.themeMode)
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkAttendanceDialog(
    dateDisplay: String,
    activeCount: Int,
    onApply: (AttendanceStatus, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(AttendanceStatus.PRESENT) }
    var overwriteExisting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk Mark Attendance", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Apply status across $activeCount active employees for $dateDisplay.",
                    style = MaterialTheme.typography.bodySmall
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        AttendanceStatus.PRESENT,
                        AttendanceStatus.ABSENT,
                        AttendanceStatus.HALF_DAY,
                        AttendanceStatus.HOLIDAY
                    ).forEach { st ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(st.label) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { overwriteExisting = !overwriteExisting }
                ) {
                    Checkbox(checked = overwriteExisting, onCheckedChange = { overwriteExisting = it })
                    Text(
                        text = "Overwrite already marked employees",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onApply(selectedStatus, overwriteExisting) }) {
                Text("Apply Bulk Mark")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AttendanceDetailDialog(
    employee: EmployeeEntity,
    date: String,
    existing: AttendanceEntity?,
    defaultStart: String,
    defaultEnd: String,
    onSave: (AttendanceStatus, String?, String?, Double?, String) -> Unit,
    onDismiss: () -> Unit
) {
    var status by remember(existing) { mutableStateOf(existing?.status ?: AttendanceStatus.PRESENT) }
    var checkIn by remember(existing) { mutableStateOf(existing?.checkInTime ?: defaultStart) }
    var checkOut by remember(existing) { mutableStateOf(existing?.checkOutTime ?: defaultEnd) }
    var otText by remember(existing) { mutableStateOf((existing?.overtimeHours ?: 0.0).toString()) }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(employee.fullName, fontWeight = FontWeight.Bold)
                Text("Attendance for $date", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AttendanceStatus.entries) { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = checkIn,
                        onValueChange = { checkIn = it },
                        label = { Text("Check-In (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = checkOut,
                        onValueChange = { checkOut = it },
                        label = { Text("Check-Out (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = otText,
                    onValueChange = { otText = it },
                    label = { Text("Overtime Hours (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Remarks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val ot = otText.toDoubleOrNull()
                onSave(
                    status,
                    checkIn.trim().takeIf { it.isNotEmpty() },
                    checkOut.trim().takeIf { it.isNotEmpty() },
                    ot,
                    notes.trim()
                )
            }) {
                Text("Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
