package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.AttendanceStatus
import com.example.data.model.EmploymentStatus
import com.example.data.model.LeaveCategory
import com.example.data.model.LeaveStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.SalaryType
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "employees",
    indices = [Index(value = ["employeeCode"], unique = true)]
)
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeCode: String,
    val fullName: String,
    val photoUri: String? = null,
    val phone: String,
    val email: String,
    val address: String,
    val department: String,
    val designation: String,
    val joiningDate: String, // yyyy-MM-dd
    val salaryType: SalaryType,
    val basicSalary: Double,
    val workingHoursPerDay: Double = 8.0,
    val weeklyHoliday: String = "SUNDAY",
    val status: EmploymentStatus = EmploymentStatus.ACTIVE,
    val annualLeaveAllowance: Int = 14,
    val sickLeaveAllowance: Int = 10,
    val casualLeaveAllowance: Int = 8,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["employeeId", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val date: String, // yyyy-MM-dd
    val status: AttendanceStatus,
    val checkInTime: String? = null, // HH:mm
    val checkOutTime: String? = null, // HH:mm
    val lateMinutes: Int = 0,
    val overtimeHours: Double = 0.0,
    val notes: String = "",
    val leaveRequestId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "leave_requests",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["employeeId"])]
)
data class LeaveRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val category: LeaveCategory,
    val startDate: String, // yyyy-MM-dd
    val endDate: String, // yyyy-MM-dd
    val totalDays: Int,
    val reason: String,
    val status: LeaveStatus = LeaveStatus.PENDING,
    val isPaid: Boolean = true,
    val reviewedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "payroll_records",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["employeeId", "yearMonth"], unique = true),
        Index(value = ["yearMonth"])
    ]
)
data class PayrollRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val yearMonth: String, // yyyy-MM
    val salaryType: SalaryType,
    val basicSalary: Double,
    val workingDaysInMonth: Int,
    val presentDays: Int,
    val absentDays: Int,
    val halfDays: Int,
    val lateDays: Int,
    val paidLeaveDays: Int,
    val unpaidLeaveDays: Int,
    val holidayDays: Int,
    val overtimeHours: Double,
    val overtimeRatePerHour: Double,
    val overtimePay: Double,
    val bonuses: Double = 0.0,
    val allowances: Double = 0.0,
    val advanceSalary: Double = 0.0,
    val otherDeductions: Double = 0.0,
    val absenceDeduction: Double = 0.0,
    val halfDayDeduction: Double = 0.0,
    val unpaidLeaveDeduction: Double = 0.0,
    val lateDeduction: Double = 0.0,
    val grossSalary: Double,
    val totalEarnings: Double,
    val totalDeductions: Double,
    val netPayableSalary: Double,
    val paidAmount: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val isApproved: Boolean = false,
    val approvedAt: Long? = null,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
