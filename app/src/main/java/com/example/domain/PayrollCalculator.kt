package com.example.domain

import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.LeaveRequestEntity
import com.example.data.local.entity.PayrollRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.CompanySettings
import com.example.data.model.LeaveCategory
import com.example.data.model.LeaveStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.PayrollBasis
import com.example.data.model.SalaryType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object DateUtils {
    private val isoDateFmt get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val isoMonthFmt get() = SimpleDateFormat("yyyy-MM", Locale.US)
    private val monthTitleFmt get() = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val dateTimeFmt get() = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)

    fun todayIso(): String = isoDateFmt.format(Date())

    fun currentYearMonth(): String = isoMonthFmt.format(Date())

    fun formatTimestamp(ts: Long): String = dateTimeFmt.format(Date(ts))

    fun formatDisplayDate(isoDate: String, pattern: String): String {
        return try {
            val parsed = isoDateFmt.parse(isoDate) ?: return isoDate
            SimpleDateFormat(pattern, Locale.US).format(parsed)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatMonthTitle(yearMonth: String): String {
        return try {
            val parsed = isoMonthFmt.parse(yearMonth) ?: return yearMonth
            monthTitleFmt.format(parsed)
        } catch (_: Exception) {
            yearMonth
        }
    }

    fun shiftMonth(yearMonth: String, deltaMonths: Int): String {
        return try {
            val cal = Calendar.getInstance(Locale.US)
            cal.time = isoMonthFmt.parse(yearMonth) ?: Date()
            cal.add(Calendar.MONTH, deltaMonths)
            isoMonthFmt.format(cal.time)
        } catch (_: Exception) {
            currentYearMonth()
        }
    }

    fun shiftDate(isoDate: String, deltaDays: Int): String {
        return try {
            val cal = Calendar.getInstance(Locale.US)
            cal.time = isoDateFmt.parse(isoDate) ?: Date()
            cal.add(Calendar.DAY_OF_MONTH, deltaDays)
            isoDateFmt.format(cal.time)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun daysInMonth(yearMonth: String): Int {
        return try {
            val cal = Calendar.getInstance(Locale.US)
            cal.time = isoMonthFmt.parse(yearMonth) ?: Date()
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        } catch (_: Exception) {
            30
        }
    }

    fun datesInMonth(yearMonth: String): List<String> {
        val count = daysInMonth(yearMonth)
        return (1..count).map { day -> "$yearMonth-${day.toString().padStart(2, '0')}" }
    }

    fun firstDayOffsetMondayStart(yearMonth: String): Int {
        return try {
            val cal = Calendar.getInstance(Locale.US)
            cal.time = isoDateFmt.parse("$yearMonth-01") ?: Date()
            // Calendar.SUNDAY=1 .. SATURDAY=7 -> convert to Monday=0 .. Sunday=6
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            (dow + 5) % 7
        } catch (_: Exception) {
            0
        }
    }

    fun dayOfWeekName(isoDate: String): String {
        return try {
            val cal = Calendar.getInstance(Locale.US)
            cal.time = isoDateFmt.parse(isoDate) ?: return "MONDAY"
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "MONDAY"
                Calendar.TUESDAY -> "TUESDAY"
                Calendar.WEDNESDAY -> "WEDNESDAY"
                Calendar.THURSDAY -> "THURSDAY"
                Calendar.FRIDAY -> "FRIDAY"
                Calendar.SATURDAY -> "SATURDAY"
                else -> "SUNDAY"
            }
        } catch (_: Exception) {
            "MONDAY"
        }
    }

    fun datesBetween(startIso: String, endIso: String): List<String> {
        return try {
            val start = isoDateFmt.parse(startIso) ?: return emptyList()
            val end = isoDateFmt.parse(endIso) ?: return emptyList()
            if (end.before(start)) return emptyList()
            val cal = Calendar.getInstance(Locale.US).apply { time = start }
            val result = mutableListOf<String>()
            var guard = 0
            while (!cal.time.after(end) && guard < 366) {
                result.add(isoDateFmt.format(cal.time))
                cal.add(Calendar.DAY_OF_MONTH, 1)
                guard++
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun parseMinutesFromMidnight(hhmm: String?): Int? {
        if (hhmm.isNullOrBlank()) return null
        val parts = hhmm.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    fun calculateLateMinutes(checkInHHmm: String?, officeStartHHmm: String, graceMinutes: Int): Int {
        val checkIn = parseMinutesFromMidnight(checkInHHmm) ?: return 0
        val start = parseMinutesFromMidnight(officeStartHHmm) ?: return 0
        val diff = checkIn - start
        return if (diff > graceMinutes) diff else 0
    }

    fun calculateOvertimeHours(checkOutHHmm: String?, officeEndHHmm: String): Double {
        val checkOut = parseMinutesFromMidnight(checkOutHHmm) ?: return 0.0
        val end = parseMinutesFromMidnight(officeEndHHmm) ?: return 0.0
        val diff = checkOut - end
        return if (diff > 15) ((diff / 60.0) * 100.0).roundToInt() / 100.0 else 0.0
    }

    fun calculateWorkingHours(checkInHHmm: String?, checkOutHHmm: String?): Double {
        val start = parseMinutesFromMidnight(checkInHHmm) ?: return 0.0
        val end = parseMinutesFromMidnight(checkOutHHmm) ?: return 0.0
        val diff = (end - start).coerceAtLeast(0)
        return ((diff / 60.0) * 100.0).roundToInt() / 100.0
    }

    fun formatCurrency(amount: Double, currency: String): String {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return "$currency ${nf.format(amount.roundToInt())}"
    }

    fun formatCurrencyDetailed(amount: Double, currency: String): String {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$currency ${nf.format(amount)}"
    }
}

data class LeaveBalanceSummary(
    val category: LeaveCategory,
    val allowance: Int,
    val used: Int,
    val remaining: Int
)

object PayrollCalculator {

    fun workingDaysInMonth(yearMonth: String, settings: CompanySettings, employeeWeeklyOff: String): Int {
        val offDays = employeeWeeklyOff.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }.toSet()
        val companyWorking = settings.workingDaysList.map { it.uppercase() }.toSet()
        return DateUtils.datesInMonth(yearMonth).count { iso ->
            val dow = DateUtils.dayOfWeekName(iso)
            dow !in offDays && (companyWorking.isEmpty() || dow in companyWorking)
        }.coerceAtLeast(1)
    }

    fun calculateForEmployee(
        employee: EmployeeEntity,
        yearMonth: String,
        monthAttendance: List<AttendanceEntity>,
        monthApprovedLeaves: List<LeaveRequestEntity>,
        settings: CompanySettings,
        existingRecord: PayrollRecordEntity? = null
    ): PayrollRecordEntity {
        val workingDays = workingDaysInMonth(yearMonth, settings, employee.weeklyHoliday)
        val calendarDays = DateUtils.daysInMonth(yearMonth)

        val empAtt = monthAttendance.filter { it.employeeId == employee.id }
        val presentDays = empAtt.count { it.status == AttendanceStatus.PRESENT }
        val absentDays = empAtt.count { it.status == AttendanceStatus.ABSENT }
        val halfDays = empAtt.count { it.status == AttendanceStatus.HALF_DAY }
        val lateDays = empAtt.count { it.status == AttendanceStatus.LATE }
        val holidayDays = empAtt.count { it.status == AttendanceStatus.HOLIDAY }

        // Count paid vs unpaid leave days intersecting this month
        val monthDatesSet = DateUtils.datesInMonth(yearMonth).toSet()
        var paidLeaveDays = 0
        var unpaidLeaveDays = 0
        val leaveDatesAccounted = mutableSetOf<String>()

        monthApprovedLeaves
            .filter { it.employeeId == employee.id && it.status == LeaveStatus.APPROVED }
            .forEach { leave ->
                val days = DateUtils.datesBetween(leave.startDate, leave.endDate)
                    .filter { it in monthDatesSet }
                days.forEach { d ->
                    if (leaveDatesAccounted.add(d)) {
                        if (leave.isPaid && leave.category != LeaveCategory.UNPAID) paidLeaveDays++
                        else unpaidLeaveDays++
                    }
                }
            }

        // Fallback if ON_LEAVE was marked directly without a leave request
        empAtt.filter { it.status == AttendanceStatus.ON_LEAVE && it.date !in leaveDatesAccounted }
            .forEach { _ -> paidLeaveDays++ }

        val overtimeHours = ((empAtt.sumOf { it.overtimeHours }) * 100.0).roundToInt() / 100.0

        val basisDays = when (settings.payrollBasis) {
            PayrollBasis.WORKING_DAYS -> workingDays
            PayrollBasis.FIXED_30_DAYS -> 30
            PayrollBasis.CALENDAR_DAYS -> calendarDays
        }.coerceAtLeast(1)

        val dailyRate = when (employee.salaryType) {
            SalaryType.MONTHLY -> employee.basicSalary / basisDays
            SalaryType.DAILY -> employee.basicSalary
        }

        val hourlyRate = dailyRate / employee.workingHoursPerDay.coerceAtLeast(1.0)
        val overtimeRate = if (settings.customOvertimeHourlyRate > 0.0) {
            settings.customOvertimeHourlyRate
        } else {
            hourlyRate * settings.overtimeMultiplier
        }
        val overtimePay = round2(overtimeHours * overtimeRate)

        val bonuses = existingRecord?.bonuses ?: 0.0
        val allowances = existingRecord?.allowances ?: 0.0
        val advanceSalary = existingRecord?.advanceSalary ?: 0.0
        val otherDeductions = existingRecord?.otherDeductions ?: 0.0

        val latePenaltyHalfDays = if (settings.latePenaltyEnabled && settings.lateCountForHalfDayDeduction > 0) {
            lateDays / settings.lateCountForHalfDayDeduction
        } else {
            0
        }
        val lateDeduction = round2(latePenaltyHalfDays * (dailyRate * 0.5))

        val grossSalary: Double
        val absenceDeduction: Double
        val halfDayDeduction: Double
        val unpaidLeaveDeduction: Double

        if (employee.salaryType == SalaryType.MONTHLY) {
            grossSalary = round2(employee.basicSalary)
            absenceDeduction = if (settings.deductAbsentDays) round2(absentDays * dailyRate) else 0.0
            halfDayDeduction = round2(halfDays * (dailyRate * 0.5))
            unpaidLeaveDeduction = round2(unpaidLeaveDays * dailyRate)
        } else {
            val paidDayUnits = presentDays + lateDays + paidLeaveDays + holidayDays + (halfDays * 0.5)
            grossSalary = round2(paidDayUnits * dailyRate)
            absenceDeduction = 0.0
            halfDayDeduction = 0.0
            unpaidLeaveDeduction = 0.0
        }

        val totalEarnings = round2(grossSalary + overtimePay + bonuses + allowances)
        val totalDeductions = round2(
            absenceDeduction + halfDayDeduction + unpaidLeaveDeduction + lateDeduction + advanceSalary + otherDeductions
        )
        val netPayable = round2((totalEarnings - totalDeductions).coerceAtLeast(0.0))

        val paidAmount = (existingRecord?.paidAmount ?: 0.0).coerceAtMost(netPayable)
        val paymentStatus = when {
            netPayable <= 0.0 && existingRecord?.paymentStatus == PaymentStatus.PAID -> PaymentStatus.PAID
            paidAmount <= 0.0 -> PaymentStatus.UNPAID
            paidAmount >= netPayable - 0.01 -> PaymentStatus.PAID
            else -> PaymentStatus.PARTIAL
        }

        return PayrollRecordEntity(
            id = existingRecord?.id ?: 0,
            employeeId = employee.id,
            yearMonth = yearMonth,
            salaryType = employee.salaryType,
            basicSalary = employee.basicSalary,
            workingDaysInMonth = workingDays,
            presentDays = presentDays,
            absentDays = absentDays,
            halfDays = halfDays,
            lateDays = lateDays,
            paidLeaveDays = paidLeaveDays,
            unpaidLeaveDays = unpaidLeaveDays,
            holidayDays = holidayDays,
            overtimeHours = overtimeHours,
            overtimeRatePerHour = round2(overtimeRate),
            overtimePay = overtimePay,
            bonuses = bonuses,
            allowances = allowances,
            advanceSalary = advanceSalary,
            otherDeductions = otherDeductions,
            absenceDeduction = absenceDeduction,
            halfDayDeduction = halfDayDeduction,
            unpaidLeaveDeduction = unpaidLeaveDeduction,
            lateDeduction = lateDeduction,
            grossSalary = grossSalary,
            totalEarnings = totalEarnings,
            totalDeductions = totalDeductions,
            netPayableSalary = netPayable,
            paidAmount = round2(paidAmount),
            paymentStatus = paymentStatus,
            isApproved = existingRecord?.isApproved ?: false,
            approvedAt = existingRecord?.approvedAt,
            notes = existingRecord?.notes ?: "",
            updatedAt = System.currentTimeMillis()
        )
    }

    fun calculateLeaveBalances(
        employee: EmployeeEntity,
        allLeaves: List<LeaveRequestEntity>,
        yearPrefix: String = DateUtils.currentYearMonth().substring(0, 4)
    ): List<LeaveBalanceSummary> {
        val approvedForEmp = allLeaves.filter {
            it.employeeId == employee.id &&
                it.status == LeaveStatus.APPROVED &&
                it.startDate.startsWith(yearPrefix)
        }
        return listOf(
            LeaveBalanceSummary(
                category = LeaveCategory.ANNUAL,
                allowance = employee.annualLeaveAllowance,
                used = approvedForEmp.filter { it.category == LeaveCategory.ANNUAL }.sumOf { it.totalDays },
                remaining = (employee.annualLeaveAllowance - approvedForEmp.filter { it.category == LeaveCategory.ANNUAL }.sumOf { it.totalDays }).coerceAtLeast(0)
            ),
            LeaveBalanceSummary(
                category = LeaveCategory.SICK,
                allowance = employee.sickLeaveAllowance,
                used = approvedForEmp.filter { it.category == LeaveCategory.SICK }.sumOf { it.totalDays },
                remaining = (employee.sickLeaveAllowance - approvedForEmp.filter { it.category == LeaveCategory.SICK }.sumOf { it.totalDays }).coerceAtLeast(0)
            ),
            LeaveBalanceSummary(
                category = LeaveCategory.CASUAL,
                allowance = employee.casualLeaveAllowance,
                used = approvedForEmp.filter { it.category == LeaveCategory.CASUAL }.sumOf { it.totalDays },
                remaining = (employee.casualLeaveAllowance - approvedForEmp.filter { it.category == LeaveCategory.CASUAL }.sumOf { it.totalDays }).coerceAtLeast(0)
            )
        )
    }

    private fun round2(v: Double): Double = (v * 100.0).roundToInt() / 100.0
}
