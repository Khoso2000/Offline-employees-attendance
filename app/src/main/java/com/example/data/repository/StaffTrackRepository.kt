package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.StaffTrackDatabase
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
import com.example.data.model.SalaryType
import com.example.data.model.UserRole
import com.example.data.preferences.SettingsRepository
import com.example.data.preferences.UserSession
import com.example.domain.DateUtils
import com.example.domain.PayrollCalculator
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

@JsonClass(generateAdapter = true)
data class StaffTrackBackup(
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appName: String = "StaffTrack",
    val settings: CompanySettings,
    val employees: List<EmployeeEntity>,
    val attendance: List<AttendanceEntity>,
    val leaveRequests: List<LeaveRequestEntity>,
    val payrollRecords: List<PayrollRecordEntity>,
    val activityLogs: List<ActivityLogEntity>
)

data class BackupValidationResult(
    val isValid: Boolean,
    val backup: StaffTrackBackup? = null,
    val errorMessage: String? = null
)

class StaffTrackRepository(
    private val db: StaffTrackDatabase,
    private val settingsRepo: SettingsRepository
) {
    private val employeeDao = db.employeeDao()
    private val attendanceDao = db.attendanceDao()
    private val leaveDao = db.leaveDao()
    private val payrollDao = db.payrollDao()
    private val activityLogDao = db.activityLogDao()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(StaffTrackBackup::class.java).indent("  ")

    val employeesFlow: Flow<List<EmployeeEntity>> = employeeDao.observeAll()
    val attendanceFlow: Flow<List<AttendanceEntity>> = attendanceDao.observeAll()
    val leavesFlow: Flow<List<LeaveRequestEntity>> = leaveDao.observeAll()
    val payrollFlow: Flow<List<PayrollRecordEntity>> = payrollDao.observeAll()
    val activityLogsFlow: Flow<List<ActivityLogEntity>> = activityLogDao.observeRecent()
    val settingsFlow: Flow<CompanySettings> = settingsRepo.settingsFlow
    val sessionFlow: Flow<UserSession> = settingsRepo.sessionFlow

    suspend fun setSession(isLoggedIn: Boolean, role: UserRole, employeeId: Long?) {
        settingsRepo.setSession(isLoggedIn, role, employeeId)
    }

    suspend fun ensureDemoDataSeeded() {
        if (settingsRepo.isSeeded() || employeeDao.count() > 0) return

        val settings = settingsRepo.getSettingsOnce()
        val today = DateUtils.todayIso()
        val currentMonth = DateUtils.currentYearMonth()
        val now = System.currentTimeMillis()

        db.withTransaction {
            val demoEmployees = listOf(
                EmployeeEntity(
                    employeeCode = "ST-1001",
                    fullName = "Ahsan Raza Khan",
                    phone = "+92 300 4128890",
                    email = "ahsan.raza@apexdynamics.pk",
                    address = "Gulberg III, Lahore",
                    department = "Engineering",
                    designation = "Senior Software Engineer",
                    joiningDate = "2024-03-15",
                    salaryType = SalaryType.MONTHLY,
                    basicSalary = 165000.0,
                    workingHoursPerDay = 8.0,
                    weeklyHoliday = "SUNDAY",
                    status = EmploymentStatus.ACTIVE
                ),
                EmployeeEntity(
                    employeeCode = "ST-1002",
                    fullName = "Sana Tariq Mahmood",
                    phone = "+92 321 7894451",
                    email = "sana.tariq@apexdynamics.pk",
                    address = "DHA Phase 5, Lahore",
                    department = "Human Resources",
                    designation = "HR & Talent Lead",
                    joiningDate = "2023-11-01",
                    salaryType = SalaryType.MONTHLY,
                    basicSalary = 115000.0,
                    workingHoursPerDay = 8.0,
                    weeklyHoliday = "SUNDAY",
                    status = EmploymentStatus.ACTIVE
                ),
                EmployeeEntity(
                    employeeCode = "ST-1003",
                    fullName = "Bilal Ahmed Qureshi",
                    phone = "+92 333 9021145",
                    email = "bilal.qureshi@apexdynamics.pk",
                    address = "Clifton Block 4, Karachi",
                    department = "Sales",
                    designation = "Regional Sales Manager",
                    joiningDate = "2024-01-10",
                    salaryType = SalaryType.MONTHLY,
                    basicSalary = 135000.0,
                    workingHoursPerDay = 8.0,
                    weeklyHoliday = "SUNDAY",
                    status = EmploymentStatus.ACTIVE
                ),
                EmployeeEntity(
                    employeeCode = "ST-1004",
                    fullName = "Hira Fatima Siddiqui",
                    phone = "+92 345 6612098",
                    email = "hira.siddiqui@apexdynamics.pk",
                    address = "F-10 Markaz, Islamabad",
                    department = "Finance",
                    designation = "Accounts Specialist",
                    joiningDate = "2024-06-20",
                    salaryType = SalaryType.MONTHLY,
                    basicSalary = 98000.0,
                    workingHoursPerDay = 8.0,
                    weeklyHoliday = "SUNDAY",
                    status = EmploymentStatus.ACTIVE
                ),
                EmployeeEntity(
                    employeeCode = "ST-1005",
                    fullName = "Usman Ghani Malik",
                    phone = "+92 301 5539201",
                    email = "usman.malik@apexdynamics.pk",
                    address = "Model Town, Lahore",
                    department = "Operations",
                    designation = "Floor Supervisor",
                    joiningDate = "2023-08-05",
                    salaryType = SalaryType.DAILY,
                    basicSalary = 3500.0,
                    workingHoursPerDay = 8.0,
                    weeklyHoliday = "SUNDAY",
                    status = EmploymentStatus.ACTIVE
                ),
                EmployeeEntity(
                    employeeCode = "ST-1006",
                    fullName = "Zainab Noor Abbas",
                    phone = "+92 312 8421177",
                    email = "zainab.abbas@apexdynamics.pk",
                    address = "Johar Town, Lahore",
                    department = "Engineering",
                    designation = "QA Automation Analyst",
                    joiningDate = "2025-02-01",
                    salaryType = SalaryType.MONTHLY,
                    basicSalary = 95000.0,
                    workingHoursPerDay = 8.0,
                    weeklyHoliday = "SUNDAY",
                    status = EmploymentStatus.ACTIVE
                ),
                EmployeeEntity(
                    employeeCode = "ST-1007",
                    fullName = "Kamran Yousaf Butt",
                    phone = "+92 302 2298412",
                    email = "kamran.butt@apexdynamics.pk",
                    address = "Industrial Area, Sialkot",
                    department = "Operations",
                    designation = "Logistics Technician",
                    joiningDate = "2024-09-12",
                    salaryType = SalaryType.DAILY,
                    basicSalary = 2800.0,
                    workingHoursPerDay = 8.0,
                    weeklyHoliday = "SUNDAY",
                    status = EmploymentStatus.ACTIVE
                ),
                EmployeeEntity(
                    employeeCode = "ST-1008",
                    fullName = "Mariam Javed Sheikh",
                    phone = "+92 334 7189930",
                    email = "mariam.sheikh@apexdynamics.pk",
                    address = "Bahria Town, Rawalpindi",
                    department = "Sales",
                    designation = "Client Relations Executive",
                    joiningDate = "2025-04-15",
                    salaryType = SalaryType.MONTHLY,
                    basicSalary = 78000.0,
                    workingHoursPerDay = 8.0,
                    weeklyHoliday = "SUNDAY",
                    status = EmploymentStatus.ACTIVE
                )
            )

            demoEmployees.forEach { employeeDao.insert(it) }
            val savedEmployees = employeeDao.getAllOnce()

            // Create realistic month attendance up to today
            val monthDates = DateUtils.datesInMonth(currentMonth).filter { it <= today }
            val attendanceBatch = mutableListOf<AttendanceEntity>()

            monthDates.forEachIndexed { dayIdx, date ->
                val dow = DateUtils.dayOfWeekName(date)
                val isToday = date == today
                savedEmployees.forEachIndexed { empIdx, emp ->
                    if (dow == emp.weeklyHoliday) {
                        attendanceBatch.add(
                            AttendanceEntity(
                                employeeId = emp.id,
                                date = date,
                                status = AttendanceStatus.HOLIDAY,
                                notes = "Weekly Holiday"
                            )
                        )
                    } else if (isToday) {
                        val status = when (empIdx) {
                            0, 1, 5, 7 -> AttendanceStatus.PRESENT
                            2 -> AttendanceStatus.LATE
                            3 -> AttendanceStatus.ON_LEAVE
                            4 -> AttendanceStatus.HALF_DAY
                            else -> AttendanceStatus.ABSENT
                        }
                        attendanceBatch.add(
                            AttendanceEntity(
                                employeeId = emp.id,
                                date = date,
                                status = status,
                                checkInTime = when (status) {
                                    AttendanceStatus.PRESENT -> "08:56"
                                    AttendanceStatus.LATE -> "09:32"
                                    AttendanceStatus.HALF_DAY -> "09:04"
                                    else -> null
                                },
                                checkOutTime = when (status) {
                                    AttendanceStatus.PRESENT -> if (empIdx == 0) "18:30" else "17:05"
                                    AttendanceStatus.LATE -> "17:15"
                                    AttendanceStatus.HALF_DAY -> "13:15"
                                    else -> null
                                },
                                lateMinutes = if (status == AttendanceStatus.LATE) 32 else 0,
                                overtimeHours = if (status == AttendanceStatus.PRESENT && empIdx == 0) 1.5 else 0.0,
                                notes = if (status == AttendanceStatus.ON_LEAVE) "Approved Casual Leave" else ""
                            )
                        )
                    } else {
                        val pattern = (dayIdx + empIdx * 3) % 14
                        val status = when {
                            pattern == 0 -> AttendanceStatus.ABSENT
                            pattern == 1 -> AttendanceStatus.LATE
                            pattern == 2 && empIdx % 2 == 0 -> AttendanceStatus.HALF_DAY
                            else -> AttendanceStatus.PRESENT
                        }
                        val ot = if (status == AttendanceStatus.PRESENT && (dayIdx + empIdx) % 5 == 0) 1.5 else 0.0
                        attendanceBatch.add(
                            AttendanceEntity(
                                employeeId = emp.id,
                                date = date,
                                status = status,
                                checkInTime = when (status) {
                                    AttendanceStatus.PRESENT -> "08:58"
                                    AttendanceStatus.LATE -> "09:28"
                                    AttendanceStatus.HALF_DAY -> "09:02"
                                    else -> null
                                },
                                checkOutTime = when (status) {
                                    AttendanceStatus.PRESENT -> if (ot > 0) "18:30" else "17:02"
                                    AttendanceStatus.LATE -> "17:10"
                                    AttendanceStatus.HALF_DAY -> "13:10"
                                    else -> null
                                },
                                lateMinutes = if (status == AttendanceStatus.LATE) 28 else 0,
                                overtimeHours = ot
                            )
                        )
                    }
                }
            }
            attendanceDao.upsertAll(attendanceBatch)

            // Seed Leave Requests (Approved one for empIdx 3 covering today, plus Pending & Rejected)
            val emp3 = savedEmployees.getOrNull(3)
            val emp5 = savedEmployees.getOrNull(5)
            val emp6 = savedEmployees.getOrNull(6)
            if (emp3 != null) {
                val leaveId = leaveDao.insert(
                    LeaveRequestEntity(
                        employeeId = emp3.id,
                        category = LeaveCategory.CASUAL,
                        startDate = today,
                        endDate = today,
                        totalDays = 1,
                        reason = "Family commitment in Islamabad",
                        status = LeaveStatus.APPROVED,
                        isPaid = true,
                        reviewedAt = now - 86_400_000L
                    )
                )
                attendanceDao.getForEmployeeAndDate(emp3.id, today)?.let { existing ->
                    attendanceDao.upsert(existing.copy(status = AttendanceStatus.ON_LEAVE, leaveRequestId = leaveId))
                }
            }
            if (emp5 != null) {
                leaveDao.insert(
                    LeaveRequestEntity(
                        employeeId = emp5.id,
                        category = LeaveCategory.ANNUAL,
                        startDate = DateUtils.shiftDate(today, 2),
                        endDate = DateUtils.shiftDate(today, 4),
                        totalDays = 3,
                        reason = "Planned family travel",
                        status = LeaveStatus.PENDING,
                        isPaid = true
                    )
                )
            }
            if (emp6 != null) {
                leaveDao.insert(
                    LeaveRequestEntity(
                        employeeId = emp6.id,
                        category = LeaveCategory.UNPAID,
                        startDate = DateUtils.shiftDate(today, -6),
                        endDate = DateUtils.shiftDate(today, -6),
                        totalDays = 1,
                        reason = "Personal errand",
                        status = LeaveStatus.REJECTED,
                        isPaid = false,
                        reviewedAt = now - 432_000_000L
                    )
                )
            }

            // Generate current month payroll records from actual attendance
            val allAttendance = attendanceDao.getAllOnce()
            val allLeaves = leaveDao.getAllOnce()
            val monthAtt = allAttendance.filter { it.date.startsWith(currentMonth) }
            val payrolls = savedEmployees.mapIndexed { idx, emp ->
                val computed = PayrollCalculator.calculateForEmployee(
                    employee = emp,
                    yearMonth = currentMonth,
                    monthAttendance = monthAtt,
                    monthApprovedLeaves = allLeaves,
                    settings = settings
                )
                when (idx) {
                    0, 1 -> computed.copy(
                        allowances = 5000.0,
                        paidAmount = computed.netPayableSalary + 5000.0,
                        totalEarnings = computed.totalEarnings + 5000.0,
                        netPayableSalary = computed.netPayableSalary + 5000.0,
                        paymentStatus = PaymentStatus.PAID,
                        isApproved = true,
                        approvedAt = now - 3600_000L
                    )
                    2 -> computed.copy(
                        bonuses = 7500.0,
                        totalEarnings = computed.totalEarnings + 7500.0,
                        netPayableSalary = computed.netPayableSalary + 7500.0,
                        paidAmount = 50000.0,
                        paymentStatus = PaymentStatus.PARTIAL,
                        isApproved = true,
                        approvedAt = now - 3600_000L
                    )
                    else -> computed
                }
            }
            payrollDao.upsertAll(payrolls)

            activityLogDao.insertAll(
                listOf(
                    ActivityLogEntity(
                        title = "Workspace Initialized",
                        description = "8 employees and $currentMonth attendance loaded offline",
                        category = "SETTINGS",
                        timestamp = now - 7200_000L
                    ),
                    ActivityLogEntity(
                        title = "Leave Approved",
                        description = "Hira Fatima Siddiqui • 1 day Casual Leave synced to attendance",
                        category = "LEAVE",
                        timestamp = now - 3600_000L
                    ),
                    ActivityLogEntity(
                        title = "Payroll Approved",
                        description = "Ahsan Raza Khan & Sana Tariq Mahmood marked Paid",
                        category = "PAYROLL",
                        timestamp = now - 1800_000L
                    )
                )
            )
        }
        settingsRepo.markSeeded()
        settingsRepo.touchLastSaved()
    }

    // --- Employees ---

    suspend fun generateNextEmployeeCode(): String {
        val existing = employeeDao.getAllOnce()
        val maxNum = existing.mapNotNull { emp ->
            emp.employeeCode.removePrefix("ST-").toIntOrNull()
        }.maxOrNull() ?: 1000
        return "ST-${maxNum + 1}"
    }

    suspend fun saveEmployee(employee: EmployeeEntity): Long {
        val id = db.withTransaction {
            val now = System.currentTimeMillis()
            val savedId = if (employee.id == 0L) {
                employeeDao.insert(employee.copy(createdAt = now, updatedAt = now))
            } else {
                employeeDao.update(employee.copy(updatedAt = now))
                employee.id
            }
            val action = if (employee.id == 0L) "Employee Added" else "Employee Updated"
            activityLogDao.insert(
                ActivityLogEntity(
                    title = action,
                    description = "${employee.fullName} (${employee.employeeCode}) • ${employee.department}",
                    category = "EMPLOYEE"
                )
            )
            savedId
        }
        settingsRepo.touchLastSaved()
        return id
    }

    suspend fun toggleEmployeeActive(employee: EmployeeEntity) {
        val nextStatus = if (employee.status == EmploymentStatus.ACTIVE) {
            EmploymentStatus.INACTIVE
        } else {
            EmploymentStatus.ACTIVE
        }
        saveEmployee(employee.copy(status = nextStatus))
    }

    suspend fun deleteEmployee(employee: EmployeeEntity) {
        db.withTransaction {
            employeeDao.delete(employee)
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Employee Removed",
                    description = "${employee.fullName} (${employee.employeeCode}) deleted",
                    category = "EMPLOYEE"
                )
            )
        }
        settingsRepo.touchLastSaved()
    }

    // --- Attendance ---

    suspend fun markAttendance(
        employee: EmployeeEntity,
        date: String,
        status: AttendanceStatus,
        checkInTime: String?,
        checkOutTime: String?,
        overtimeHours: Double?,
        notes: String = ""
    ) {
        val settings = settingsRepo.getSettingsOnce()
        val computedLate = if (status == AttendanceStatus.LATE || status == AttendanceStatus.PRESENT) {
            DateUtils.calculateLateMinutes(checkInTime, settings.officeStartTime, settings.lateGraceMinutes)
        } else 0
        val finalStatus = if (status == AttendanceStatus.PRESENT && computedLate > 0) {
            AttendanceStatus.LATE
        } else status

        val computedOt = overtimeHours ?: if (finalStatus == AttendanceStatus.PRESENT || finalStatus == AttendanceStatus.LATE) {
            DateUtils.calculateOvertimeHours(checkOutTime, settings.officeEndTime)
        } else 0.0

        db.withTransaction {
            val existing = attendanceDao.getForEmployeeAndDate(employee.id, date)
            val entity = AttendanceEntity(
                id = existing?.id ?: 0,
                employeeId = employee.id,
                date = date,
                status = finalStatus,
                checkInTime = checkInTime,
                checkOutTime = checkOutTime,
                lateMinutes = computedLate,
                overtimeHours = computedOt,
                notes = notes,
                leaveRequestId = existing?.leaveRequestId,
                updatedAt = System.currentTimeMillis()
            )
            attendanceDao.upsert(entity)
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Attendance Marked",
                    description = "${employee.fullName} • $date • ${finalStatus.label}",
                    category = "ATTENDANCE"
                )
            )
        }
        settingsRepo.touchLastSaved()
    }

    suspend fun bulkMarkAttendance(
        employees: List<EmployeeEntity>,
        date: String,
        status: AttendanceStatus,
        overwriteExisting: Boolean = false
    ): Int {
        val settings = settingsRepo.getSettingsOnce()
        var markedCount = 0
        db.withTransaction {
            val defaultIn = if (status == AttendanceStatus.PRESENT || status == AttendanceStatus.HALF_DAY) settings.officeStartTime else null
            val defaultOut = when (status) {
                AttendanceStatus.PRESENT -> settings.officeEndTime
                AttendanceStatus.HALF_DAY -> "13:00"
                else -> null
            }
            employees.filter { it.status == EmploymentStatus.ACTIVE }.forEach { emp ->
                val existing = attendanceDao.getForEmployeeAndDate(emp.id, date)
                if (existing == null || overwriteExisting) {
                    attendanceDao.upsert(
                        AttendanceEntity(
                            id = existing?.id ?: 0,
                            employeeId = emp.id,
                            date = date,
                            status = status,
                            checkInTime = defaultIn,
                            checkOutTime = defaultOut,
                            lateMinutes = 0,
                            overtimeHours = 0.0,
                            notes = if (status == AttendanceStatus.HOLIDAY) "Bulk Holiday" else ""
                        )
                    )
                    markedCount++
                }
            }
            if (markedCount > 0) {
                activityLogDao.insert(
                    ActivityLogEntity(
                        title = "Bulk Attendance",
                        description = "Marked $markedCount employees as ${status.label} for $date",
                        category = "ATTENDANCE"
                    )
                )
            }
        }
        settingsRepo.touchLastSaved()
        return markedCount
    }

    // --- Leaves ---

    suspend fun submitLeaveRequest(
        employee: EmployeeEntity,
        category: LeaveCategory,
        startDate: String,
        endDate: String,
        reason: String
    ): Long {
        val days = DateUtils.datesBetween(startDate, endDate).size.coerceAtLeast(1)
        val isPaid = category != LeaveCategory.UNPAID
        val id = db.withTransaction {
            val leaveId = leaveDao.insert(
                LeaveRequestEntity(
                    employeeId = employee.id,
                    category = category,
                    startDate = startDate,
                    endDate = endDate,
                    totalDays = days,
                    reason = reason.trim(),
                    status = LeaveStatus.PENDING,
                    isPaid = isPaid
                )
            )
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Leave Requested",
                    description = "${employee.fullName} • ${category.label} ($days day${if (days > 1) "s" else ""})",
                    category = "LEAVE"
                )
            )
            leaveId
        }
        settingsRepo.touchLastSaved()
        return id
    }

    suspend fun approveLeave(leave: LeaveRequestEntity, employeeName: String) {
        db.withTransaction {
            val updated = leave.copy(
                status = LeaveStatus.APPROVED,
                reviewedAt = System.currentTimeMillis()
            )
            leaveDao.update(updated)

            // Sync approved leave dates into attendance automatically
            val dates = DateUtils.datesBetween(leave.startDate, leave.endDate)
            dates.forEach { date ->
                val existing = attendanceDao.getForEmployeeAndDate(leave.employeeId, date)
                attendanceDao.upsert(
                    AttendanceEntity(
                        id = existing?.id ?: 0,
                        employeeId = leave.employeeId,
                        date = date,
                        status = AttendanceStatus.ON_LEAVE,
                        checkInTime = null,
                        checkOutTime = null,
                        lateMinutes = 0,
                        overtimeHours = 0.0,
                        notes = "${leave.category.label}: ${leave.reason}",
                        leaveRequestId = leave.id,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Leave Approved",
                    description = "$employeeName • ${leave.category.label} synced to ${dates.size} attendance day(s)",
                    category = "LEAVE"
                )
            )
        }
        settingsRepo.touchLastSaved()
    }

    suspend fun rejectLeave(leave: LeaveRequestEntity, employeeName: String) {
        db.withTransaction {
            leaveDao.update(
                leave.copy(
                    status = LeaveStatus.REJECTED,
                    reviewedAt = System.currentTimeMillis()
                )
            )
            attendanceDao.deleteByLeaveRequestId(leave.id)
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Leave Rejected",
                    description = "$employeeName • ${leave.category.label} (${leave.startDate})",
                    category = "LEAVE"
                )
            )
        }
        settingsRepo.touchLastSaved()
    }

    suspend fun deleteLeave(leave: LeaveRequestEntity) {
        db.withTransaction {
            attendanceDao.deleteByLeaveRequestId(leave.id)
            leaveDao.deleteById(leave.id)
        }
        settingsRepo.touchLastSaved()
    }

    // --- Payroll ---

    suspend fun generatePayrollForMonth(yearMonth: String) {
        val settings = settingsRepo.getSettingsOnce()
        val employees = employeeDao.getAllOnce().filter { it.status == EmploymentStatus.ACTIVE }
        val monthAttendance = attendanceDao.getAllOnce().filter { it.date.startsWith(yearMonth) }
        val allLeaves = leaveDao.getAllOnce()

        db.withTransaction {
            val records = employees.map { emp ->
                val existing = payrollDao.getForEmployeeAndMonth(emp.id, yearMonth)
                PayrollCalculator.calculateForEmployee(
                    employee = emp,
                    yearMonth = yearMonth,
                    monthAttendance = monthAttendance,
                    monthApprovedLeaves = allLeaves,
                    settings = settings,
                    existingRecord = existing
                )
            }
            payrollDao.upsertAll(records)
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Payroll Calculated",
                    description = "Generated ${records.size} salary records for ${DateUtils.formatMonthTitle(yearMonth)}",
                    category = "PAYROLL"
                )
            )
        }
        settingsRepo.touchLastSaved()
    }

    suspend fun updatePayrollAdjustments(
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
        val settings = settingsRepo.getSettingsOnce()
        val monthAtt = attendanceDao.getAllOnce().filter { it.date.startsWith(record.yearMonth) }
        val allLeaves = leaveDao.getAllOnce()

        val seededExisting = record.copy(
            bonuses = bonuses,
            allowances = allowances,
            advanceSalary = advanceSalary,
            otherDeductions = otherDeductions,
            paidAmount = paidAmount,
            isApproved = isApproved,
            approvedAt = if (isApproved && record.approvedAt == null) System.currentTimeMillis() else record.approvedAt,
            notes = notes
        )

        val recalculated = PayrollCalculator.calculateForEmployee(
            employee = employee,
            yearMonth = record.yearMonth,
            monthAttendance = monthAtt,
            monthApprovedLeaves = allLeaves,
            settings = settings,
            existingRecord = seededExisting
        )

        db.withTransaction {
            payrollDao.upsert(recalculated)
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Payroll Updated",
                    description = "${employee.fullName} • ${recalculated.yearMonth} • ${recalculated.paymentStatus.label}",
                    category = "PAYROLL"
                )
            )
        }
        settingsRepo.touchLastSaved()
    }

    suspend fun approveAllPayrollForMonth(yearMonth: String) {
        val all = payrollDao.getAllOnce().filter { it.yearMonth == yearMonth }
        val now = System.currentTimeMillis()
        db.withTransaction {
            payrollDao.upsertAll(all.map { it.copy(isApproved = true, approvedAt = it.approvedAt ?: now, updatedAt = now) })
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Payroll Approved",
                    description = "Approved ${all.size} payroll slips for ${DateUtils.formatMonthTitle(yearMonth)}",
                    category = "PAYROLL"
                )
            )
        }
        settingsRepo.touchLastSaved()
    }

    // --- Settings ---

    suspend fun saveSettings(settings: CompanySettings) {
        settingsRepo.updateSettings(settings)
        db.withTransaction {
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Settings Updated",
                    description = "Saved company configuration (${settings.companyName}, ${settings.currency})",
                    category = "SETTINGS"
                )
            )
        }
    }

    // --- Backup & Restore ---

    suspend fun exportBackupJson(): String {
        val now = System.currentTimeMillis()
        val settings = settingsRepo.getSettingsOnce().copy(lastBackupTimestamp = now)
        val backup = StaffTrackBackup(
            schemaVersion = 1,
            exportedAt = now,
            settings = settings,
            employees = employeeDao.getAllOnce(),
            attendance = attendanceDao.getAllOnce(),
            leaveRequests = leaveDao.getAllOnce(),
            payrollRecords = payrollDao.getAllOnce(),
            activityLogs = activityLogDao.getAllOnce()
        )
        settingsRepo.updateLastBackupTimestamp(now)
        db.withTransaction {
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "JSON Backup Exported",
                    description = "${backup.employees.size} employees, ${backup.attendance.size} attendance logs",
                    category = "BACKUP"
                )
            )
        }
        return backupAdapter.toJson(backup)
    }

    fun validateBackupJson(rawJson: String): BackupValidationResult {
        if (rawJson.isBlank()) {
            return BackupValidationResult(false, errorMessage = "Selected file is empty.")
        }
        return try {
            val parsed = backupAdapter.fromJson(rawJson)
                ?: return BackupValidationResult(false, errorMessage = "Invalid JSON structure.")
            if (parsed.schemaVersion < 1 || parsed.appName != "StaffTrack") {
                return BackupValidationResult(false, errorMessage = "File is not a valid StaffTrack backup.")
            }
            val empIds = parsed.employees.map { it.id }.toSet()
            if (parsed.employees.any { it.fullName.isBlank() || it.employeeCode.isBlank() }) {
                return BackupValidationResult(false, errorMessage = "Backup contains invalid employee records.")
            }
            if (parsed.attendance.any { it.employeeId !in empIds }) {
                return BackupValidationResult(false, errorMessage = "Backup attendance references unknown employees.")
            }
            if (parsed.leaveRequests.any { it.employeeId !in empIds }) {
                return BackupValidationResult(false, errorMessage = "Backup leaves reference unknown employees.")
            }
            if (parsed.payrollRecords.any { it.employeeId !in empIds }) {
                return BackupValidationResult(false, errorMessage = "Backup payroll references unknown employees.")
            }
            BackupValidationResult(true, backup = parsed)
        } catch (e: Exception) {
            BackupValidationResult(false, errorMessage = "Failed to parse backup: ${e.message ?: "Malformed JSON"}")
        }
    }

    suspend fun restoreFromBackup(backup: StaffTrackBackup) {
        db.withTransaction {
            payrollDao.deleteAll()
            leaveDao.deleteAll()
            attendanceDao.deleteAll()
            employeeDao.deleteAll()
            activityLogDao.deleteAll()

            employeeDao.insertAll(backup.employees)
            attendanceDao.upsertAll(backup.attendance)
            leaveDao.insertAll(backup.leaveRequests)
            payrollDao.upsertAll(backup.payrollRecords)
            activityLogDao.insertAll(backup.activityLogs)
            activityLogDao.insert(
                ActivityLogEntity(
                    title = "Backup Restored",
                    description = "Restored ${backup.employees.size} employees & ${backup.attendance.size} attendance records",
                    category = "BACKUP"
                )
            )
        }
        settingsRepo.markSeeded()
        settingsRepo.updateSettings(backup.settings.copy(lastBackupTimestamp = backup.exportedAt))
    }
}
