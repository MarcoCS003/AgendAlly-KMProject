package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ui.components.AgendAllyNavigationRail
import ui.components.NavigationScreen

@Composable
fun MainScreen() {
    var selectedScreen by remember { mutableStateOf(NavigationScreen.CALENDAR) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail
        AgendAllyNavigationRail(
            selectedScreen = selectedScreen,
            onScreenSelected = { selectedScreen = it },
            modifier = Modifier.width(120.dp).padding(vertical = 16.dp).clip(RoundedCornerShape(15.dp))
        )

        // Contenido principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedScreen) {
                NavigationScreen.CALENDAR -> CalendarScreen()
                NavigationScreen.INSTITUTE -> InstitutePlaceholderScreen()
                NavigationScreen.SETTINGS -> SettingsPlaceholderScreen()
            }
        }
    }
}



@Composable
fun InstitutePlaceholderScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            "Pantalla de Instituto - Próximamente",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun SettingsPlaceholderScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            "Pantalla de Configuración - Próximamente",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}