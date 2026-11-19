package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.LeaveRequestEntity
import com.example.data.local.entity.PayrollRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY fullName ASC")
    fun observeAll(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees ORDER BY id ASC")
    suspend fun getAllOnce(): List<EmployeeEntity>

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun count(): Int

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: EmployeeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(employees: List<EmployeeEntity>)

    @Update
    suspend fun update(employee: EmployeeEntity)

    @Delete
    suspend fun delete(employee: EmployeeEntity)

    @Query("DELETE FROM employees")
    suspend fun deleteAll()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY id ASC")
    fun observeByDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getByDateRange(startDate: String, endDate: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance ORDER BY date DESC")
    suspend fun getAllOnce(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getForEmployeeAndDate(employeeId: Long, date: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<AttendanceEntity>)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM attendance WHERE leaveRequestId = :leaveRequestId")
    suspend fun deleteByLeaveRequestId(leaveRequestId: Long)

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
}

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leave_requests ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests ORDER BY createdAt DESC")
    suspend fun getAllOnce(): List<LeaveRequestEntity>

    @Query("SELECT * FROM leave_requests WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LeaveRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(leave: LeaveRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(leaves: List<LeaveRequestEntity>)

    @Update
    suspend fun update(leave: LeaveRequestEntity)

    @Query("DELETE FROM leave_requests WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM leave_requests")
    suspend fun deleteAll()
}

@Dao
interface PayrollDao {
    @Query("SELECT * FROM payroll_records ORDER BY yearMonth DESC, id ASC")
    fun observeAll(): Flow<List<PayrollRecordEntity>>

    @Query("SELECT * FROM payroll_records ORDER BY yearMonth DESC, id ASC")
    suspend fun getAllOnce(): List<PayrollRecordEntity>

    @Query("SELECT * FROM payroll_records WHERE employeeId = :employeeId AND yearMonth = :yearMonth LIMIT 1")
    suspend fun getForEmployeeAndMonth(employeeId: Long, yearMonth: String): PayrollRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(payroll: PayrollRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(payrolls: List<PayrollRecordEntity>)

    @Query("DELETE FROM payroll_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM payroll_records")
    suspend fun deleteAll()
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 50")
    fun observeRecent(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    suspend fun getAllOnce(): List<ActivityLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ActivityLogEntity>)

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAll()
}
