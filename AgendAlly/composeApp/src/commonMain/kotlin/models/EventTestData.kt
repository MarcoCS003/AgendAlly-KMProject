package models

import kotlinx.datetime.LocalDate

object EventTestData {
    fun getSampleEvents(): List<Event> {
        return listOf(
            Event(
                id = 0,
                title = "Días inhábiles",
                colorIndex = 13,
                startDate = LocalDate(2025, 1, 1),
                endDate = LocalDate(2025, 1, 1),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 2,
                colorIndex = 13,
                title = "Días inhábiles",
                startDate = LocalDate(2025, 2, 3),
                endDate = LocalDate(2025, 2, 3),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 4,
                colorIndex = 6,
                title = "Período vacacional",
                startDate = LocalDate(2025, 7, 7),
                endDate = LocalDate(2025, 7, 31),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 5,
                colorIndex = 5,
                title = "Actividades intersemestrales",
                startDate = LocalDate(2025, 6, 9),
                endDate = LocalDate(2025, 7, 4),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 6,
                colorIndex = 5,
                title = "Actividades intersemestrales",
                startDate = LocalDate(2025, 1, 8),
                endDate = LocalDate(2025, 1, 17),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 7,
                colorIndex = 14,
                title = "Inicio de clases",
                startDate = LocalDate(2025, 1, 27),
                endDate = LocalDate(2025, 1, 27),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 8,
                colorIndex = 13,
                title = "Días inhábiles",
                startDate = LocalDate(2025, 3, 17),
                endDate = LocalDate(2025, 3, 17),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 9,
                colorIndex = 6,
                title = "Período vacacional",
                startDate = LocalDate(2025, 1, 1),
                endDate = LocalDate(2025, 1, 7),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 10,
                colorIndex = 6,
                title = "Período vacacional",
                startDate = LocalDate(2025, 4, 14),
                endDate = LocalDate(2025, 4, 25),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 11,
                colorIndex = 14,
                title = "Inicio de clases",
                startDate = LocalDate(2025, 2, 26),
                endDate = LocalDate(2025, 2, 26),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 12,
                colorIndex = 13,
                title = "Días inhábiles",
                startDate = LocalDate(2025, 5, 1),
                endDate = LocalDate(2025, 5, 1),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 13,
                colorIndex = 13,
                title = "Días inhábiles",
                startDate = LocalDate(2025, 5, 5),
                endDate = LocalDate(2025, 5, 5),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 14,
                colorIndex = 13,
                title = "Días inhábiles",
                startDate = LocalDate(2025, 5, 15),
                endDate = LocalDate(2025, 5, 15),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 15,
                colorIndex = 14,
                title = "Fin de clases",
                startDate = LocalDate(2025, 5, 30),
                endDate = LocalDate(2025, 5, 30),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 16,
                colorIndex = 4,
                title = "Inscripciones",
                startDate = LocalDate(2025, 1, 20),
                endDate = LocalDate(2025, 1, 21),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 17,
                colorIndex = 11,
                title = "Reinscripciones",
                startDate = LocalDate(2025, 1, 22),
                endDate = LocalDate(2025, 1, 24),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 18,
                colorIndex = 7,
                title = "Entrega de calificaciones",
                startDate = LocalDate(2025, 6, 5),
                endDate = LocalDate(2025, 6, 6),
                category = EventCategory.INSTITUTIONAL
            ),
            Event(
                id = 19,
                title = "Convocatoria Servicio Social",
                shortDescription = "Registro para servicio",
                longDescription = "Estimado estudiante de TICs si le interesa realizar su servicio social durante el periodo Diciembre 2024 - Junio 2025 guardar esta información Coordinación Instruccional de tutorías Desarrollo Académico.",
                location = "Edificio 6",
                startDate = LocalDate(2025, 11, 28),
                endDate = LocalDate(2025, 11, 29),
                category = EventCategory.CAREER,
                colorIndex = 12
            )
        )
    }

    val scheduleColors = listOf(
        0xFFFFF59D, // 0 - Azul
        0xFFCE93D8, // 1 - Verde
        0xFF80DEEA, // 2 - Naranja
        0xFFFFAB91, // 3 - Púrpura
        0xFFC5E1A5, // 4 - Rojo
        0xFFB39DDB, // 5 - Cian
        0xFFFFCC80, // 6 - Amarillo
        0xFF90CAF9, // 7 - Marrón
        0xFFF48FB1, // 8 - Azul gris
        0xFF81D4FA, // 9 - Verde claro
        0xFFFFD54F, // 10 - Rosa
        0xFF4DB6AC, // 11 - Púrpura profundo
        0xFF9575CD, // 12 - Verde azulado
        0xFFE57373, // 13 - Gris
        0xFF7986CB, // 14 - Índigo
        0xFF4DD0E1  // 15 - Naranja profundo
    )
}