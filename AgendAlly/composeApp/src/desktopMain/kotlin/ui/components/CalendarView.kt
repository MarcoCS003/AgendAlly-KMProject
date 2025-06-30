package ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import models.Event
import models.EventTestData
import models.MonthData
import models.ProcessedEvent
import utils.CalendarUtils

@Composable
fun CalendarView(
    monthData: MonthData,
    events: List<Event>,
    onMonthChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val allMonths = remember { CalendarUtils.getMonthsData() }
    val gridState = rememberLazyGridState()

    // Scroll al mes actual al inicio
    LaunchedEffect(Unit) {
        val currentMonthIndex = CalendarUtils.getCurrentMonthIndex()
        gridState.scrollToItem(currentMonthIndex)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Encabezado con institución
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Eventos Instituto Tecnológico de Puebla",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Año actual
                Text(
                    text = "2025",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Grid de meses (2 columnas en desktop)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            items(allMonths) { month ->
                val monthEvents = events.filter { event ->
                    event.startDate.month.ordinal == month.month.ordinal ||
                            event.endDate.month.ordinal == month.month.ordinal ||
                            (event.startDate.month.ordinal < month.month.ordinal &&
                                    event.endDate.month.ordinal > month.month.ordinal)
                }

                val isSelected = month.month.ordinal == monthData.month.ordinal

                MonthCalendarCard(
                    monthData = month,
                    events = monthEvents,
                    isSelected = isSelected,
                    today = today,
                    onMonthSelected = { onMonthChanged(month.month.ordinal) }
                )
            }
        }
    }
}

@Composable
fun MonthCalendarCard(
    monthData: MonthData,
    events: List<Event>,
    isSelected: Boolean,
    today: LocalDate,
    onMonthSelected: () -> Unit
) {
    val processedEvents = remember(events, monthData) {
        processEventsForMonth(monthData, events)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f) // Hace que sea más cuadrado
            .clickable { onMonthSelected() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        3.dp,
                        Color(0xFF4CAF50), // Verde
                        RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFF4CAF50).copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Encabezado del mes (más compacto)
            Text(
                text = monthData.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Encabezados de días de la semana (más pequeños)
            CompactWeekDaysHeader()

            Spacer(modifier = Modifier.height(4.dp))

            // Grid del calendario (ocupa el resto del espacio)
            Box(modifier = Modifier.weight(1f)) {
                CompactCalendarGrid(
                    monthData = monthData,
                    processedEvents = processedEvents,
                    today = today
                )
            }
        }
    }
}

@Composable
fun CompactWeekDaysHeader() {
    // ✅ CAMBIO: Empezar con Domingo
    val daysOfWeek = listOf("D", "L", "M", "M", "J", "V", "S") // Dom, Lun, Mar, Mie, Jue, Vie, Sab

    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 8.sp
                )
            }
        }
    }
}

@Composable
fun CompactCalendarGrid(
    monthData: MonthData,
    processedEvents: Map<Int, ProcessedEvent>,
    today: LocalDate
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        monthData.weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                week.forEach { day ->
                    CompactCalendarDayCell(
                        day = day,
                        monthData = monthData,
                        processedEvent = processedEvents[day],
                        isToday = day != 0 && today.dayOfMonth == day &&
                                today.month.name == monthData.month.name &&
                                today.year == monthData.year,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CompactCalendarDayCell(
    day: Int,
    monthData: MonthData,
    processedEvent: ProcessedEvent?,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        if (day == 0) {
            // Celda vacía
            return@Box
        }

        if (processedEvent != null) {
            // Día con evento
            CompactDayWithEvent(
                day = day,
                processedEvent = processedEvent,
                isToday = isToday
            )
        } else {
            // Día normal
            CompactDayCell(
                day = day,
                isToday = isToday
            )
        }
    }
}

@Composable
fun CompactDayCell(
    day: Int,
    isToday: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isToday) {
                    Modifier
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clip(CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = 10.sp
        )
    }
}

@Composable
fun CompactDayWithEvent(
    day: Int,
    processedEvent: ProcessedEvent,
    isToday: Boolean
) {
    val color = Color(EventTestData.scheduleColors[processedEvent.event.colorIndex % EventTestData.scheduleColors.size])
    val shape = when (processedEvent.shape) {
        models.EventShape.Circle -> CircleShape
        models.EventShape.RoundedStart -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
        models.EventShape.RoundedMiddle -> RoundedCornerShape(0.dp)
        models.EventShape.RoundedEnd -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
        models.EventShape.RoundedFull -> RoundedCornerShape(4.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color, shape)
            .then(
                if (isToday) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

// Función auxiliar para procesar eventos del mes (igual que antes)
fun processEventsForMonth(monthData: MonthData, events: List<Event>): Map<Int, ProcessedEvent> {
    val processedEvents = mutableMapOf<Int, ProcessedEvent>()

    events.forEach { event ->
        val eventStartMonth = event.startDate.month.ordinal
        val eventEndMonth = event.endDate.month.ordinal
        val currentMonth = monthData.month.ordinal

        // Calcular días de inicio y fin para este mes
        val startDay = when {
            eventStartMonth == currentMonth -> event.startDate.dayOfMonth
            eventStartMonth < currentMonth -> 1
            else -> return@forEach // Evento no aplica a este mes
        }

        val endDay = when {
            eventEndMonth == currentMonth -> event.endDate.dayOfMonth
            eventEndMonth > currentMonth -> monthData.weeks.flatten().filter { it != 0 }.maxOrNull() ?: 31
            else -> return@forEach // Evento no aplica a este mes
        }

        for (day in startDay..endDay) {
            if (day in 1..31) {
                val shape = when {
                    event.startDate == event.endDate -> models.EventShape.Circle
                    day == startDay -> models.EventShape.RoundedStart
                    day == endDay -> models.EventShape.RoundedEnd
                    else -> models.EventShape.RoundedMiddle
                }

                if (processedEvents.containsKey(day)) {
                    // Si ya hay un evento, agregarlo como adicional
                    val existing = processedEvents[day]!!
                    processedEvents[day] = existing.copy(
                        additionalEvents = existing.additionalEvents + event
                    )
                } else {
                    // Crear nuevo evento procesado
                    processedEvents[day] = ProcessedEvent(
                        day = day,
                        event = event,
                        shape = shape
                    )
                }
            }
        }
    }

    return processedEvents
}