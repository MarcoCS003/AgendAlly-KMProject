package com.example.routes

import com.example.*
import com.example.services.BlogEventsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.eventsRoutes() {
    val blogEventsService = BlogEventsService()

    route("/api/events") {

        // GET /api/events - Obtener todos los eventos
        get {
            try {
                val events = blogEventsService.getAllEvents()
                val response = BlogEventsResponse(
                    events = events,
                    total = events.size,
                    organizationInfo = null
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo eventos: ${e.message}")
                )
            }
        }

        // GET /api/events/search?q=query - Buscar eventos
        get("/search") {
            try {
                val query = call.request.queryParameters["q"]
                if (query.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Query requerido"))
                    return@get
                }

                val events = blogEventsService.searchEvents(query)
                val response = EventSearchResponse(
                    events = events,
                    total = events.size,
                    query = query
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error buscando eventos: ${e.message}")
                )
            }
        }

        // GET /api/events/{id} - Obtener evento por ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID inválido"))
                    return@get
                }

                val event = blogEventsService.getEventById(id)

                if (event != null) {
                    call.respond(HttpStatusCode.OK, event)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Evento no encontrado"))
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo evento: ${e.message}")
                )
            }
        }

        // GET /api/events/organization/{organizationId} - Eventos por organización
        get("/organization/{organizationId}") {
            try {
                val organizationId = call.parameters["organizationId"]?.toIntOrNull()
                if (organizationId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID de organización inválido"))
                    return@get
                }

                val response = blogEventsService.getEventsByOrganization(organizationId)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo eventos de organización: ${e.message}")
                )
            }
        }

        // GET /api/events/channel/{channelId} - Eventos por canal
        get("/channel/{channelId}") {
            try {
                val channelId = call.parameters["channelId"]?.toIntOrNull()
                if (channelId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID de canal inválido"))
                    return@get
                }

                val events = blogEventsService.getEventsByChannel(channelId)
                val response = BlogEventsResponse(
                    events = events,
                    total = events.size,
                    organizationInfo = null
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo eventos de canal: ${e.message}")
                )
            }
        }

        // GET /api/events/upcoming - Eventos próximos
        get("/upcoming") {
            try {
                val events = blogEventsService.getUpcomingEvents()
                val response = UpcomingEventsResponse(
                    events = events,
                    total = events.size,
                    description = "Próximos eventos en las siguientes semanas"
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo eventos próximos: ${e.message}")
                )
            }
        }

        // GET /api/events/category/{category} - Eventos por categoría
        get("/category/{category}") {
            try {
                val category = call.parameters["category"] ?: ""
                if (category.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Categoría requerida"))
                    return@get
                }

                val events = blogEventsService.getEventsByCategory(category)
                val response = EventsByCategoryResponse(
                    events = events,
                    total = events.size,
                    category = category
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo eventos por categoría: ${e.message}")
                )
            }
        }

        // GET /api/events/date-range?start=YYYY-MM-DD&end=YYYY-MM-DD - Eventos en rango de fechas
        get("/date-range") {
            try {
                val startDate = call.request.queryParameters["start"]
                val endDate = call.request.queryParameters["end"]

                if (startDate.isNullOrBlank() || endDate.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Fechas start y end requeridas"))
                    return@get
                }

                val events = blogEventsService.getEventsByDateRange(startDate, endDate)
                val response = BlogEventsResponse(
                    events = events,
                    total = events.size,
                    organizationInfo = null
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo eventos por rango de fechas: ${e.message}")
                )
            }
        }

        // GET /api/events/stats - Estadísticas de eventos
        get("/stats") {
            try {
                val stats = blogEventsService.getEventStats()
                call.respond(HttpStatusCode.OK, stats)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo estadísticas de eventos: ${e.message}")
                )
            }
        }
    }
}