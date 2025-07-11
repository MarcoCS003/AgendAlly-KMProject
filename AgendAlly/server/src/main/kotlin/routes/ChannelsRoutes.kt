package routes

import com.example.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import services.*

/**
 * ✅ RUTAS DE CANALES ACTUALIZADAS - AUTH SIMPLIFICADA
 */
fun Route.channelsRoutes() {
    val channelsService = ChannelsService()

    route("/api/channels") {

        // GET /api/channels - Obtener todos los canales
        get {
            try {
                val organizationId = call.request.queryParameters["organizationId"]?.toIntOrNull()

                val response = when {
                    organizationId != null -> channelsService.getChannelsByOrganization(organizationId)
                    else -> channelsService.getAllChannels()
                }

                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo canales: ${e.message}")
                )
            }
        }

        // GET /api/channels/search?q=query - Buscar canales
        get("/search") {
            try {
                val query = call.request.queryParameters["q"]
                val organizationId = call.request.queryParameters["organizationId"]?.toIntOrNull()

                if (query.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Query requerido"))
                    return@get
                }

                val channels = channelsService.searchChannels(query, organizationId)
                call.respond(HttpStatusCode.OK, channels)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error buscando canales: ${e.message}")
                )
            }
        }

        // GET /api/channels/{id} - Obtener canal por ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID inválido"))
                    return@get
                }

                val channel = channelsService.getChannelById(id)

                if (channel != null) {
                    call.respond(HttpStatusCode.OK, channel)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Canal no encontrado"))
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo canal: ${e.message}")
                )
            }
        }


        // GET /api/channels/{id}/events - Obtener eventos de un canal
        get("/{id}/events") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID inválido"))
                    return@get
                }

                // TODO: Implementar en BlogEventsService
                call.respond(
                    HttpStatusCode.NotImplemented,
                    ErrorResponse(error = "Endpoint en desarrollo")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo eventos del canal: ${e.message}")
                )
            }
        }
    }
}