package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.local.dao.ActivityLogDao
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.EmployeeDao
import com.example.data.local.dao.LeaveDao
import com.example.data.local.dao.PayrollDao
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.LeaveRequestEntity
import com.example.data.local.entity.PayrollRecordEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.EmploymentStatus
import com.example.data.model.LeaveCategory
import com.example.data.model.LeaveStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.SalaryType

class StaffTrackConverters {
    @TypeConverter fun fromSalaryType(value: SalaryType): String = value.name
    @TypeConverter fun toSalaryType(value: String): SalaryType = SalaryType.valueOf(value)

    @TypeConverter fun fromEmploymentStatus(value: EmploymentStatus): String = value.name
    @TypeConverter fun toEmploymentStatus(value: String): EmploymentStatus = EmploymentStatus.valueOf(value)

    @TypeConverter fun fromAttendanceStatus(value: AttendanceStatus): String = value.name
    @TypeConverter fun toAttendanceStatus(value: String): AttendanceStatus = AttendanceStatus.valueOf(value)

    @TypeConverter fun fromLeaveCategory(value: LeaveCategory): String = value.name
    @TypeConverter fun toLeaveCategory(value: String): LeaveCategory = LeaveCategory.valueOf(value)

    @TypeConverter fun fromLeaveStatus(value: LeaveStatus): String = value.name
    @TypeConverter fun toLeaveStatus(value: String): LeaveStatus = LeaveStatus.valueOf(value)

    @TypeConverter fun fromPaymentStatus(value: PaymentStatus): String = value.name
    @TypeConverter fun toPaymentStatus(value: String): PaymentStatus = PaymentStatus.valueOf(value)
}

@Database(
    entities = [
        EmployeeEntity::class,
        AttendanceEntity::class,
        LeaveRequestEntity::class,
        PayrollRecordEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StaffTrackConverters::class)
abstract class StaffTrackDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveDao(): LeaveDao
    abstract fun payrollDao(): PayrollDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: StaffTrackDatabase? = null

        fun getInstance(context: Context): StaffTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StaffTrackDatabase::class.java,
                    "stafftrack_offline.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
