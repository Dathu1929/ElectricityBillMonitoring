package com.example.electricitybillmonitoring

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.electricitybillmonitoring.ui.screens.*
import com.example.electricitybillmonitoring.ui.viewmodel.*
import com.example.electricitybillmonitoring.util.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val billViewModel: BillViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by viewModels()
    private val connectionViewModel: ConnectionViewModel by viewModels()
    private val reminderViewModel: ReminderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationUtils.createNotificationChannel(this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val user by authViewModel.userState.collectAsState()
                    var authScreen by remember { mutableStateOf("login") }
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(onTimeout = { showSplash = false })
                    } else if (user == null) {
                        when (authScreen) {
                            "login" -> LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = { dashboardViewModel.loadData() },
                                onNavigateToRegister = { authScreen = "register" }
                            )
                            "register" -> RegisterScreen(
                                viewModel = authViewModel,
                                onRegisterSuccess = { authScreen = "login" },
                                onNavigateToLogin = { authScreen = "login" }
                            )
                        }
                    } else {
                        AppNavigation(
                            authViewModel = authViewModel,
                            dashboardViewModel = dashboardViewModel,
                            billViewModel = billViewModel,
                            paymentViewModel = paymentViewModel,
                            connectionViewModel = connectionViewModel,
                            reminderViewModel = reminderViewModel
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    billViewModel: BillViewModel,
    paymentViewModel: PaymentViewModel,
    connectionViewModel: ConnectionViewModel,
    reminderViewModel: ReminderViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val bottomItems = listOf(
        BottomNavItem("Home", "home", Icons.Default.Home),
        BottomNavItem("Bills", "bills", Icons.AutoMirrored.Filled.List),
        BottomNavItem("Analytics", "analytics", Icons.Default.Info),
        BottomNavItem("Reminders", "reminders", Icons.Default.Notifications),
        BottomNavItem("Profile", "profile", Icons.Default.Person)
    )

    var selectedRoute by rememberSaveable { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = selectedRoute == "home",
                    onClick = { selectedRoute = "home"; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Bill History") },
                    selected = selectedRoute == "bills",
                    onClick = { selectedRoute = "bills"; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Payments") },
                    selected = selectedRoute == "payments",
                    onClick = { selectedRoute = "payments"; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.ShoppingCart, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Meters") },
                    selected = selectedRoute == "connections",
                    onClick = { selectedRoute = "connections"; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Build, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Calculate Bill") },
                    selected = selectedRoute == "calculate_bill",
                    onClick = { selectedRoute = "calculate_bill"; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Info, null) }
                )
                NavigationDrawerItem(
                    label = { Text("New Customer") },
                    selected = selectedRoute == "new_customer",
                    onClick = { selectedRoute = "new_customer"; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Person, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Service Lookup") },
                    selected = selectedRoute == "service_lookup",
                    onClick = { selectedRoute = "service_lookup"; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Search, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = selectedRoute == "settings",
                    onClick = { selectedRoute = "settings"; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Settings, null) }
                )
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { authViewModel.logout() },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Electricity Monitor") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = selectedRoute == item.route,
                            onClick = { selectedRoute = item.route },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (selectedRoute) {
                    "home" -> HomeScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToPayments = { selectedRoute = "payments" },
                        onNavigateToConnections = { selectedRoute = "connections" },
                        onNavigateToBills = { selectedRoute = "bills" },
                        onNavigateToAnalytics = { selectedRoute = "analytics" },
                        onNavigateToReminders = { selectedRoute = "reminders" },
                        onNavigateToProfile = { selectedRoute = "profile" }
                    )
                    "bills" -> BillsScreen(viewModel = billViewModel)
                    "analytics" -> AnalyticsScreen(billViewModel = billViewModel)
                    "reminders" -> RemindersScreen(viewModel = reminderViewModel)
                    "profile" -> ProfileScreen(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onLogout = { authViewModel.logout() }
                    )
                    "payments" -> PaymentsScreen(
                        paymentViewModel = paymentViewModel,
                        billViewModel = billViewModel,
                        dashboardViewModel = dashboardViewModel
                    )
                    "connections" -> ConnectionsScreen(
                        connectionViewModel = connectionViewModel,
                        dashboardViewModel = dashboardViewModel
                    )
                    "calculate_bill" -> CalculateBillScreen(
                        billViewModel = billViewModel,
                        onNavigateBack = { selectedRoute = "home" }
                    )
                    "new_customer" -> NewCustomerScreen(
                        connectionViewModel = connectionViewModel,
                        dashboardViewModel = dashboardViewModel,
                        billViewModel = billViewModel,
                        onNavigateBack = { selectedRoute = "home" }
                    )
                    "service_lookup" -> EnterServiceNumberScreen(
                        billViewModel = billViewModel,
                        connectionViewModel = connectionViewModel,
                        onNavigateBack = { selectedRoute = "home" },
                        onNavigateToBills = { selectedRoute = "bills" },
                        onNavigateToPayments = { selectedRoute = "payments" },
                        onNavigateToAnalytics = { selectedRoute = "analytics" },
                        onNavigateToReminders = { selectedRoute = "reminders" },
                        onNavigateToProfile = { selectedRoute = "profile" }
                    )
                    "settings" -> SettingsScreen(viewModel = dashboardViewModel)
                }
            }
        }
    }
}

private data class BottomNavItem(val title: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
