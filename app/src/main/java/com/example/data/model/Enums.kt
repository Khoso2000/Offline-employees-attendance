package com.example.data.model

import com.squareup.moshi.JsonClass

enum class UserRole(val label: String) {
    ADMIN("HR / Admin"),
    EMPLOYEE("Employee")
}

enum class SalaryType(val label: String) {
    MONTHLY("Monthly Fixed"),
    DAILY("Daily Wage")
}

enum class EmploymentStatus(val label: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    TERMINATED("Terminated")
}

enum class AttendanceStatus(val label: String, val shortCode: String) {
    PRESENT("Present", "P"),
    ABSENT("Absent", "A"),
    HALF_DAY("Half-Day", "H"),
    LATE("Late", "L"),
    ON_LEAVE("On Leave", "LV"),
    HOLIDAY("Holiday", "HL")
}

enum class LeaveCategory(val label: String, val defaultPaid: Boolean) {
    SICK("Sick Leave", true),
    CASUAL("Casual Leave", true),
    ANNUAL("Annual Leave", true),
    PAID("Paid Leave", true),
    UNPAID("Unpaid Leave", false),
    EMERGENCY("Emergency Leave", true)
}

enum class LeaveStatus(val label: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

enum class PaymentStatus(val label: String) {
    UNPAID("Unpaid"),
    PARTIAL("Partial"),
    PAID("Paid")
}

enum class PayrollBasis(val label: String, val description: String) {
    WORKING_DAYS("Working Days", "Basic salary divided by actual working days in the month"),
    FIXED_30_DAYS("Fixed 30 Days", "Basic salary divided by 30 days regardless of month length"),
    CALENDAR_DAYS("Calendar Days", "Basic salary divided by total days in the month")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

@JsonClass(generateAdapter = true)
data class CompanySettings(
    val companyName: String = "Apex Dynamics Pvt Ltd",
    val companyLogoUri: String? = null,
    val currency: String = "PKR",
    val dateFormat: String = "dd MMM yyyy",
    val workingDaysList: List<String> = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"),
    val officeStartTime: String = "09:00",
    val officeEndTime: String = "17:00",
    val lateGraceMinutes: Int = 15,
    val halfDayHoursThreshold: Double = 4.0,
    val overtimeMultiplier: Double = 1.5,
    val customOvertimeHourlyRate: Double = 0.0,
    val payrollBasis: PayrollBasis = PayrollBasis.WORKING_DAYS,
    val latePenaltyEnabled: Boolean = true,
    val lateCountForHalfDayDeduction: Int = 3,
    val deductAbsentDays: Boolean = true,
    val defaultAnnualLeave: Int = 14,
    val defaultSickLeave: Int = 10,
    val defaultCasualLeave: Int = 8,
    val autoMarkHolidayOnWeeklyOff: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastBackupTimestamp: Long? = null,
    val lastSavedTimestamp: Long = System.currentTimeMillis()
)

enum class ReportType(val label: String, val description: String) {
    MONTHLY_ATTENDANCE("Monthly Attendance", "Summary of present, absent, half-day, late & leave counts"),
    EMPLOYEE_HISTORY("Employee Attendance History", "Detailed day-by-day attendance log for an employee"),
    ABSENCE_REPORT("Absence Report", "All absent & unpaid leave occurrences in date range"),
    LATE_ARRIVAL("Late Arrival Report", "Late check-ins with arrival time and minutes delayed"),
    LEAVE_REPORT("Leave Report", "Leave applications, categories, days & approval status"),
    OVERTIME_REPORT("Overtime Report", "Logged overtime hours and estimated overtime pay"),
    MONTHLY_PAYROLL("Monthly Payroll Report", "Complete payroll register with earnings & deductions"),
    SALARY_SLIP("Individual Salary Slip", "Detailed itemized payslip for a selected employee"),
    TOTAL_SALARY_EXPENSE("Total Salary Expense", "Departmental & organization-wide payroll cost breakdown")
}
