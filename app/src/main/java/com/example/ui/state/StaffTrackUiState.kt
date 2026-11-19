package com.example.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.LeaveRequestEntity
import com.example.data.local.entity.PayrollRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.CompanySettings
import com.example.data.model.EmploymentStatus
import com.example.data.model.LeaveCategory
import com.example.data.model.LeaveStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ReportType
import com.example.data.model.UserRole
import com.example.data.preferences.UserSession
import com.example.data.repository.BackupValidationResult
import com.example.data.repository.StaffTrackBackup
import com.example.data.repository.StaffTrackRepository
import com.example.domain.DateUtils
import com.example.util.NetworkMonitor
import com.example.util.ReportTableData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val totalActiveEmployees: Int = 0,
    val presentToday: Int = 0,
    val absentToday: Int = 0,
    val onLeaveToday: Int = 0,
    val lateToday: Int = 0,
    val halfDayToday: Int = 0,
    val currentMonthPayrollTotal: Double = 0.0,
    val pendingSalaryTotal: Double = 0.0,
    val pendingLeaveCount: Int = 0
)

data class DailyChartPoint(
    val dayLabel: String,
    val present: Int,
    val late: Int,
    val halfDay: Int,
    val leave: Int,
    val absent: Int
)

data class StaffTrackUiState(
    val isLoading: Boolean = true,
    val isOnline: Boolean = false,
    val settings: CompanySettings = CompanySettings(),
    val employees: List<EmployeeEntity> = emptyList(),
    val attendance: List<AttendanceEntity> = emptyList(),
    val leaves: List<LeaveRequestEntity> = emptyList(),
    val payrolls: List<PayrollRecordEntity> = emptyList(),
    val activityLogs: List<ActivityLogEntity> = emptyList(),
    val dashboardMetrics: DashboardMetrics = DashboardMetrics(),
    val monthlyChartPoints: List<DailyChartPoint> = emptyList(),
    val todayDate: String = DateUtils.todayIso(),
    val currentMonth: String = DateUtils.currentYearMonth(),
    val session: UserSession = UserSession()
)

