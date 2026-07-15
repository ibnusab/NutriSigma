package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.viewmodel.NutriViewModel

@Composable
fun MainContainer(
    viewModel: NutriViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val items = listOf(
        NavigationItem(
            title = "Home",
            selectedIcon = Icons.Default.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard,
            tag = "tab_home"
        ),
        NavigationItem(
            title = "Hitung",
            selectedIcon = Icons.Default.Calculate,
            unselectedIcon = Icons.Outlined.Calculate,
            tag = "tab_calculate"
        ),
        NavigationItem(
            title = "Riwayat",
            selectedIcon = Icons.Default.History,
            unselectedIcon = Icons.Outlined.History,
            tag = "tab_history"
        ),
        NavigationItem(
            title = "Statistik",
            selectedIcon = Icons.Default.BarChart,
            unselectedIcon = Icons.Outlined.BarChart,
            tag = "tab_stats"
        ),
        NavigationItem(
            title = "Edukasi",
            selectedIcon = Icons.Default.Article,
            unselectedIcon = Icons.Outlined.Article,
            tag = "tab_education"
        ),
        NavigationItem(
            title = "Profil",
            selectedIcon = Icons.Default.Person,
            unselectedIcon = Icons.Outlined.Person,
            tag = "tab_profile"
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(item.title) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        modifier = Modifier.testTag(item.tag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(viewModel = viewModel)
                1 -> CalculatorScreen(viewModel = viewModel)
                2 -> HistoryScreen(viewModel = viewModel)
                3 -> StatsScreen(viewModel = viewModel)
                4 -> ArticlesScreen(viewModel = viewModel)
                5 -> ProfileScreen(viewModel = viewModel, onLogout = onLogout)
            }
        }
    }
}

private data class NavigationItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)
