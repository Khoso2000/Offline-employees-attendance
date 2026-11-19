package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.CompanySettings
import com.example.data.model.PayrollBasis
import com.example.data.model.ThemeMode
import com.example.ui.components.SaasCard
import com.example.ui.components.SectionHeader
import com.example.ui.state.StaffTrackUiState
import com.example.util.ReportExporter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: StaffTrackUiState,
    onSaveSettings: (CompanySettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initial = uiState.settings

    var companyName by remember(initial) { mutableStateOf(initial.companyName) }
    var companyLogoUri by remember(initial) { mutableStateOf(initial.companyLogoUri) }
    var currency by remember(initial) { mutableStateOf(initial.currency) }
    var dateFormat by remember(initial) { mutableStateOf(initial.dateFormat) }
    var workingDays by remember(initial) { mutableStateOf(initial.workingDaysList.toSet()) }
    var officeStart by remember(initial) { mutableStateOf(initial.officeStartTime) }
    var officeEnd by remember(initial) { mutableStateOf(initial.officeEndTime) }
    var graceMinutes by remember(initial) { mutableStateOf(initial.lateGraceMinutes.toString()) }
    var halfDayHours by remember(initial) { mutableStateOf(initial.halfDayHoursThreshold.toString()) }
    var otMultiplier by remember(initial) { mutableStateOf(initial.overtimeMultiplier.toString()) }
    var customOtRate by remember(initial) { mutableStateOf(initial.customOvertimeHourlyRate.toInt().toString()) }
    var payrollBasis by remember(initial) { mutableStateOf(initial.payrollBasis) }
    var latePenaltyEnabled by remember(initial) { mutableStateOf(initial.latePenaltyEnabled) }
    var lateCountHalfDay by remember(initial) { mutableStateOf(initial.lateCountForHalfDayDeduction.toString()) }
    var deductAbsentDays by remember(initial) { mutableStateOf(initial.deductAbsentDays) }
    var defaultAnnual by remember(initial) { mutableStateOf(initial.defaultAnnualLeave.toString()) }
    var defaultSick by remember(initial) { mutableStateOf(initial.defaultSickLeave.toString()) }
    var defaultCasual by remember(initial) { mutableStateOf(initial.defaultCasualLeave.toString()) }
    var autoHoliday by remember(initial) { mutableStateOf(initial.autoMarkHolidayOnWeeklyOff) }
    var themeMode by remember(initial) { mutableStateOf(initial.themeMode) }

    var currencyOpen by remember { mutableStateOf(false) }
    var dateFmtOpen by remember { mutableStateOf(false) }
    var basisOpen by remember { mutableStateOf(false) }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = ReportExporter.copyUriToInternalFile(context, uri, "company_logo")
            if (saved != null) companyLogoUri = saved
        }
    }

    val currencies = listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR", "INR", "CAD", "AUD")
    val dateFormats = listOf("dd MMM yyyy", "yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy")
    val allDays = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "System Settings",
                subtitle = "Configure company identity, working schedule, payroll formulas, and appearance"
            )
        }

        // Appearance / Theme
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { idx, mode ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = {
                                    themeMode = mode
                                    onSaveSettings(initial.copy(themeMode = mode))
                                },
                                shape = SegmentedButtonDefaults.itemShape(idx, ThemeMode.entries.size)
                            ) {
                                Text(mode.label.replace(" Mode", "").replace(" Default", ""), maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Company Profile
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Company Profile & Locale", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val logo = companyLogoUri
                        if (!logo.isNullOrBlank()) {
                            AsyncImage(
                                model = if (logo.startsWith("/")) File(logo) else logo,
                                contentDescription = "Company logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Icon(Icons.Rounded.Business, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        OutlinedButton(
                            onClick = {
                                logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (companyLogoUri == null) "Upload Company Logo" else "Change Logo")
                        }
                    }

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = currencyOpen,
                            onExpandedChange = { currencyOpen = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = currency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyOpen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            )
                            ExposedDropdownMenu(expanded = currencyOpen, onDismissRequest = { currencyOpen = false }) {
                                currencies.forEach { c ->
                                    DropdownMenuItem(text = { Text(c) }, onClick = { currency = c; currencyOpen = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = dateFmtOpen,
                            onExpandedChange = { dateFmtOpen = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = dateFormat,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Date Format") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateFmtOpen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            )
                            ExposedDropdownMenu(expanded = dateFmtOpen, onDismissRequest = { dateFmtOpen = false }) {
                                dateFormats.forEach { f ->
                                    DropdownMenuItem(text = { Text(f) }, onClick = { dateFormat = f; dateFmtOpen = false })
                                }
                            }
                        }
                    }
                }
            }
        }

        // Working Days & Office Timings
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Working Days & Office Timings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        allDays.forEach { day ->
                            val selected = day in workingDays
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    workingDays = if (selected && workingDays.size > 1) workingDays - day else workingDays + day
                                },
                                label = { Text(day.take(3)) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = officeStart,
                            onValueChange = { officeStart = it },
                            label = { Text("Start (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = officeEnd,
                            onValueChange = { officeEnd = it },
                            label = { Text("End (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = graceMinutes,
                            onValueChange = { graceMinutes = it },
                            label = { Text("Late Grace (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = halfDayHours,
                            onValueChange = { halfDayHours = it },
                            label = { Text("Half-Day Threshold (hrs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-treat weekly off as Holiday", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = autoHoliday, onCheckedChange = { autoHoliday = it })
                    }
                }
            }
        }

        // Salary & Overtime Rules
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Salary & Overtime Rules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    ExposedDropdownMenuBox(
                        expanded = basisOpen,
                        onExpandedChange = { basisOpen = it }
                    ) {
                        OutlinedTextField(
                            value = payrollBasis.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Monthly Daily-Rate Basis") },
                            supportingText = { Text(payrollBasis.description) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = basisOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(expanded = basisOpen, onDismissRequest = { basisOpen = false }) {
                            PayrollBasis.entries.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text("${b.label} — ${b.description}") },
                                    onClick = { payrollBasis = b; basisOpen = false }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = otMultiplier,
                            onValueChange = { otMultiplier = it },
                            label = { Text("OT Multiplier (e.g. 1.5x)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customOtRate,
                            onValueChange = { customOtRate = it },
                            label = { Text("Custom OT/hr (0 = auto)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Deduct unexcused Absent days", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = deductAbsentDays, onCheckedChange = { deductAbsentDays = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Late Arrival Penalty", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = latePenaltyEnabled, onCheckedChange = { latePenaltyEnabled = it })
                    }

                    if (latePenaltyEnabled) {
                        OutlinedTextField(
                            value = lateCountHalfDay,
                            onValueChange = { lateCountHalfDay = it },
                            label = { Text("Late Arrivals per Half-Day Deduction") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Default Leave Allowances
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Default Annual Leave Allowances (Days/Year)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = defaultAnnual,
                            onValueChange = { defaultAnnual = it },
                            label = { Text("Annual") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = defaultSick,
                            onValueChange = { defaultSick = it },
                            label = { Text("Sick") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = defaultCasual,
                            onValueChange = { defaultCasual = it },
                            label = { Text("Casual") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSaveSettings(
                        initial.copy(
                            companyName = companyName.trim().ifBlank { "StaffTrack Company" },
                            companyLogoUri = companyLogoUri,
                            currency = currency,
                            dateFormat = dateFormat,
                            workingDaysList = workingDays.toList(),
                            officeStartTime = officeStart.trim().ifBlank { "09:00" },
                            officeEndTime = officeEnd.trim().ifBlank { "17:00" },
                            lateGraceMinutes = graceMinutes.toIntOrNull()?.coerceIn(0, 180) ?: 15,
                            halfDayHoursThreshold = halfDayHours.toDoubleOrNull()?.coerceIn(1.0, 12.0) ?: 4.0,
                            overtimeMultiplier = otMultiplier.toDoubleOrNull()?.coerceIn(1.0, 5.0) ?: 1.5,
                            customOvertimeHourlyRate = customOtRate.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                            payrollBasis = payrollBasis,
                            latePenaltyEnabled = latePenaltyEnabled,
                            lateCountForHalfDayDeduction = lateCountHalfDay.toIntOrNull()?.coerceIn(1, 30) ?: 3,
                            deductAbsentDays = deductAbsentDays,
                            defaultAnnualLeave = defaultAnnual.toIntOrNull()?.coerceIn(0, 120) ?: 14,
                            defaultSickLeave = defaultSick.toIntOrNull()?.coerceIn(0, 120) ?: 10,
                            defaultCasualLeave = defaultCasual.toIntOrNull()?.coerceIn(0, 120) ?: 8,
                            autoMarkHolidayOnWeeklyOff = autoHoliday,
                            themeMode = themeMode
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button")
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Settings")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
