package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.CompanySettings
import com.example.data.model.PayrollBasis
import com.example.data.model.ThemeMode
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class UserSession(
    val isLoggedIn: Boolean = true,
    val role: UserRole = UserRole.ADMIN,
    val employeeId: Long? = null
)

private val Context.staffTrackDataStore: DataStore<Preferences> by preferencesDataStore(name = "stafftrack_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val IS_SEEDED = booleanPreferencesKey("is_seeded")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ROLE = stringPreferencesKey("user_role")
        val LOGGED_EMPLOYEE_ID = longPreferencesKey("logged_employee_id")
        val COMPANY_NAME = stringPreferencesKey("company_name")
        val COMPANY_LOGO_URI = stringPreferencesKey("company_logo_uri")
        val CURRENCY = stringPreferencesKey("currency")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val WORKING_DAYS = stringPreferencesKey("working_days")
        val OFFICE_START_TIME = stringPreferencesKey("office_start_time")
        val OFFICE_END_TIME = stringPreferencesKey("office_end_time")
        val LATE_GRACE_MINUTES = intPreferencesKey("late_grace_minutes")
        val HALF_DAY_HOURS_THRESHOLD = doublePreferencesKey("half_day_hours_threshold")
        val OVERTIME_MULTIPLIER = doublePreferencesKey("overtime_multiplier")
        val CUSTOM_OT_RATE = doublePreferencesKey("custom_ot_rate")
        val PAYROLL_BASIS = stringPreferencesKey("payroll_basis")
        val LATE_PENALTY_ENABLED = booleanPreferencesKey("late_penalty_enabled")
        val LATE_COUNT_HALF_DAY = intPreferencesKey("late_count_half_day")
        val DEDUCT_ABSENT_DAYS = booleanPreferencesKey("deduct_absent_days")
        val DEFAULT_ANNUAL_LEAVE = intPreferencesKey("default_annual_leave")
        val DEFAULT_SICK_LEAVE = intPreferencesKey("default_sick_leave")
        val DEFAULT_CASUAL_LEAVE = intPreferencesKey("default_casual_leave")
        val AUTO_MARK_HOLIDAY = booleanPreferencesKey("auto_mark_holiday")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_BACKUP_TS = longPreferencesKey("last_backup_ts")
        val LAST_SAVED_TS = longPreferencesKey("last_saved_ts")
    }

    val settingsFlow: Flow<CompanySettings> = context.staffTrackDataStore.data.map { prefs ->
        val default = CompanySettings()
        CompanySettings(
            companyName = prefs[Keys.COMPANY_NAME] ?: default.companyName,
            companyLogoUri = prefs[Keys.COMPANY_LOGO_URI]?.takeIf { it.isNotBlank() },
            currency = prefs[Keys.CURRENCY] ?: default.currency,
            dateFormat = prefs[Keys.DATE_FORMAT] ?: default.dateFormat,
            workingDaysList = prefs[Keys.WORKING_DAYS]
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: default.workingDaysList,
            officeStartTime = prefs[Keys.OFFICE_START_TIME] ?: default.officeStartTime,
            officeEndTime = prefs[Keys.OFFICE_END_TIME] ?: default.officeEndTime,
            lateGraceMinutes = prefs[Keys.LATE_GRACE_MINUTES] ?: default.lateGraceMinutes,
            halfDayHoursThreshold = prefs[Keys.HALF_DAY_HOURS_THRESHOLD] ?: default.halfDayHoursThreshold,
            overtimeMultiplier = prefs[Keys.OVERTIME_MULTIPLIER] ?: default.overtimeMultiplier,
            customOvertimeHourlyRate = prefs[Keys.CUSTOM_OT_RATE] ?: default.customOvertimeHourlyRate,
            payrollBasis = prefs[Keys.PAYROLL_BASIS]?.let {
                runCatching { PayrollBasis.valueOf(it) }.getOrNull()
            } ?: default.payrollBasis,
            latePenaltyEnabled = prefs[Keys.LATE_PENALTY_ENABLED] ?: default.latePenaltyEnabled,
            lateCountForHalfDayDeduction = prefs[Keys.LATE_COUNT_HALF_DAY] ?: default.lateCountForHalfDayDeduction,
            deductAbsentDays = prefs[Keys.DEDUCT_ABSENT_DAYS] ?: default.deductAbsentDays,
            defaultAnnualLeave = prefs[Keys.DEFAULT_ANNUAL_LEAVE] ?: default.defaultAnnualLeave,
            defaultSickLeave = prefs[Keys.DEFAULT_SICK_LEAVE] ?: default.defaultSickLeave,
            defaultCasualLeave = prefs[Keys.DEFAULT_CASUAL_LEAVE] ?: default.defaultCasualLeave,
            autoMarkHolidayOnWeeklyOff = prefs[Keys.AUTO_MARK_HOLIDAY] ?: default.autoMarkHolidayOnWeeklyOff,
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: default.themeMode,
            lastBackupTimestamp = prefs[Keys.LAST_BACKUP_TS],
            lastSavedTimestamp = prefs[Keys.LAST_SAVED_TS] ?: default.lastSavedTimestamp
        )
    }

    suspend fun getSettingsOnce(): CompanySettings = settingsFlow.first()

    suspend fun isSeeded(): Boolean =
        context.staffTrackDataStore.data.first()[Keys.IS_SEEDED] ?: false

    suspend fun markSeeded() {
        context.staffTrackDataStore.edit { it[Keys.IS_SEEDED] = true }
    }

    suspend fun updateSettings(settings: CompanySettings) {
        val now = System.currentTimeMillis()
        context.staffTrackDataStore.edit { prefs ->
            prefs[Keys.COMPANY_NAME] = settings.companyName
            prefs[Keys.COMPANY_LOGO_URI] = settings.companyLogoUri.orEmpty()
            prefs[Keys.CURRENCY] = settings.currency
            prefs[Keys.DATE_FORMAT] = settings.dateFormat
            prefs[Keys.WORKING_DAYS] = settings.workingDaysList.joinToString(",")
            prefs[Keys.OFFICE_START_TIME] = settings.officeStartTime
            prefs[Keys.OFFICE_END_TIME] = settings.officeEndTime
            prefs[Keys.LATE_GRACE_MINUTES] = settings.lateGraceMinutes
            prefs[Keys.HALF_DAY_HOURS_THRESHOLD] = settings.halfDayHoursThreshold
            prefs[Keys.OVERTIME_MULTIPLIER] = settings.overtimeMultiplier
            prefs[Keys.CUSTOM_OT_RATE] = settings.customOvertimeHourlyRate
            prefs[Keys.PAYROLL_BASIS] = settings.payrollBasis.name
            prefs[Keys.LATE_PENALTY_ENABLED] = settings.latePenaltyEnabled
            prefs[Keys.LATE_COUNT_HALF_DAY] = settings.lateCountForHalfDayDeduction
            prefs[Keys.DEDUCT_ABSENT_DAYS] = settings.deductAbsentDays
            prefs[Keys.DEFAULT_ANNUAL_LEAVE] = settings.defaultAnnualLeave
            prefs[Keys.DEFAULT_SICK_LEAVE] = settings.defaultSickLeave
            prefs[Keys.DEFAULT_CASUAL_LEAVE] = settings.defaultCasualLeave
            prefs[Keys.AUTO_MARK_HOLIDAY] = settings.autoMarkHolidayOnWeeklyOff
            prefs[Keys.THEME_MODE] = settings.themeMode.name
            if (settings.lastBackupTimestamp != null) {
                prefs[Keys.LAST_BACKUP_TS] = settings.lastBackupTimestamp
            }
            prefs[Keys.LAST_SAVED_TS] = now
        }
    }

    suspend fun touchLastSaved() {
        context.staffTrackDataStore.edit { it[Keys.LAST_SAVED_TS] = System.currentTimeMillis() }
    }

    val sessionFlow: Flow<UserSession> = context.staffTrackDataStore.data.map { prefs ->
        UserSession(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: true,
            role = prefs[Keys.USER_ROLE]?.let {
                runCatching { UserRole.valueOf(it) }.getOrNull()
            } ?: UserRole.ADMIN,
            employeeId = prefs[Keys.LOGGED_EMPLOYEE_ID]
        )
    }

    suspend fun setSession(isLoggedIn: Boolean, role: UserRole, employeeId: Long?) {
        context.staffTrackDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = isLoggedIn
            prefs[Keys.USER_ROLE] = role.name
            if (employeeId != null) {
                prefs[Keys.LOGGED_EMPLOYEE_ID] = employeeId
            } else {
                prefs.remove(Keys.LOGGED_EMPLOYEE_ID)
            }
        }
    }

    suspend fun updateLastBackupTimestamp(ts: Long = System.currentTimeMillis()) {
        context.staffTrackDataStore.edit {
            it[Keys.LAST_BACKUP_TS] = ts
            it[Keys.LAST_SAVED_TS] = ts
        }
    }
}
