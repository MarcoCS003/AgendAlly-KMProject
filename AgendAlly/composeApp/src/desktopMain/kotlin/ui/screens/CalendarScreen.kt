package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import models.EventTestData
import ui.components.CalendarView
import ui.components.EventsList
import utils.CalendarUtils

enum class CalendarMode {
    VIEW,
    ADD_EVENT,
    EDIT_EVENT
}

@Composable
fun CalendarScreen() {
    var calendarMode by remember { mutableStateOf(CalendarMode.VIEW) }
    var selectedMonth by remember {
        mutableIntStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).month.ordinal)
    }

    val monthsData = remember { CalendarUtils.getMonthsData() }
    val currentMonthData = monthsData[selectedMonth]

    // Usar los datos de prueba actualizados
    val allEvents = remember { EventTestData.getSampleEvents() }

    // Filtrar eventos para el mes seleccionado
    val monthEvents = remember(selectedMonth, allEvents) {
        allEvents.filter { event ->
            event.startDate.month.ordinal == selectedMonth ||
                    event.endDate.month.ordinal == selectedMonth ||
                    (event.startDate.month.ordinal < selectedMonth && event.endDate.month.ordinal > selectedMonth)
        }
    }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Panel izquierdo - Grid de calendarios
        Box(
            modifier = Modifier
                .weight(0.6f) // Un poco menos de espacio para el calendario
                .fillMaxHeight()
                .padding(end = 8.dp)
        ) {
            CalendarView(
                monthData = currentMonthData,
                events = allEvents,
                onMonthChanged = { newMonth -> selectedMonth = newMonth },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Panel derecho - Lista de eventos del mes seleccionado
        Card(
            modifier = Modifier
                .weight(0.4f) // Un poco más de espacio para los eventos
                .fillMaxHeight()
                .padding(start = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (calendarMode) {
                    CalendarMode.VIEW -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header del panel de eventos
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentMonthData.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "${monthEvents.size} eventos",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Lista de eventos
                            EventsList(
                                events = monthEvents,
                                onEditEvent = { calendarMode = CalendarMode.EDIT_EVENT },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    CalendarMode.ADD_EVENT -> {
                        AddEditEventScreen(
                            isEditing = false,
                            onSave = { calendarMode = CalendarMode.VIEW },
                            onCancel = { calendarMode = CalendarMode.VIEW }
                        )
                    }
                    CalendarMode.EDIT_EVENT -> {
                        AddEditEventScreen(
                            isEditing = true,
                            onSave = { calendarMode = CalendarMode.VIEW },
                            onCancel = { calendarMode = CalendarMode.VIEW }
                        )
                    }
                }

                // FAB para añadir evento (solo en modo VIEW)
                if (calendarMode == CalendarMode.VIEW) {
                    FloatingActionButton(
                        onClick = { calendarMode = CalendarMode.ADD_EVENT },
                        modifier = Modifier
                            .offset(x = (-16).dp, y = (-16).dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir evento")
                    }
                }
            }
        }
    }
}