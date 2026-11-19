package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.model.ThemeMode
import com.example.data.model.UserRole
import com.example.ui.components.OnlineSavedPills
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.BackupRestoreScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmployeeDashboardScreen
import com.example.ui.screens.EmployeeDetailsScreen
import com.example.ui.screens.EmployeesScreen
import com.example.ui.screens.LeaveScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PayrollScreen
import com.example.ui.screens.PayslipScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.StaffTrackUiState
import com.example.ui.state.StaffTrackViewModel
import com.example.ui.theme.RoyalBlueDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showInBottomBar: Boolean = false,
    val showInRail: Boolean = true
)

val staffTrackDestinations = listOf(
    NavDestination("dashboard", "Dashboard", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard, showInBottomBar = true),
    NavDestination("employees", "Employees", Icons.Rounded.Groups, Icons.Outlined.Groups, showInBottomBar = true),
    NavDestination("attendance", "Attendance", Icons.Rounded.FactCheck, Icons.Outlined.FactCheck, showInBottomBar = true),
    NavDestination("leaves", "Leaves", Icons.Rounded.BeachAccess, Icons.Outlined.BeachAccess, showInBottomBar = true),
    NavDestination("payroll", "Payroll", Icons.Rounded.Payments, Icons.Outlined.Payments, showInBottomBar = true),
    NavDestination("reports", "Reports", Icons.Rounded.Assessment, Icons.Outlined.Assessment, showInBottomBar = false),
    NavDestination("settings", "Settings", Icons.Rounded.Settings, Icons.Outlined.Settings, showInBottomBar = false),
    NavDestination("backup", "Backup & Restore", Icons.Rounded.Backup, Icons.Outlined.Backup, showInBottomBar = false, showInRail = false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffTrackApp(
    viewModel: StaffTrackViewModel,
    uiState: StaffTrackUiState
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showLaunchSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1300)
        showLaunchSplash = false
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (showLaunchSplash || uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF045568), RoyalBlueDark)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_app_launch),
                    contentDescription = "StaffTrack launch logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(132.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .border(2.5.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(30.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "StaffTrack",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Employee Attendance & Payroll",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.88f)
                )
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(6.dp))
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }
        return
    }

    if (!uiState.session.isLoggedIn) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
            LoginScreen(
                uiState = uiState,
                onLoginAdmin = viewModel::loginAsAdmin,
                onLoginEmployee = viewModel::loginAsEmployee,
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    // Employee Self-Service Mode
    if (uiState.session.role == UserRole.EMPLOYEE) {
        val loggedEmp = uiState.employees.firstOrNull { it.id == uiState.session.employeeId }
            ?: uiState.employees.firstOrNull()
        val empNavController = rememberNavController()
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Employee Portal", fontWeight = FontWeight.Bold) },
                    actions = {
                        OnlineSavedPills(isOnline = uiState.isOnline, compact = true, modifier = Modifier.padding(end = 4.dp))
                        IconButton(onClick = {
                            val next = if (uiState.settings.themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
                            viewModel.saveSettings(uiState.settings.copy(themeMode = next))
                        }) {
                            Icon(
                                imageVector = if (uiState.settings.themeMode == ThemeMode.DARK) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = "Toggle theme"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            if (loggedEmp == null) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    OutlinedButton(onClick = viewModel::logout) { Text("Return to Login") }
                }
            } else {
                NavHost(
                    navController = empNavController,
                    startDestination = "employee_dashboard",
                    enterTransition = { fadeIn(animationSpec = tween(220)) },
                    exitTransition = { fadeOut(animationSpec = tween(180)) },
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    composable("employee_dashboard") {
                        EmployeeDashboardScreen(
                            employee = loggedEmp,
                            uiState = uiState,
                            onCheckInOut = viewModel::markAttendance,
                            onSubmitLeave = viewModel::submitLeave,
                            onOpenPayslip = { empId, ym -> empNavController.navigate("payslip/$empId/$ym") },
                            onLogout = viewModel::logout
                        )
                    }
                    composable(
                        route = "payslip/{employeeId}/{yearMonth}",
                        arguments = listOf(
                            navArgument("employeeId") { type = NavType.LongType },
                            navArgument("yearMonth") { type = NavType.StringType }
                        )
                    ) { entry ->
                        val empId = entry.arguments?.getLong("employeeId") ?: loggedEmp.id
                        val ym = entry.arguments?.getString("yearMonth") ?: uiState.currentMonth
                        PayslipScreen(
                            employeeId = empId,
                            yearMonth = ym,
                            uiState = uiState,
                            onBack = { empNavController.popBackStack() }
                        )
                    }
                }
            }
        }
        return
    }

    // HR / Admin Full Workspace
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "dashboard"
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var openAddEmployee by remember { mutableStateOf(false) }
    var editEmployeeId by remember { mutableStateOf<Long?>(null) }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val currentDestLabel = when {
        currentRoute.startsWith("employee_details") -> "Employee Profile"
        currentRoute.startsWith("payslip") -> "Salary Payslip"
        else -> staffTrackDestinations.firstOrNull { it.route == currentRoute }?.label ?: "StaffTrack"
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 840.dp
        val isMedium = maxWidth in 600.dp..<840.dp
        val isCompact = maxWidth < 600.dp

        val drawerContent: @Composable () -> Unit = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_admin_panel),
                        contentDescription = "Admin Panel logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "StaffTrack",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Admin Panel • HRM & Payroll",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                staffTrackDestinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label
                            )
                        },
                        label = { Text(dest.label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
                        selected = selected,
                        onClick = {
                            navigateTo(dest.route)
                            if (!isExpanded) {
                                scope.launch { drawerState.close() }
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("drawer_nav_${dest.route}")
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                OnlineSavedPills(isOnline = uiState.isOnline, modifier = Modifier.padding(horizontal = 6.dp))
                Spacer(modifier = Modifier.size(10.dp))
                OutlinedButton(
                    onClick = {
                        if (!isExpanded) scope.launch { drawerState.close() }
                        viewModel.logout()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Switch Role / Sign Out")
                }
            }
        }

        val appScaffold: @Composable () -> Unit = {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = currentDestLabel,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            if (!isExpanded) {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("open_drawer_button")
                                ) {
                                    Icon(Icons.Rounded.Menu, contentDescription = "Open navigation menu")
                                }
                            }
                        },
                        actions = {
                            OnlineSavedPills(
                                isOnline = uiState.isOnline,
                                compact = isCompact,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                            if (isCompact) {
                                IconButton(
                                    onClick = { navigateTo("reports") },
                                    modifier = Modifier.testTag("header_nav_reports")
                                ) {
                                    Icon(
                                        imageVector = if (currentRoute == "reports") Icons.Rounded.Assessment else Icons.Outlined.Assessment,
                                        contentDescription = "Reports",
                                        tint = if (currentRoute == "reports") {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                IconButton(
                                    onClick = { navigateTo("settings") },
                                    modifier = Modifier.testTag("header_nav_settings")
                                ) {
                                    Icon(
                                        imageVector = if (currentRoute == "settings") Icons.Rounded.Settings else Icons.Outlined.Settings,
                                        contentDescription = "Settings",
                                        tint = if (currentRoute == "settings") {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    val next = if (uiState.settings.themeMode == ThemeMode.DARK) {
                                        ThemeMode.LIGHT
                                    } else {
                                        ThemeMode.DARK
                                    }
                                    viewModel.saveSettings(uiState.settings.copy(themeMode = next))
                                },
                                modifier = Modifier.testTag("toggle_theme_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.settings.themeMode == ThemeMode.DARK) {
                                        Icons.Rounded.LightMode
                                    } else {
                                        Icons.Rounded.DarkMode
                                    },
                                    contentDescription = "Toggle light/dark theme"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    if (isCompact) {
                        NavigationBar(
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            staffTrackDestinations.filter { it.showInBottomBar }.forEach { dest ->
                                val selected = currentRoute == dest.route ||
                                    (dest.route == "employees" && currentRoute.startsWith("employee_details")) ||
                                    (dest.route == "payroll" && currentRoute.startsWith("payslip"))
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("bottom_nav_${dest.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isMedium) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            staffTrackDestinations.filter { it.showInRail }.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("rail_nav_${dest.route}")
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        enterTransition = { fadeIn(animationSpec = tween(220)) },
                        exitTransition = { fadeOut(animationSpec = tween(180)) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                    composable("dashboard") {
                        DashboardScreen(
                            uiState = uiState,
                            onNavigateTo = ::navigateTo,
                            onQuickAddEmployee = {
                                openAddEmployee = true
                                navigateTo("employees")
                            },
                            onSwitchAccount = viewModel::logout
                        )
                    }
                    composable("employees") {
                        EmployeesScreen(
                            uiState = uiState,
                            openAddImmediately = openAddEmployee,
                            editEmployeeId = editEmployeeId,
                            onEditHandled = { editEmployeeId = null },
                            onAddHandled = { openAddEmployee = false },
                            onOpenEmployeeDetails = { id -> navController.navigate("employee_details/$id") },
                            onGenerateCode = { viewModel.getNextEmployeeCode() },
                            onSaveEmployee = viewModel::saveEmployee,
                            onToggleStatus = viewModel::toggleEmployeeStatus,
                            onDeleteEmployee = viewModel::deleteEmployee
                        )
                    }
                    composable(
                        route = "employee_details/{employeeId}",
                        arguments = listOf(navArgument("employeeId") { type = NavType.LongType })
                    ) { entry ->
                        val empId = entry.arguments?.getLong("employeeId") ?: 0L
                        EmployeeDetailsScreen(
                            employeeId = empId,
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onEditEmployee = { emp ->
                                editEmployeeId = emp.id
                                navigateTo("employees")
                            },
                            onToggleStatus = viewModel::toggleEmployeeStatus,
                            onDeleteEmployee = viewModel::deleteEmployee,
                            onOpenPayslip = { id, ym -> navController.navigate("payslip/$id/$ym") }
                        )
                    }
                    composable("attendance") {
                        AttendanceScreen(
                            uiState = uiState,
                            onMarkAttendance = viewModel::markAttendance,
                            onBulkMarkAttendance = viewModel::bulkMarkAttendance
                        )
                    }
                    composable("leaves") {
                        LeaveScreen(
                            uiState = uiState,
                            onSubmitLeave = viewModel::submitLeave,
                            onApproveLeave = viewModel::approveLeave,
                            onRejectLeave = viewModel::rejectLeave,
                            onDeleteLeave = viewModel::deleteLeave
                        )
                    }
                    composable("payroll") {
                        PayrollScreen(
                            uiState = uiState,
                            onGeneratePayroll = viewModel::generatePayroll,
                            onUpdatePayroll = viewModel::updatePayrollRecord,
                            onApproveAll = viewModel::approveAllPayroll,
                            onOpenPayslip = { empId, ym -> navController.navigate("payslip/$empId/$ym") }
                        )
                    }
                    composable(
                        route = "payslip/{employeeId}/{yearMonth}",
                        arguments = listOf(
                            navArgument("employeeId") { type = NavType.LongType },
                            navArgument("yearMonth") { type = NavType.StringType }
                        )
                    ) { entry ->
                        val empId = entry.arguments?.getLong("employeeId") ?: 0L
                        val ym = entry.arguments?.getString("yearMonth") ?: uiState.currentMonth
                        PayslipScreen(
                            employeeId = empId,
                            yearMonth = ym,
                            uiState = uiState,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("reports") {
                        ReportsScreen(
                            uiState = uiState,
                            buildReport = viewModel::buildReportTable,
                            onMessage = viewModel::emitMessage
                        )
                    }
                    composable("backup") {
                        BackupRestoreScreen(
                            uiState = uiState,
                            onExportJson = { viewModel.exportBackupJson() },
                            onValidateBackup = viewModel::validateBackupJson,
                            onRestoreBackup = viewModel::restoreFromBackup,
                            onMessage = viewModel::emitMessage
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            uiState = uiState,
                            onSaveSettings = viewModel::saveSettings
                        )
                    }
                    }
                }
            }
        }

        if (isExpanded) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(modifier = Modifier.width(260.dp)) {
                        drawerContent()
                    }
                },
                content = appScaffold
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                        drawerContent()
                    }
                },
                content = appScaffold
            )
        }
    }
}
