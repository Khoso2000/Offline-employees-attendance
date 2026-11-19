package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.EmployeeEntity
import com.example.data.model.EmploymentStatus
import com.example.data.model.UserRole
import com.example.ui.components.OnlineSavedPills
import com.example.ui.components.SaasCard
import com.example.ui.state.StaffTrackUiState
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: StaffTrackUiState,
    onLoginAdmin: () -> Unit,
    onLoginEmployee: (EmployeeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(UserRole.ADMIN) }
    var adminEmail by remember { mutableStateOf("admin@stafftrack.app") }
    var passcode by remember { mutableStateOf("1234") }
    var empMenuExpanded by remember { mutableStateOf(false) }

    val activeEmployees = remember(uiState.employees) {
        uiState.employees.filter { it.status == EmploymentStatus.ACTIVE }
    }
    var selectedEmployee by remember(activeEmployees) {
        mutableStateOf(activeEmployees.firstOrNull())
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(RoyalBluePrimary, RoyalBlueDark, MaterialTheme.colorScheme.background)
                )
            )
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Brand Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Badge,
                        contentDescription = "StaffTrack logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "StaffTrack",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${uiState.settings.companyName} • HRM & Payroll",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OnlineSavedPills(isOnline = uiState.isOnline)
            }

            SaasCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Sign In to Workspace",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose HR/Admin management or Employee self-service portal. Works 100% offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        UserRole.entries.forEachIndexed { idx, role ->
                            SegmentedButton(
                                selected = selectedRole == role,
                                onClick = { selectedRole = role; errorMsg = null },
                                shape = SegmentedButtonDefaults.itemShape(idx, UserRole.entries.size),
                                icon = {
                                    Icon(
                                        imageVector = if (role == UserRole.ADMIN) Icons.Rounded.AdminPanelSettings else Icons.Rounded.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            ) {
                                Text(role.label)
                            }
                        }
                    }

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = AbsentRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (selectedRole == UserRole.ADMIN) {
                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it; errorMsg = null },
                            label = { Text("Administrator ID / Email") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = passcode,
                            onValueChange = { passcode = it; errorMsg = null },
                            label = { Text("Workspace PIN") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (adminEmail.isBlank() || passcode.isBlank()) {
                                    errorMsg = "Enter administrator ID and workspace PIN."
                                } else {
                                    onLoginAdmin()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_admin_button")
                        ) {
                            Text("Sign In as HR / Admin", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = empMenuExpanded,
                            onExpandedChange = { empMenuExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedEmployee?.let { "${it.fullName} (${it.employeeCode})" }
                                    ?: "No active employees available",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Employee Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empMenuExpanded) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            )
                            ExposedDropdownMenu(
                                expanded = empMenuExpanded,
                                onDismissRequest = { empMenuExpanded = false }
                            ) {
                                activeEmployees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text("${emp.fullName} (${emp.employeeCode}) • ${emp.department}") },
                                        onClick = {
                                            selectedEmployee = emp
                                            empMenuExpanded = false
                                            errorMsg = null
                                        }
                                    )
                                }
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Employee Portal lets staff check in/out, inspect monthly attendance, apply for leave, and download payslips.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val emp = selectedEmployee
                                if (emp == null) {
                                    errorMsg = "No active employee selected."
                                } else {
                                    onLoginEmployee(emp)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_employee_button")
                        ) {
                            Text("Open Employee Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
