package routes

import com.example.ChannelsResponse
import com.example.ErrorResponse
import com.example.OrganizationSearchResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import services.OrganizationService


/**
 * ✅ RUTAS DE ORGANIZACIONES
 */
fun Route.organizationRoutes() {
    val organizationService = OrganizationService()

    route("/api/organizations") {

        // GET /api/organizations - Obtener todas las organizaciones
        get {
            try {
                val organizations = organizationService.getAllOrganizations()
                val response = OrganizationSearchResponse(
                    organizations = organizations,
                    total = organizations.size
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo organizaciones: ${e.message}")
                )
            }
        }

        // GET /api/organizations/search?q=query - Buscar organizaciones
        get("/search") {
            try {
                val query = call.request.queryParameters["q"] ?: ""
                if (query.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Query requerido"))
                    return@get
                }

                val organizations = organizationService.searchOrganizations(query)
                call.respond(HttpStatusCode.OK, organizations)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error buscando organizaciones: ${e.message}")
                )
            }
        }

        // GET /api/organizations/{id} - Obtener organización por ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID inválido"))
                    return@get
                }

                val organization = organizationService.getOrganizationById(id)

                if (organization != null) {
                    call.respond(HttpStatusCode.OK, organization)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Organización no encontrada"))
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo organización: ${e.message}")
                )
            }
        }

        // GET /api/organizations/stats - Obtener estadísticas
        get("/stats") {
            try {
                val stats = organizationService.getOrganizationStats()
                call.respond(HttpStatusCode.OK, stats)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo estadísticas: ${e.message}")
                )
            }
        }

        // GET /api/organizations/{id}/channels - Obtener canales de una organización
        get("/{id}/channels") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID inválido"))
                    return@get
                }

                val channels = organizationService.getChannelsByOrganization(id)
                val response = ChannelsResponse(
                    channels = channels,
                    total = channels.size,
                    organizationId = id
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error obteniendo canales: ${e.message}")
                )
            }
        }
    }
}