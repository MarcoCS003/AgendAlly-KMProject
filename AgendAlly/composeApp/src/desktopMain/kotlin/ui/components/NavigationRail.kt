package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavigationScreen(val label: String, val icon: ImageVector) {
    LOGIN("Login", Icons.Default.Login),
    CALENDAR("Calendario", Icons.Default.CalendarToday),
    ORGANIZATION_DASHBOARD("Instituto", Icons.Default.Business),  // ✅ ACTUALIZADO
    ORGANIZATION_SETUP("Setup", Icons.Default.Settings),
    CONFIGURATION("Configuración", Icons.Default.Settings),
    CHANNELS_LIST("Canales", Icons.Default.Notifications),
    ADD_CHANNEL("Agregar Canal", Icons.Default.Add),
    TEST("Test", Icons.Default.BugReport)
}

@Composable
fun AgendAllyNavigationRail(
    selectedScreen: NavigationScreen,
    onScreenSelected: (NavigationScreen) -> Unit,
    isUserLoggedIn: Boolean,
    userName: String?,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Logo de la aplicación
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "AgendAlly",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Elementos de navegación principales
            val navigationItems = if (isUserLoggedIn) {
                listOf(
                    NavigationScreen.CALENDAR,
                    NavigationScreen.ORGANIZATION_DASHBOARD,  // ✅ NUEVO
                    NavigationScreen.CONFIGURATION
                )
            } else {
                listOf(NavigationScreen.CALENDAR)
            }

            navigationItems.forEach { screen ->
                NavigationRailItem(
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label
                        )
                    },
                    label = {
                        Text(
                            text = screen.label,
                            fontSize = 10.sp,
                            fontWeight = if (selectedScreen == screen) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = selectedScreen == screen,
                    onClick = { onScreenSelected(screen) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Área de usuario en la parte inferior
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Avatar/Login
                if (isUserLoggedIn) {
                    // Avatar del usuario logueado
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName?.firstOrNull()?.uppercase() ?: "U",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (userName != null) {
                        Text(
                            text = userName.split(" ").firstOrNull() ?: "",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Botón de Login/Logout
                NavigationRailItem(
                    icon = {
                        Icon(
                            imageVector = if (isUserLoggedIn) Icons.Default.Logout else Icons.Default.Login,
                            contentDescription = if (isUserLoggedIn) "Logout" else "Login"
                        )
                    },
                    label = {
                        Text(
                            text = if (isUserLoggedIn) "Salir" else "Login",
                            fontSize = 10.sp
                        )
                    },
                    selected = selectedScreen == NavigationScreen.LOGIN,
                    onClick = { onScreenSelected(NavigationScreen.LOGIN) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = if (isUserLoggedIn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        selectedTextColor = if (isUserLoggedIn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        indicatorColor = if (isUserLoggedIn) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}