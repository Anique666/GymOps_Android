package com.example.gymmanagement.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.example.gymmanagement.R

@Composable
fun AppBottomBar(selectedItemId: Int, onDestinationSelected: (Int) -> Unit) {
    val destinations = listOf(
        BottomBarDestination(
            itemId = R.id.navDashboard,
            route = AppRoutes.DASHBOARD,
            icon = Icons.Default.Home,
            label = stringResource(R.string.nav_dashboard)
        ),
        BottomBarDestination(
            itemId = R.id.navMembers,
            route = AppRoutes.MEMBERS,
            icon = Icons.Default.People,
            label = stringResource(R.string.nav_members)
        ),
        BottomBarDestination(
            itemId = R.id.navInventory,
            route = AppRoutes.INVENTORY,
            icon = Icons.Default.Build,
            label = stringResource(R.string.nav_inventory)
        ),
        BottomBarDestination(
            itemId = R.id.navPlans,
            route = AppRoutes.PLANS,
            icon = Icons.Default.Payments,
            label = stringResource(R.string.nav_plans)
        ),
        BottomBarDestination(
            itemId = R.id.navReports,
            route = AppRoutes.REPORTS,
            icon = Icons.Default.PieChart,
            label = stringResource(R.string.nav_reports)
        )
    )

    NavigationBar(containerColor = colorResource(R.color.surface)) {
        destinations.forEach { destination ->
            val selected = selectedItemId == destination.itemId
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onDestinationSelected(destination.itemId)
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(destination.label)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorResource(R.color.text_primary),
                    selectedTextColor = colorResource(R.color.text_primary),
                    unselectedIconColor = colorResource(R.color.text_secondary),
                    unselectedTextColor = colorResource(R.color.text_secondary),
                    indicatorColor = colorResource(R.color.nav_active_indicator)
                )
            )
        }
    }
}

private data class BottomBarDestination(
    val itemId: Int,
    val route: String,
    val icon: ImageVector,
    val label: String
)
