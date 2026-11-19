package com.example

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
import com.example.domain.DateUtils
import com.example.domain.PayrollCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class PayrollCalculatorTest {

    @Test
    fun monthlySalary_calculatesDeductionsAndOvertimeAccurately() {
        val emp = EmployeeEntity(
            id = 1L,
            employeeCode = "ST-1001",
            fullName = "Test Employee",
            phone = "123",
            email = "",
            address = "",
            department = "Engineering",
            designation = "Dev",
            joiningDate = "2024-01-01",
            salaryType = SalaryType.MONTHLY,
            basicSalary = 90000.0,
            workingHoursPerDay = 8.0,
            weeklyHoliday = "SUNDAY"
        )
        val settings = CompanySettings(
            payrollBasis = PayrollBasis.FIXED_30_DAYS, // dailyRate = 3000, hourly = 375
            overtimeMultiplier = 1.5, // OT rate = 562.5/hr
            deductAbsentDays = true,
            latePenaltyEnabled = true,
            lateCountForHalfDayDeduction = 3
        )
        val attendance = listOf(
            AttendanceEntity(employeeId = 1L, date = "2026-11-02", status = AttendanceStatus.PRESENT, overtimeHours = 2.0),
            AttendanceEntity(employeeId = 1L, date = "2026-11-03", status = AttendanceStatus.ABSENT),
            AttendanceEntity(employeeId = 1L, date = "2026-11-04", status = AttendanceStatus.HALF_DAY),
            AttendanceEntity(employeeId = 1L, date = "2026-11-05", status = AttendanceStatus.LATE),
            AttendanceEntity(employeeId = 1L, date = "2026-11-06", status = AttendanceStatus.LATE),
            AttendanceEntity(employeeId = 1L, date = "2026-11-07", status = AttendanceStatus.LATE)
        )
        val unpaidLeave = listOf(
            LeaveRequestEntity(
                id = 10L,
                employeeId = 1L,
                category = LeaveCategory.UNPAID,
                startDate = "2026-11-09",
                endDate = "2026-11-09",
                totalDays = 1,
                reason = "Personal",
                status = LeaveStatus.APPROVED,
                isPaid = false
            )
        )
        val existing = PayrollRecordEntity(
            employeeId = 1L,
            yearMonth = "2026-11",
            salaryType = SalaryType.MONTHLY,
            basicSalary = 90000.0,
            workingDaysInMonth = 25,
            presentDays = 0,
            absentDays = 0,
            halfDays = 0,
            lateDays = 0,
            paidLeaveDays = 0,
            unpaidLeaveDays = 0,
            holidayDays = 0,
            overtimeHours = 0.0,
            overtimeRatePerHour = 0.0,
            overtimePay = 0.0,
            bonuses = 5000.0,
            allowances = 2000.0,
            advanceSalary = 10000.0,
            otherDeductions = 500.0,
            grossSalary = 0.0,
            totalEarnings = 0.0,
            totalDeductions = 0.0,
            netPayableSalary = 0.0,
            paidAmount = 50000.0
        )

        val result = PayrollCalculator.calculateForEmployee(
            employee = emp,
            yearMonth = "2026-11",
            monthAttendance = attendance,
            monthApprovedLeaves = unpaidLeave,
            settings = settings,
            existingRecord = existing
        )

        // Gross = 90,000; OT = 2 * 562.5 = 1125; Bonus+Allowance = 7000 -> Total Earnings = 98,125
        assertEquals(90000.0, result.grossSalary, 0.01)
        assertEquals(1125.0, result.overtimePay, 0.01)
        assertEquals(98125.0, result.totalEarnings, 0.01)

        // Deductions: 1 absent (3000) + 1 half (1500) + 1 unpaid leave (3000) + 3 lates -> 1 half (1500) + advance (10000) + other (500) = 19,500
        assertEquals(3000.0, result.absenceDeduction, 0.01)
        assertEquals(1500.0, result.halfDayDeduction, 0.01)
        assertEquals(3000.0, result.unpaidLeaveDeduction, 0.01)
        assertEquals(1500.0, result.lateDeduction, 0.01)
        assertEquals(19500.0, result.totalDeductions, 0.01)

        // Net = 98125 - 19500 = 78625; Paid = 50000 -> PARTIAL
        assertEquals(78625.0, result.netPayableSalary, 0.01)
        assertEquals(PaymentStatus.PARTIAL, result.paymentStatus)
    }

    @Test
    fun dailyWage_paysWorkedAndHalfDaysAccurately() {
        val emp = EmployeeEntity(
            id = 2L,
            employeeCode = "ST-1002",
            fullName = "Daily Worker",
            phone = "123",
            email = "",
            address = "",
            department = "Operations",
            designation = "Technician",
            joiningDate = "2024-01-01",
            salaryType = SalaryType.DAILY,
            basicSalary = 3000.0,
            workingHoursPerDay = 8.0,
            weeklyHoliday = "SUNDAY"
        )
        val settings = CompanySettings(customOvertimeHourlyRate = 600.0, latePenaltyEnabled = false)
        val attendance = listOf(
            AttendanceEntity(employeeId = 2L, date = "2026-11-02", status = AttendanceStatus.PRESENT, overtimeHours = 1.5),
            AttendanceEntity(employeeId = 2L, date = "2026-11-03", status = AttendanceStatus.PRESENT),
            AttendanceEntity(employeeId = 2L, date = "2026-11-04", status = AttendanceStatus.HALF_DAY),
            AttendanceEntity(employeeId = 2L, date = "2026-11-05", status = AttendanceStatus.ABSENT)
        )

        val result = PayrollCalculator.calculateForEmployee(
            employee = emp,
            yearMonth = "2026-11",
            monthAttendance = attendance,
            monthApprovedLeaves = emptyList(),
            settings = settings
        )

        // 2.5 paid days * 3000 = 7500 gross + 1.5 * 600 = 900 OT = 8400 net
        assertEquals(7500.0, result.grossSalary, 0.01)
        assertEquals(900.0, result.overtimePay, 0.01)
        assertEquals(8400.0, result.netPayableSalary, 0.01)
    }

    @Test
    fun dateUtils_lateMinutesAndDatesBetweenWorkProperly() {
        assertEquals(20, DateUtils.calculateLateMinutes("09:20", "09:00", 15))
        assertEquals(0, DateUtils.calculateLateMinutes("09:15", "09:00", 15))
        assertEquals(3, DateUtils.datesBetween("2026-11-10", "2026-11-12").size)
    }
}
