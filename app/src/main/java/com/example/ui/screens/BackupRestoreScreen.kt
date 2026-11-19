package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.BackupValidationResult
import com.example.data.repository.StaffTrackBackup
import com.example.domain.DateUtils
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.SaasCard
import com.example.ui.components.SectionHeader
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.PresentGreen
import com.example.util.ReportExporter
import kotlinx.coroutines.launch

@Composable
fun BackupRestoreScreen(
    uiState: StaffTrackUiState,
    onExportJson: suspend () -> String,
    onValidateBackup: (String) -> BackupValidationResult,
    onRestoreBackup: (StaffTrackBackup) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingRestore by remember { mutableStateOf<StaffTrackBackup?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = onExportJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    onMessage("JSON backup exported successfully")
                }.onFailure {
                    onMessage("Export failed: ${it.message}")
                }
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                val result = onValidateBackup(raw)
                if (result.isValid && result.backup != null) {
                    pendingRestore = result.backup
                } else {
                    validationError = result.errorMessage ?: "Invalid backup file."
                }
            }.onFailure {
                validationError = "Unable to read file: ${it.message}"
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
                title = "Offline Database & Backup",
                subtitle = "Automatic local Room SQLite persistence with JSON export & validated restore"
            )
        }

        // Local Storage Status Card
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.Storage, contentDescription = null, tint = PresentGreen, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local Offline Database Active", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Last saved locally: ${DateUtils.formatTimestamp(uiState.settings.lastSavedTimestamp)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Saved", tint = PresentGreen)
                    }

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BackupCountCol("Employees", uiState.employees.size.toString())
                        BackupCountCol("Attendance", uiState.attendance.size.toString())
                        BackupCountCol("Leaves", uiState.leaves.size.toString())
                        BackupCountCol("Payroll", uiState.payrolls.size.toString())
                    }

                    HorizontalDivider()

                    val lastBackupStr = uiState.settings.lastBackupTimestamp?.let {
                        DateUtils.formatTimestamp(it)
                    } ?: "Never backed up yet"
                    Text(
                        text = "Last JSON Backup: $lastBackupStr",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Export Backup Card
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Export Complete JSON Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Exports all employees, attendance history, leave records, payroll slips, activity logs, and company settings into a portable JSON file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                createBackupLauncher.launch("StaffTrack_Backup_${uiState.todayDate}.json")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_json_backup_button")
                        ) {
                            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save JSON File")
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val json = onExportJson()
                                    ReportExporter.shareFile(
                                        context,
                                        "StaffTrack_Backup_${uiState.todayDate}.json",
                                        "application/json",
                                        json.toByteArray(Charsets.UTF_8)
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Backup")
                        }
                    }
                }
            }
        }

        // Import & Restore Card
        item {
            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text("Import & Restore Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Select a previously exported StaffTrack JSON backup. The file is validated for schema integrity before asking for confirmation to replace current records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { openBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_json_backup_button")
                    ) {
                        Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select JSON Backup to Restore")
                    }
                }
            }
        }
    }

    pendingRestore?.let { backup ->
        ConfirmActionDialog(
            title = "Overwrite & Restore Database?",
            message = "Backup from ${DateUtils.formatTimestamp(backup.exportedAt)} verified!\n\n" +
                "• Employees: ${backup.employees.size}\n" +
                "• Attendance Logs: ${backup.attendance.size}\n" +
                "• Leave Requests: ${backup.leaveRequests.size}\n" +
                "• Payroll Records: ${backup.payrollRecords.size}\n\n" +
                "Proceeding will overwrite all current local data.",
            confirmLabel = "Restore Now",
            isDestructive = true,
            onConfirm = {
                onRestoreBackup(backup)
                pendingRestore = null
            },
            onDismiss = { pendingRestore = null }
        )
    }

    validationError?.let { err ->
        AlertDialog(
            onDismissRequest = { validationError = null },
            icon = { Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = AbsentRed) },
            title = { Text("Backup Validation Failed", fontWeight = FontWeight.Bold) },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { validationError = null }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun BackupCountCol(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
