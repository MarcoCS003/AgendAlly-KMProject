package models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

data class MonthData(
    val id: Int,
    val name: String,
    val month: Month,
    val year: Int,
    val weeks: List<List<Int>> // Días organizados por semanas
)

data class ProcessedEvent(
    val day: Int,
    val event: Event,
    val shape: EventShape,
    val additionalEvents: List<Event> = emptyList()
)