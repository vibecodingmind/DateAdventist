package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.AdventHeartsRepository
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.LikesScreen
import com.example.ui.screens.MatchesScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SafetyCenterScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.AdventHeartsTheme
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.DiscoveryViewModel
import com.example.ui.viewmodel.MatchesViewModel
import com.example.ui.viewmodel.SafetyViewModel
import com.example.ui.viewmodel.SubscriptionViewModel

import com.example.ui.theme.ThemeWrapper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = AdventHeartsRepository.getInstance(applicationContext)

        setContent {
            ThemeWrapper {
                AdventHeartsApp(repository = repository)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Welcome : Screen("welcome", "Welcome")
    object Onboarding : Screen("onboarding", "Onboarding")
    object Discover : Screen("discover", "Discover", Icons.Filled.Style)
    object Likes : Screen("likes", "Likes", Icons.Filled.Favorite)
    object Matches : Screen("matches", "Matches", Icons.Filled.People)
    object Messages : Screen("messages", "Messages", Icons.Filled.Message)
    object Search : Screen("search", "Search", Icons.Filled.Search)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object ChatDetail : Screen("chat_detail/{matchId}", "Chat") {
        fun createRoute(matchId: String) = "chat_detail/$matchId"
    }
    object SafetyCenter : Screen("safety_center", "Safety")
    object Subscription : Screen("subscription", "Subscription")
    object Settings : Screen("settings", "Settings")
    object AdminLogin : Screen("admin_login", "Admin Login")
    object AdminDashboard : Screen("admin_dashboard", "Admin")
}

@Composable
fun AdventHeartsApp(
    repository: AdventHeartsRepository,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    discoveryViewModel: DiscoveryViewModel = viewModel(),
    matchesViewModel: MatchesViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel(),
    safetyViewModel: SafetyViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val onboardingStep by authViewModel.onboardingStep.collectAsState()
    val likesYouList by matchesViewModel.likesYouProfiles.collectAsState()
    val matchesList by matchesViewModel.matchesWithProfiles.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentUserId, onboardingStep) {
        if (currentUserId == null) {
            if (currentRoute != Screen.Welcome.route) {
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(0)
                }
            }
        } else if (onboardingStep > 0) {
            if (currentRoute != Screen.Onboarding.route) {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(0)
                }
            }
        }
    }

    val bottomNavItems = listOf(
        Screen.Discover,
        Screen.Likes,
        Screen.Matches,
        Screen.Messages,
        Screen.Profile
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar && currentUserId != null && onboardingStep == 0) {
                NavigationBar(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF090A0E),
                    tonalElevation = 12.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.ui.graphics.Color(0xFFFF3366),
                                selectedTextColor = androidx.compose.ui.graphics.Color(0xFFFF3366),
                                indicatorColor = androidx.compose.ui.graphics.Color(0xFF222533),
                                unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF64748B),
                                unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF64748B)
                            ),
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (screen == Screen.Likes && likesYouList.isNotEmpty()) {
                                            Badge(
                                                containerColor = androidx.compose.ui.graphics.Color(0xFFF43F5E),
                                                contentColor = androidx.compose.ui.graphics.Color.White
                                            ) { Text("${likesYouList.size}") }
                                        } else if (screen == Screen.Messages && matchesList.isNotEmpty()) {
                                            Badge(
                                                containerColor = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                                                contentColor = androidx.compose.ui.graphics.Color.White
                                            ) { Text("${matchesList.size}") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = screen.icon!!,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { Text(screen.title, fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium) },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val startDestination = if (currentUserId == null) {
                Screen.Welcome.route
            } else if (onboardingStep > 0) {
                Screen.Onboarding.route
            } else {
                Screen.Discover.route
            }

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(Screen.Welcome.route) {
                    WelcomeScreen(
                        authViewModel = authViewModel,
                        onStartRegistration = {
                            authViewModel.setOnboardingStep(1)
                            navController.navigate(Screen.Onboarding.route)
                        },
                        onDemoLogin = { userId ->
                            authViewModel.switchAccount(userId)
                            if (userId == "usr_admin") {
                                navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(0)
                                }
                            } else {
                                navController.navigate(Screen.Discover.route) {
                                    popUpTo(0)
                                }
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        authViewModel = authViewModel,
                        onCompleteOnboarding = {
                            authViewModel.setOnboardingStep(0)
                            navController.navigate(Screen.Discover.route) {
                                popUpTo(0)
                            }
                        }
                    )
                }

                composable(Screen.Discover.route) {
                    DiscoverScreen(
                        discoveryViewModel = discoveryViewModel,
                        onNavigateToChat = { matchId ->
                            matchesViewModel.selectMatch(matchId)
                            navController.navigate(Screen.ChatDetail.createRoute(matchId))
                        },
                        onNavigateToSubscription = {
                            navController.navigate(Screen.Subscription.route)
                        }
                    )
                }

                composable(Screen.Likes.route) {
                    LikesScreen(
                        matchesViewModel = matchesViewModel,
                        subscriptionViewModel = subscriptionViewModel,
                        onNavigateToChat = { matchId ->
                            navController.navigate(Screen.ChatDetail.createRoute(matchId))
                        },
                        onNavigateToSubscription = {
                            navController.navigate(Screen.Subscription.route)
                        }
                    )
                }

                composable(Screen.Matches.route) {
                    MatchesScreen(
                        matchesViewModel = matchesViewModel,
                        onNavigateToChat = { matchId ->
                            navController.navigate(Screen.ChatDetail.createRoute(matchId))
                        }
                    )
                }

                composable(Screen.Messages.route) {
                    MessagesScreen(
                        matchesViewModel = matchesViewModel,
                        onNavigateToChat = { matchId ->
                            navController.navigate(Screen.ChatDetail.createRoute(matchId))
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        discoveryViewModel = discoveryViewModel,
                        onSelectProfile = { prof ->
                            navController.navigate(Screen.Discover.route)
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        safetyViewModel = safetyViewModel,
                        onNavigateToSubscription = { navController.navigate(Screen.Subscription.route) },
                        onNavigateToSafety = { navController.navigate(Screen.SafetyCenter.route) },
                        onNavigateToAdmin = { navController.navigate(Screen.AdminLogin.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        authViewModel = authViewModel,
                        subscriptionViewModel = subscriptionViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToSubscription = { navController.navigate(Screen.Subscription.route) },
                        onNavigateToSafety = { navController.navigate(Screen.SafetyCenter.route) }
                    )
                }

                composable(Screen.AdminLogin.route) {
                    AdminLoginScreen(
                        adminViewModel = adminViewModel,
                        onLoginSuccess = { role ->
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.AdminLogin.route) { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.ChatDetail.route) { backStackEntry ->
                    val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                    ChatDetailScreen(
                        matchesViewModel = matchesViewModel,
                        safetyViewModel = safetyViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.SafetyCenter.route) {
                    SafetyCenterScreen(safetyViewModel = safetyViewModel)
                }

                composable(Screen.Subscription.route) {
                    SubscriptionScreen(
                        subscriptionViewModel = subscriptionViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminDashboard.route) {
                    AdminDashboardScreen(adminViewModel = adminViewModel)
                }
            }
        }
    }
}
