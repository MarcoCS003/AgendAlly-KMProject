package models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: Int,
    val title: String,
    val shortDescription: String = "",
    val longDescription: String = "",
    val location: String = "",
    val colorIndex: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val category: EventCategory = EventCategory.INSTITUTIONAL,
    val imagePath: String = "",
    val shape: EventShape = EventShape.RoundedFull
)

@Serializable
enum class EventCategory(val id: Int, val displayName: String) {
    INSTITUTIONAL(1, "Institucional"),
    CAREER(2, "Carrera"),
}

@Serializable
enum class EventShape {
    Circle,
    RoundedStart,
    RoundedMiddle,
    RoundedEnd,
    RoundedFull
}