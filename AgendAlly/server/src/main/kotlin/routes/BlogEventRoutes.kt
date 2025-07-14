package com.example.routes

import com.example.*
import com.example.services.BlogEventsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import services.AuthMiddleware

fun Route.eventsRoutes() {
    val blogEventsService = BlogEventsService()
    val authMiddleware = AuthMiddleware()

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

        post {
            try {
                // 1. Verificar autenticación
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Token requerido"))
                    return@post
                }

                val token = authHeader.removePrefix("Bearer ")
                val authResult = authMiddleware.authenticateUser(token)

                if (authResult == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Token inválido"))
                    return@post
                }

                // 2. Verificar permisos
                if (!authResult.permissions.canCreateEvents) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Sin permisos para crear eventos"))
                    return@post
                }

                // 3. Validar que el usuario tenga organización
                if (authResult.user.organizationId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Usuario debe tener organización asignada"))
                    return@post
                }

                // 4. Recibir datos del evento
                val request = call.receive<CreateEventRequest>()

                // 5. Validar datos básicos
                if (request.title.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "El título es obligatorio"))
                    return@post
                }

                // 6. Verificar que el evento pertenezca a la organización del usuario
                if (request.organizationId != authResult.user.organizationId) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Solo puedes crear eventos en tu organización"))
                    return@post
                }

                // 7. Crear evento
                val eventId = blogEventsService.createEvent(request, authResult.user.id)

                if (eventId == null) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Error creando evento"))
                    return@post
                }

                // 8. Obtener evento creado
                val createdEvent = blogEventsService.getEventById(eventId)

                val response = CreateEventResponse(
                    success = true,
                    message = "Evento creado exitosamente",
                    eventId = eventId,
                    event = createdEvent
                )

                call.respond(HttpStatusCode.Created, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error creando evento: ${e.message}")
                )
            }
        }

        // PUT /api/events/{id} - Actualizar evento
        put("/{id}") {
            try {
                // 1. Verificar autenticación
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Token requerido"))
                    return@put
                }

                val token = authHeader.removePrefix("Bearer ")
                val authResult = authMiddleware.authenticateUser(token)

                if (authResult == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Token inválido"))
                    return@put
                }

                // 2. Obtener ID del evento
                val eventId = call.parameters["id"]?.toIntOrNull()
                if (eventId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID inválido"))
                    return@put
                }

                // 3. Verificar que el evento existe
                val existingEvent = blogEventsService.getEventById(eventId)
                if (existingEvent == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Evento no encontrado"))
                    return@put
                }

                // 4. Verificar permisos (solo puede editar eventos de su organización)
                if (existingEvent.organizationId != authResult.user.organizationId) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Solo puedes editar eventos de tu organización"))
                    return@put
                }

                // 5. Recibir datos de actualización
                val request = call.receive<UpdateEventRequest>()

                // 6. Validar datos
                if (request.title.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "El título es obligatorio"))
                    return@put
                }

                // 7. Actualizar evento
                val success = blogEventsService.updateEvent(eventId, request)

                if (!success) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Error actualizando evento"))
                    return@put
                }

                // 8. Obtener evento actualizado
                val updatedEvent = blogEventsService.getEventById(eventId)

                val response = UpdateEventResponse(
                    success = true,
                    message = "Evento actualizado exitosamente",
                    event = updatedEvent
                )

                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error actualizando evento: ${e.message}")
                )
            }
        }

        // DELETE /api/events/{id} - Eliminar evento (soft delete)
        delete("/{id}") {
            try {
                // 1. Verificar autenticación
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Token requerido"))
                    return@delete
                }

                val token = authHeader.removePrefix("Bearer ")
                val authResult = authMiddleware.authenticateUser(token)

                if (authResult == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Token inválido"))
                    return@delete
                }

                // 2. Obtener ID del evento
                val eventId = call.parameters["id"]?.toIntOrNull()
                if (eventId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID inválido"))
                    return@delete
                }

                // 3. Verificar que el evento existe
                val existingEvent = blogEventsService.getEventById(eventId)
                if (existingEvent == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Evento no encontrado"))
                    return@delete
                }

                // 4. Verificar permisos
                if (existingEvent.organizationId != authResult.user.organizationId) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Solo puedes eliminar eventos de tu organización"))
                    return@delete
                }

                // 5. Eliminar evento (soft delete)
                val success = blogEventsService.deleteEvent(eventId)

                if (!success) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Error eliminando evento"))
                    return@delete
                }

                val response = DeleteEventResponse(
                    success = true,
                    message = "Evento eliminado exitosamente"
                )

                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error eliminando evento: ${e.message}")
                )
            }
        }
    }
}