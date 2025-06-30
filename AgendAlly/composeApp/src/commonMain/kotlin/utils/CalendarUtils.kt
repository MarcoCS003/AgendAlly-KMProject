// shared/src/commonMain/kotlin/utils/CalendarUtils.kt
package utils

import kotlinx.datetime.*
import models.MonthData

object CalendarUtils {
    fun getMonthsData(year: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year): List<MonthData> {
        return Month.values().mapIndexed { index, month ->
            val firstDay = LocalDate(year, month, 1)
            val daysInMonth = when (month) {
                Month.FEBRUARY -> if (year % 4 == 0) 29 else 28
                Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
                else -> 31
            }

            // Calcular semanas EMPEZANDO EN DOMINGO
            val weeks = mutableListOf<List<Int>>()

            // ✅ CAMBIO PRINCIPAL: Ajustar para que domingo sea 0
            val firstDayOfWeek = when (firstDay.dayOfWeek) {
                DayOfWeek.SUNDAY -> 0    // Domingo = 0 espacios
                DayOfWeek.MONDAY -> 1    // Lunes = 1 espacio
                DayOfWeek.TUESDAY -> 2   // Martes = 2 espacios
                DayOfWeek.WEDNESDAY -> 3 // Miércoles = 3 espacios
                DayOfWeek.THURSDAY -> 4  // Jueves = 4 espacios
                DayOfWeek.FRIDAY -> 5    // Viernes = 5 espacios
                DayOfWeek.SATURDAY -> 6  // Sábado = 6 espacios
                else -> TODO()
            }

            var currentWeek = mutableListOf<Int>()

            // Añadir espacios vacíos para el primer día
            repeat(firstDayOfWeek) {
                currentWeek.add(0)
            }

            // Añadir días
            for (day in 1..daysInMonth) {
                currentWeek.add(day)
                if (currentWeek.size == 7) {
                    weeks.add(currentWeek)
                    currentWeek = mutableListOf()
                }
            }

            // Completar última semana
            if (currentWeek.isNotEmpty()) {
                while (currentWeek.size < 7) {
                    currentWeek.add(0)
                }
                weeks.add(currentWeek)
            }

            MonthData(
                id = month.ordinal + 1,
                name = month.name.lowercase().replaceFirstChar { it.uppercase() },
                month = month,
                year = year,
                weeks = weeks
            )
        }
    }

    fun getCurrentMonthIndex(): Int {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).month.ordinal
    }
}