package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class NavigationScreen {
    CALENDAR,
    INSTITUTE,
    SETTINGS,
    LOGIN  // Nueva pantalla de login
}

@Composable
fun AgendAllyNavigationRail(
    selectedScreen: NavigationScreen,
    onScreenSelected: (NavigationScreen) -> Unit,
    modifier: Modifier = Modifier,
    isUserLoggedIn: Boolean = false,  // Nuevo parámetro para saber si hay usuario
    userName: String? = null  // Nombre del usuario para mostrar
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        header = {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "AgendAlly",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                // Mostrar información del usuario si está logueado
                if (isUserLoggedIn && userName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hola,",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userName.split(" ").firstOrNull() ?: "Usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) {
        // Calendario
        NavigationRailItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendario"
                )
            },
            label = { Text("Calendario") },
            selected = selectedScreen == NavigationScreen.CALENDAR,
            onClick = { onScreenSelected(NavigationScreen.CALENDAR) }
        )

        // Instituto
        NavigationRailItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "Instituto"
                )
            },
            label = { Text("Instituto") },
            selected = selectedScreen == NavigationScreen.INSTITUTE,
            onClick = { onScreenSelected(NavigationScreen.INSTITUTE) }
        )

        // Configuración
        NavigationRailItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración"
                )
            },
            label = { Text("Configuración") },
            selected = selectedScreen == NavigationScreen.SETTINGS,
            onClick = { onScreenSelected(NavigationScreen.SETTINGS) }
        )

        // Spacer para separar el botón de login
        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Login/Logout
        if (isUserLoggedIn) {
            // Si está logueado, mostrar botón de logout
            NavigationRailItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                label = {
                    Text(
                        "Logout",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                selected = false,
                onClick = { onScreenSelected(NavigationScreen.LOGIN) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.error,
                    unselectedIconColor = MaterialTheme.colorScheme.error,
                    selectedTextColor = MaterialTheme.colorScheme.error,
                    unselectedTextColor = MaterialTheme.colorScheme.error
                )
            )
        } else {
            // Si no está logueado, mostrar botón de login prominente
            NavigationRailItem(
                icon = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Iniciar sesión",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                label = {
                    Text(
                        "Login",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                selected = selectedScreen == NavigationScreen.LOGIN,
                onClick = { onScreenSelected(NavigationScreen.LOGIN) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}