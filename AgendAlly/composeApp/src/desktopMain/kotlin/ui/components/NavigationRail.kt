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
    SETTINGS
}

@Composable
fun AgendAllyNavigationRail(
    selectedScreen: NavigationScreen,
    onScreenSelected: (NavigationScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        header = {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "AgendAlly",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ){
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
    }

}