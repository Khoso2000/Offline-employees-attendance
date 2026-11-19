package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ReportType
import com.example.ui.components.SaasCard
import com.example.ui.components.SectionHeader
import com.example.ui.state.StaffTrackUiState
import com.example.util.ReportExporter
import com.example.util.ReportTableData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    uiState: StaffTrackUiState,
    buildReport: (ReportType, String, String, String, String, Long?) -> ReportTableData,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(ReportType.MONTHLY_ATTENDANCE) }
    var yearMonth by remember { mutableStateOf(uiState.currentMonth) }
    var startDate by remember { mutableStateOf("${uiState.currentMonth}-01") }
    var endDate by remember { mutableStateOf(uiState.todayDate) }
    var departmentFilter by remember { mutableStateOf("All") }
    var employeeFilterId by remember { mutableStateOf<Long?>(null) }

    var deptOpen by remember { mutableStateOf(false) }
    var empOpen by remember { mutableStateOf(false) }

    val departments = remember(uiState.employees) {
        listOf("All") + uiState.employees.map { it.department }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val empMap = remember(uiState.employees) { uiState.employees.associateBy { it.id } }

    val reportData = remember(
        selectedType, startDate, endDate, yearMonth, departmentFilter, employeeFilterId,
        uiState.employees, uiState.attendance, uiState.leaves, uiState.payrolls, uiState.settings
    ) {
        buildReport(selectedType, startDate, endDate, yearMonth, departmentFilter, employeeFilterId)
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val bytes = ReportExporter.buildPdfBytes(reportData, uiState.settings)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                onMessage("PDF saved successfully")
            }.onFailure {
                onMessage("Failed to save PDF: ${it.message}")
            }
        }
    }

    val saveCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val bytes = ReportExporter.generateCsvString(reportData).toByteArray(Charsets.UTF_8)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                onMessage("CSV saved successfully")
            }.onFailure {
                onMessage("Failed to save CSV: ${it.message}")
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Reports & Exports",
                subtitle = "Filter by employee, department, or date range and export PDF, CSV, or Print"
            )
        }

        // Report type chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ReportType.entries) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.label) },
                        modifier = Modifier.testTag("report_chip_${type.name}")
                    )
                }
            }
        }

        // Filters Card
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(selectedType.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = yearMonth,
                            onValueChange = { yearMonth = it },
                            label = { Text("Month (yyyy-MM)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = deptOpen,
                            onExpandedChange = { deptOpen = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = departmentFilter,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Department") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptOpen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            )
                            ExposedDropdownMenu(expanded = deptOpen, onDismissRequest = { deptOpen = false }) {
                                departments.forEach { d ->
                                    DropdownMenuItem(text = { Text(d) }, onClick = { departmentFilter = d; deptOpen = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = empOpen,
                            onExpandedChange = { empOpen = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = employeeFilterId?.let { empMap[it]?.fullName } ?: "All Employees",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Employee") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empOpen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            )
                            ExposedDropdownMenu(expanded = empOpen, onDismissRequest = { empOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("All Employees") },
                                    onClick = { employeeFilterId = null; empOpen = false }
                                )
                                uiState.employees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text("${emp.fullName} (${emp.employeeCode})") },
                                        onClick = { employeeFilterId = emp.id; empOpen = false }
                                    )
                                }
                            }
                        }
                    }

                    // Export & Print Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                savePdfLauncher.launch("StaffTrack_${selectedType.name}_${yearMonth}.pdf")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save PDF")
                        }
                        Button(
                            onClick = {
                                saveCsvLauncher.launch("StaffTrack_${selectedType.name}_${yearMonth}.csv")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save CSV")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val bytes = ReportExporter.buildPdfBytes(reportData, uiState.settings)
                                ReportExporter.shareFile(context, "StaffTrack_${selectedType.name}.pdf", "application/pdf", bytes)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share PDF")
                        }
                        OutlinedButton(
                            onClick = {
                                val bytes = ReportExporter.generateCsvString(reportData).toByteArray(Charsets.UTF_8)
                                ReportExporter.shareFile(context, "StaffTrack_${selectedType.name}.csv", "text/csv", bytes)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share CSV")
                        }
                        OutlinedButton(
                            onClick = { ReportExporter.printReport(context, reportData, uiState.settings) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print")
                        }
                    }
                }
            }
        }

        // Report Table Preview
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(reportData.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(reportData.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (reportData.rows.isEmpty()) {
                        Text(
                            text = "No matching records for the selected filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                                reportData.headers.forEach { header ->
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(108.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            HorizontalDivider()
                            reportData.rows.forEach { row ->
                                Row(modifier = Modifier.padding(vertical = 7.dp)) {
                                    row.forEach { cell ->
                                        Text(
                                            text = cell,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.width(108.dp),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }

                    if (reportData.summaryLines.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        reportData.summaryLines.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