class StaffTrackViewModel(
    private val repository: StaffTrackRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _isLoading = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            repository.ensureDemoDataSeeded()
            _isLoading.value = false
        }
    }

    val uiState: StateFlow<StaffTrackUiState> = combine(
        _isLoading,
        networkMonitor.isOnline,
        repository.settingsFlow,
        repository.employeesFlow,
        repository.attendanceFlow,
        repository.leavesFlow,
        repository.payrollFlow,
        repository.activityLogsFlow,
        repository.sessionFlow
    ) { values ->
        val loading = values[0] as Boolean
        val online = values[1] as Boolean
        val settings = values[2] as CompanySettings
        @Suppress("UNCHECKED_CAST")
        val employees = values[3] as List<EmployeeEntity>
        @Suppress("UNCHECKED_CAST")
        val attendance = values[4] as List<AttendanceEntity>
        @Suppress("UNCHECKED_CAST")
        val leaves = values[5] as List<LeaveRequestEntity>
        @Suppress("UNCHECKED_CAST")
        val payrolls = values[6] as List<PayrollRecordEntity>
        @Suppress("UNCHECKED_CAST")
        val logs = values[7] as List<ActivityLogEntity>
        val session = values[8] as UserSession

        val today = DateUtils.todayIso()
        val currentMonth = DateUtils.currentYearMonth()
        val activeEmployees = employees.filter { it.status == EmploymentStatus.ACTIVE }
        val activeIds = activeEmployees.map { it.id }.toSet()
        val todayAtt = attendance.filter { it.date == today && it.employeeId in activeIds }
        val monthPayrolls = payrolls.filter { it.yearMonth == currentMonth }

        val metrics = DashboardMetrics(
            totalActiveEmployees = activeEmployees.size,
            presentToday = todayAtt.count { it.status == AttendanceStatus.PRESENT },
            absentToday = todayAtt.count { it.status == AttendanceStatus.ABSENT },
            onLeaveToday = todayAtt.count { it.status == AttendanceStatus.ON_LEAVE },
            lateToday = todayAtt.count { it.status == AttendanceStatus.LATE },
            halfDayToday = todayAtt.count { it.status == AttendanceStatus.HALF_DAY },
            currentMonthPayrollTotal = monthPayrolls.sumOf { it.netPayableSalary },
            pendingSalaryTotal = monthPayrolls.sumOf { (it.netPayableSalary - it.paidAmount).coerceAtLeast(0.0) },
            pendingLeaveCount = leaves.count { it.status == LeaveStatus.PENDING }
        )

        // Build chart points for the last 14 active dates of the current month
        val monthDates = DateUtils.datesInMonth(currentMonth)
            .filter { it <= today }
            .takeLast(14)
        val attByDate = attendance.filter { it.date.startsWith(currentMonth) }.groupBy { it.date }
        val chartPoints = monthDates.map { date ->
            val dayAtt = attByDate[date].orEmpty()
            DailyChartPoint(
                dayLabel = date.takeLast(2),
                present = dayAtt.count { it.status == AttendanceStatus.PRESENT },
                late = dayAtt.count { it.status == AttendanceStatus.LATE },
                halfDay = dayAtt.count { it.status == AttendanceStatus.HALF_DAY },
                leave = dayAtt.count { it.status == AttendanceStatus.ON_LEAVE },
                absent = dayAtt.count { it.status == AttendanceStatus.ABSENT }
            )
        }

        StaffTrackUiState(
            isLoading = loading,
            isOnline = online,
            settings = settings,
            employees = employees,
            attendance = attendance,
            leaves = leaves,
            payrolls = payrolls,
            activityLogs = logs,
            dashboardMetrics = metrics,
            monthlyChartPoints = chartPoints,
            todayDate = today,
            currentMonth = currentMonth,
            session = session
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StaffTrackUiState()
    )

    // --- Session Actions ---

    fun loginAsAdmin() {
        viewModelScope.launch {
            repository.setSession(isLoggedIn = true, role = UserRole.ADMIN, employeeId = null)
        }
    }

    fun loginAsEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.setSession(isLoggedIn = true, role = UserRole.EMPLOYEE, employeeId = employee.id)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.setSession(isLoggedIn = false, role = UserRole.ADMIN, employeeId = null)
        }
    }

    // --- Employee Actions ---

    suspend fun getNextEmployeeCode(): String = repository.generateNextEmployeeCode()

    fun saveEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.saveEmployee(employee)
            _messages.emit(if (employee.id == 0L) "Employee added & saved locally" else "Employee updated")
        }
    }

    fun toggleEmployeeStatus(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.toggleEmployeeActive(employee)
            val newStatus = if (employee.status == EmploymentStatus.ACTIVE) "deactivated" else "activated"
            _messages.emit("${employee.fullName} $newStatus")
        }
    }

    fun deleteEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
            _messages.emit("${employee.fullName} deleted")
        }
    }

    // --- Attendance Actions ---

    fun markAttendance(
        employee: EmployeeEntity,
        date: String,
        status: AttendanceStatus,
        checkInTime: String?,
        checkOutTime: String?,
        overtimeHours: Double?,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.markAttendance(employee, date, status, checkInTime, checkOutTime, overtimeHours, notes)
            _messages.emit("${employee.fullName}: ${status.label} saved")
        }
    }

    fun bulkMarkAttendance(
        employees: List<EmployeeEntity>,
        date: String,
        status: AttendanceStatus,
        overwriteExisting: Boolean
    ) {
        viewModelScope.launch {
            val count = repository.bulkMarkAttendance(employees, date, status, overwriteExisting)
            if (count > 0) {
                _messages.emit("Marked $count employees as ${status.label}")
            } else {
                _messages.emit("All active employees already have entries for $date")
            }
        }
    }

    // --- Leave Actions ---

    fun submitLeave(
        employee: EmployeeEntity,
        category: LeaveCategory,
        startDate: String,
        endDate: String,
        reason: String
    ) {
        viewModelScope.launch {
            repository.submitLeaveRequest(employee, category, startDate, endDate, reason)
            _messages.emit("Leave request submitted for ${employee.fullName}")
        }
    }

    fun approveLeave(leave: LeaveRequestEntity, employeeName: String) {
        viewModelScope.launch {
            repository.approveLeave(leave, employeeName)
            _messages.emit("Leave approved & synced to attendance")
        }
    }

    fun rejectLeave(leave: LeaveRequestEntity, employeeName: String) {
        viewModelScope.launch {
            repository.rejectLeave(leave, employeeName)
            _messages.emit("Leave request rejected")
        }
    }

    fun deleteLeave(leave: LeaveRequestEntity) {
        viewModelScope.launch {
            repository.deleteLeave(leave)
            _messages.emit("Leave request removed")
        }
    }

    // --- Payroll Actions ---

    fun generatePayroll(yearMonth: String) {
        viewModelScope.launch {
            repository.generatePayrollForMonth(yearMonth)
            _messages.emit("Payroll calculated for ${DateUtils.formatMonthTitle(yearMonth)}")
        }
    }

    fun updatePayrollRecord(
        record: PayrollRecordEntity,
        employee: EmployeeEntity,
        bonuses: Double,
        allowances: Double,
        advanceSalary: Double,
        otherDeductions: Double,
        paidAmount: Double,
        isApproved: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            repository.updatePayrollAdjustments(
                record, employee, bonuses, allowances, advanceSalary, otherDeductions, paidAmount, isApproved, notes
            )
            _messages.emit("Payroll updated for ${employee.fullName}")
        }
    }

    fun approveAllPayroll(yearMonth: String) {
        viewModelScope.launch {
            repository.approveAllPayrollForMonth(yearMonth)
            _messages.emit("All payroll slips approved for ${DateUtils.formatMonthTitle(yearMonth)}")
        }
    }

    // --- Settings & Backup ---

    fun saveSettings(settings: CompanySettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            _messages.emit("Settings saved locally")
        }
    }

    suspend fun exportBackupJson(): String = repository.exportBackupJson()

    fun validateBackupJson(rawJson: String): BackupValidationResult = repository.validateBackupJson(rawJson)

    fun restoreFromBackup(backup: StaffTrackBackup) {
        viewModelScope.launch {
            repository.restoreFromBackup(backup)
            _messages.emit("Database restored from backup (${backup.employees.size} employees)")
        }
    }

    fun emitMessage(message: String) {
        viewModelScope.launch { _messages.emit(message) }
    }

    // --- Report Builder ---

    fun buildReportTable(
        reportType: ReportType,
        startDate: String,
        endDate: String,
        yearMonth: String,
        departmentFilter: String,
        employeeFilterId: Long?
    ): ReportTableData {
        val state = uiState.value
        val cur = state.settings.currency
        val fmt = state.settings.dateFormat

        val filteredEmployees = state.employees.filter { emp ->
            (departmentFilter == "All" || emp.department == departmentFilter) &&
                (employeeFilterId == null || emp.id == employeeFilterId)
        }
        val empMap = state.employees.associateBy { it.id }
        val filteredIds = filteredEmployees.map { it.id }.toSet()

        val rangeAtt = state.attendance.filter {
            it.employeeId in filteredIds && it.date >= startDate && it.date <= endDate
        }
        val rangeLeaves = state.leaves.filter {
            it.employeeId in filteredIds && it.endDate >= startDate && it.startDate <= endDate
        }
        val monthPayrolls = state.payrolls.filter {
            it.yearMonth == yearMonth && it.employeeId in filteredIds
        }

        val rangeLabel = "${DateUtils.formatDisplayDate(startDate, fmt)} to ${DateUtils.formatDisplayDate(endDate, fmt)}"

        return when (reportType) {
            ReportType.MONTHLY_ATTENDANCE -> {
                val monthAtt = state.attendance.filter {
                    it.employeeId in filteredIds && it.date.startsWith(yearMonth)
                }
                val rows = filteredEmployees.map { emp ->
                    val list = monthAtt.filter { it.employeeId == emp.id }
                    listOf(
                        emp.employeeCode,
                        emp.fullName,
                        emp.department,
                        list.count { it.status == AttendanceStatus.PRESENT }.toString(),
                        list.count { it.status == AttendanceStatus.LATE }.toString(),
                        list.count { it.status == AttendanceStatus.HALF_DAY }.toString(),
                        list.count { it.status == AttendanceStatus.ON_LEAVE }.toString(),
                        list.count { it.status == AttendanceStatus.ABSENT }.toString(),
                        String.format("%.1f h", list.sumOf { it.overtimeHours })
                    )
                }
                ReportTableData(
                    title = "Monthly Attendance Report",
                    subtitle = "${DateUtils.formatMonthTitle(yearMonth)} • Dept: $departmentFilter",
                    headers = listOf("ID", "Employee", "Dept", "Present", "Late", "Half", "Leave", "Absent", "OT"),
                    rows = rows,
                    summaryLines = listOf(
                        "Employees Covered: ${filteredEmployees.size}",
                        "Total Present Days: ${monthAtt.count { it.status == AttendanceStatus.PRESENT }}  |  Absences: ${monthAtt.count { it.status == AttendanceStatus.ABSENT }}"
                    )
                )
            }

            ReportType.EMPLOYEE_HISTORY -> {
                val targetEmp = employeeFilterId?.let { empMap[it] } ?: filteredEmployees.firstOrNull()
                val empAtt = if (targetEmp != null) {
                    rangeAtt.filter { it.employeeId == targetEmp.id }.sortedByDescending { it.date }
                } else emptyList()
                val rows = empAtt.map { a ->
                    listOf(
                        DateUtils.formatDisplayDate(a.date, fmt),
                        a.status.label,
                        a.checkInTime ?: "-",
                        a.checkOutTime ?: "-",
                        if (a.lateMinutes > 0) "${a.lateMinutes}m" else "-",
                        if (a.overtimeHours > 0) "${a.overtimeHours}h" else "-",
                        a.notes.ifBlank { "-" }
                    )
                }
                ReportTableData(
                    title = "Employee Attendance History",
                    subtitle = "${targetEmp?.fullName ?: "All"} (${targetEmp?.employeeCode ?: "-"}) • $rangeLabel",
                    headers = listOf("Date", "Status", "In", "Out", "Late", "OT", "Notes"),
                    rows = rows,
                    summaryLines = listOf("Total Records: ${empAtt.size}")
                )
            }

            ReportType.ABSENCE_REPORT -> {
                val absences = rangeAtt.filter {
                    it.status == AttendanceStatus.ABSENT || it.status == AttendanceStatus.ON_LEAVE
                }.sortedByDescending { it.date }
                val rows = absences.map { a ->
                    val emp = empMap[a.employeeId]
                    listOf(
                        DateUtils.formatDisplayDate(a.date, fmt),
                        emp?.employeeCode ?: "-",
                        emp?.fullName ?: "Unknown",
                        emp?.department ?: "-",
                        a.status.label,
                        a.notes.ifBlank { "-" }
                    )
                }
                ReportTableData(
                    title = "Absence Report",
                    subtitle = "$rangeLabel • Dept: $departmentFilter",
                    headers = listOf("Date", "ID", "Employee", "Department", "Status", "Notes"),
                    rows = rows,
                    summaryLines = listOf(
                        "Total Absent Entries: ${absences.count { it.status == AttendanceStatus.ABSENT }}  |  On Leave: ${absences.count { it.status == AttendanceStatus.ON_LEAVE }}"
                    )
                )
            }

            ReportType.LATE_ARRIVAL -> {
                val lates = rangeAtt.filter { it.status == AttendanceStatus.LATE || it.lateMinutes > 0 }
                    .sortedByDescending { it.date }
                val rows = lates.map { a ->
                    val emp = empMap[a.employeeId]
                    listOf(
                        DateUtils.formatDisplayDate(a.date, fmt),
                        emp?.employeeCode ?: "-",
                        emp?.fullName ?: "Unknown",
                        emp?.department ?: "-",
                        a.checkInTime ?: "-",
                        "${a.lateMinutes} min"
                    )
                }
                ReportTableData(
                    title = "Late Arrival Report",
                    subtitle = "$rangeLabel • Office Start: ${state.settings.officeStartTime}",
                    headers = listOf("Date", "ID", "Employee", "Department", "Check-In", "Delayed"),
                    rows = rows,
                    summaryLines = listOf(
                        "Total Late Arrivals: ${lates.size}  |  Total Minutes Delayed: ${lates.sumOf { it.lateMinutes }} min"
                    )
                )
            }

            ReportType.LEAVE_REPORT -> {
                val rows = rangeLeaves.map { l ->
                    val emp = empMap[l.employeeId]
                    listOf(
                        emp?.employeeCode ?: "-",
                        emp?.fullName ?: "Unknown",
                        l.category.label,
                        DateUtils.formatDisplayDate(l.startDate, fmt),
                        DateUtils.formatDisplayDate(l.endDate, fmt),
                        "${l.totalDays}d",
                        l.status.label
                    )
                }
                ReportTableData(
                    title = "Leave Report",
                    subtitle = "$rangeLabel • Dept: $departmentFilter",
                    headers = listOf("ID", "Employee", "Category", "From", "To", "Days", "Status"),
                    rows = rows,
                    summaryLines = listOf(
                        "Approved Days: ${rangeLeaves.filter { it.status == LeaveStatus.APPROVED }.sumOf { it.totalDays }}  |  Pending: ${rangeLeaves.count { it.status == LeaveStatus.PENDING }}"
                    )
                )
            }

            ReportType.OVERTIME_REPORT -> {
                val otRecords = rangeAtt.filter { it.overtimeHours > 0.0 }
                val grouped = otRecords.groupBy { it.employeeId }
                val rows = grouped.map { (empId, list) ->
                    val emp = empMap[empId]
                    val hours = list.sumOf { it.overtimeHours }
                    val payMatch = state.payrolls.firstOrNull { it.employeeId == empId && it.yearMonth == yearMonth }
                    val estPay = payMatch?.overtimePay ?: 0.0
                    listOf(
                        emp?.employeeCode ?: "-",
                        emp?.fullName ?: "Unknown",
                        emp?.department ?: "-",
                        list.size.toString(),
                        String.format("%.1f hrs", hours),
                        DateUtils.formatCurrency(estPay, cur)
                    )
                }
                ReportTableData(
                    title = "Overtime Report",
                    subtitle = "$rangeLabel • Dept: $departmentFilter",
                    headers = listOf("ID", "Employee", "Department", "OT Days", "Total Hours", "OT Pay"),
                    rows = rows,
                    summaryLines = listOf(
                        "Total Overtime Hours: ${String.format("%.1f", otRecords.sumOf { it.overtimeHours })} hrs"
                    )
                )
            }

            ReportType.MONTHLY_PAYROLL -> {
                val rows = monthPayrolls.map { p ->
                    val emp = empMap[p.employeeId]
                    listOf(
                        emp?.employeeCode ?: "-",
                        emp?.fullName ?: "Unknown",
                        DateUtils.formatCurrency(p.grossSalary, cur),
                        DateUtils.formatCurrency(p.overtimePay + p.bonuses + p.allowances, cur),
                        DateUtils.formatCurrency(p.totalDeductions, cur),
                        DateUtils.formatCurrency(p.netPayableSalary, cur),
                        p.paymentStatus.label
                    )
                }
                ReportTableData(
                    title = "Monthly Payroll Report",
                    subtitle = "${DateUtils.formatMonthTitle(yearMonth)} • Dept: $departmentFilter",
                    headers = listOf("ID", "Employee", "Gross", "OT/Bonus", "Deduct", "Net Pay", "Status"),
                    rows = rows,
                    summaryLines = listOf(
                        "Total Net Payroll: ${DateUtils.formatCurrency(monthPayrolls.sumOf { it.netPayableSalary }, cur)}",
                        "Total Paid: ${DateUtils.formatCurrency(monthPayrolls.sumOf { it.paidAmount }, cur)}  |  Pending: ${DateUtils.formatCurrency(monthPayrolls.sumOf { (it.netPayableSalary - it.paidAmount).coerceAtLeast(0.0) }, cur)}"
                    )
                )
            }

            ReportType.SALARY_SLIP -> {
                val targetEmp = employeeFilterId?.let { empMap[it] } ?: filteredEmployees.firstOrNull()
                val slip = targetEmp?.let { emp -> monthPayrolls.firstOrNull { it.employeeId == emp.id } }
                val rows = if (targetEmp != null && slip != null) {
                    listOf(
                        listOf("Employee", "${targetEmp.fullName} (${targetEmp.employeeCode})", "Department", targetEmp.department),
                        listOf("Designation", targetEmp.designation, "Salary Type", slip.salaryType.label),
                        listOf("Present / Late", "${slip.presentDays} / ${slip.lateDays} days", "Half / Absent", "${slip.halfDays} / ${slip.absentDays} days"),
                        listOf("Paid / Unpaid Leave", "${slip.paidLeaveDays} / ${slip.unpaidLeaveDays} days", "Overtime", "${slip.overtimeHours} hrs"),
                        listOf("Gross Basic Salary", DateUtils.formatCurrency(slip.grossSalary, cur), "Absence Deduction", DateUtils.formatCurrency(slip.absenceDeduction, cur)),
                        listOf("Overtime Pay", DateUtils.formatCurrency(slip.overtimePay, cur), "Half/Late/Leave Ded.", DateUtils.formatCurrency(slip.halfDayDeduction + slip.lateDeduction + slip.unpaidLeaveDeduction, cur)),
                        listOf("Bonuses & Allowances", DateUtils.formatCurrency(slip.bonuses + slip.allowances, cur), "Advance & Other Ded.", DateUtils.formatCurrency(slip.advanceSalary + slip.otherDeductions, cur)),
                        listOf("Total Earnings", DateUtils.formatCurrency(slip.totalEarnings, cur), "Total Deductions", DateUtils.formatCurrency(slip.totalDeductions, cur)),
                        listOf("NET PAYABLE SALARY", DateUtils.formatCurrency(slip.netPayableSalary, cur), "Payment Status", "${slip.paymentStatus.label} (${DateUtils.formatCurrency(slip.paidAmount, cur)})")
                    )
                } else emptyList()

                ReportTableData(
                    title = "Individual Salary Slip",
                    subtitle = "${targetEmp?.fullName ?: "No Employee"} • ${DateUtils.formatMonthTitle(yearMonth)}",
                    headers = listOf("Earning / Info", "Value", "Deduction / Meta", "Value"),
                    rows = rows,
                    summaryLines = listOf(
                        if (slip != null) "Approval: ${if (slip.isApproved) "Approved" else "Pending Admin Review"}" else "Generate payroll for $yearMonth to view salary slip."
                    )
                )
            }

            ReportType.TOTAL_SALARY_EXPENSE -> {
                val byDept = monthPayrolls.groupBy { empMap[it.employeeId]?.department ?: "Other" }
                val rows = byDept.map { (dept, list) ->
                    listOf(
                        dept,
                        list.size.toString(),
                        DateUtils.formatCurrency(list.sumOf { it.totalEarnings }, cur),
                        DateUtils.formatCurrency(list.sumOf { it.totalDeductions }, cur),
                        DateUtils.formatCurrency(list.sumOf { it.netPayableSalary }, cur),
                        DateUtils.formatCurrency(list.sumOf { it.paidAmount }, cur)
                    )
                }
                ReportTableData(
                    title = "Total Salary Expense",
                    subtitle = "${DateUtils.formatMonthTitle(yearMonth)} • Organization Breakdown",
                    headers = listOf("Department", "Staff", "Total Earnings", "Deductions", "Net Expense", "Disbursed"),
                    rows = rows,
                    summaryLines = listOf(
                        "Organization Net Expense: ${DateUtils.formatCurrency(monthPayrolls.sumOf { it.netPayableSalary }, cur)}",
                        "Total Disbursed: ${DateUtils.formatCurrency(monthPayrolls.sumOf { it.paidAmount }, cur)}"
                    )
                )
            }
        }
    }

    class Factory(
        private val repository: StaffTrackRepository,
        private val networkMonitor: NetworkMonitor
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StaffTrackViewModel(repository, networkMonitor) as T
        }
    }
}
