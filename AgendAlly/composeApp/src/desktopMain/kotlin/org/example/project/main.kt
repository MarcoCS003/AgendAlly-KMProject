package org.example.project

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import ui.AgendAllyApp
import ui.screens.MainScreen


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Academic Ally - Gestión de Eventos Académicos",
        state = WindowState(width = 1200.dp, height = 800.dp)
    ) {
        AgendAllyApp()
    }
}