package com.example.queueless_smartqueue.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.queueless_smartqueue.model.Organization
import com.example.queueless_smartqueue.model.QueueService
import com.example.queueless_smartqueue.ui.components.QueueLessBottomBar
import com.example.queueless_smartqueue.ui.screens.staff.*
import com.example.queueless_smartqueue.ui.screens.state.UIStateDemoScreen
import com.example.queueless_smartqueue.ui.screens.user.*
import com.example.queueless_smartqueue.ui.state.QueueViewModel

@Composable
fun AppNavigation(
    viewModel: QueueViewModel = viewModel()
) {
    val navController = rememberNavController()

    val userToken by viewModel.userToken.collectAsState()
    val organizations by viewModel.organizations.collectAsState()
    val services by viewModel.services.collectAsState()
    val counters by viewModel.counters.collectAsState()
    val staffStats by viewModel.staffStats.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val history by viewModel.history.collectAsState()
    val specialState by viewModel.specialState.collectAsState()
    val isStaffMode by viewModel.isStaffMode.collectAsState()
    val currentServingToken by viewModel.currentServingToken.collectAsState()

    var selectedOrg by remember { mutableStateOf<Organization?>(organizations.firstOrNull()) }
    var selectedService by remember { mutableStateOf<QueueService?>(services.firstOrNull()) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "splash"

    // Show bottom bar on primary user bottom-nav screens
    val showBottomBar = currentRoute in listOf("home", "my_queue", "history", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar && !isStaffMode) {
                QueueLessBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            // Login Screen
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onStaffLoginClick = {
                        navController.navigate("staff_dashboard")
                    }
                )
            }

            // Home Screen (Bottom Nav Item 1)
            composable("home") {
                HomeScreen(
                    organizations = organizations,
                    onOrganizationSelected = { org ->
                        selectedOrg = org
                        navController.navigate("org_services")
                    },
                    onNotificationsClick = {
                        navController.navigate("notifications")
                    },
                    onSwitchToStaffMode = {
                        navController.navigate("staff_dashboard")
                    },
                    onDemoStatesClick = {
                        navController.navigate("state_demo")
                    }
                )
            }

            // Organization Services Screen
            composable("org_services") {
                OrgServicesScreen(
                    organization = selectedOrg,
                    services = services,
                    onBackClick = { navController.popBackStack() },
                    onServiceSelected = { service ->
                        selectedService = service
                        navController.navigate("take_token")
                    }
                )
            }

            // Take Token Screen
            composable("take_token") {
                TakeTokenScreen(
                    service = selectedService,
                    onBackClick = { navController.popBackStack() },
                    onTakeTokenConfirmed = {
                        if (selectedService != null) {
                            viewModel.takeToken(selectedService!!)
                        }
                        navController.navigate("token_confirmation") {
                            popUpTo("home")
                        }
                    }
                )
            }

            // Token Confirmation Screen
            composable("token_confirmation") {
                TokenConfirmationScreen(
                    tokenInfo = userToken,
                    onViewLiveQueueClick = {
                        navController.navigate("live_queue")
                    },
                    onCancelTokenClick = {
                        viewModel.cancelToken()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            // Live Queue Tracking Screen (Key Screen)
            composable("live_queue") {
                LiveQueueScreen(
                    tokenInfo = userToken,
                    counters = counters,
                    onSimulateNextToken = { viewModel.callNextToken() },
                    onSimulateCounterToggle = { viewModel.toggleCounter(3) },
                    onCancelToken = {
                        viewModel.cancelToken()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            // Notifications Screen
            composable("notifications") {
                NotificationsScreen(
                    notifications = notifications,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // My Queue Screen (Bottom Nav Item 2)
            composable("my_queue") {
                MyQueueScreen(
                    userToken = userToken,
                    onTrackQueueClick = {
                        navController.navigate("live_queue")
                    },
                    onFindServiceClick = {
                        navController.navigate("home")
                    }
                )
            }

            // History Screen (Bottom Nav Item 3)
            composable("history") {
                HistoryScreen(historyItems = history)
            }

            // Profile Screen (Bottom Nav Item 4)
            composable("profile") {
                ProfileScreen(
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onSwitchToStaffMode = {
                        navController.navigate("staff_dashboard")
                    }
                )
            }

            // UI State Demo Screen
            composable("state_demo") {
                UIStateDemoScreen(
                    currentState = specialState,
                    onStateSelected = { state ->
                        viewModel.setSpecialState(state)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Staff Dashboard Screen
            composable("staff_dashboard") {
                StaffDashboardScreen(
                    stats = staffStats,
                    onNavigateToQueueManagement = { navController.navigate("staff_queue") },
                    onNavigateToCounters = { navController.navigate("counter_management") },
                    onNavigateToAnalytics = { navController.navigate("admin_analytics") },
                    onBackToUserMode = { navController.navigate("home") }
                )
            }

            // Counter Management Screen
            composable("counter_management") {
                CounterManagementScreen(
                    counters = counters,
                    onToggleCounter = { id -> viewModel.toggleCounter(id) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Staff Queue Management Screen
            composable("staff_queue") {
                StaffQueueManagementScreen(
                    currentServingToken = currentServingToken,
                    onCallNext = { viewModel.callNextToken() },
                    onPauseQueue = { viewModel.setSpecialState(com.example.queueless_smartqueue.model.SpecialUIState.QUEUE_PAUSED) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Admin Analytics Screen
            composable("admin_analytics") {
                AdminAnalyticsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
