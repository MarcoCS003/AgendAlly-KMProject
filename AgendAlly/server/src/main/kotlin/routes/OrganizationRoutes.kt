package routes

import com.example.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import services.AuthMiddleware
import services.OrganizationService


/**
 * ✅ RUTAS DE ORGANIZACIONES
 */
fun Route.organizationRoutes() {
    val organizationService = OrganizationService()

    route("/api/organizations") {
        get("/me") {
            try {
                val authHeader = call.request.headers["Authorization"]
                println("🔍 GET /api/organizations/me")
                println("   Auth Header: ${authHeader?.take(100)}...")

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    println("❌ No token provided")
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Token requerido"))
                    return@get
                }

                val token = authHeader.removePrefix("Bearer ")

                val authMiddleware = AuthMiddleware()
                val authResult = authMiddleware.authenticateUser(token)

                if (authResult == null) {
                    println("❌ Invalid token")
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = "Token inválido"))
                    return@get
                }

                // 🔍 DEBUG: Mostrar datos del usuario autenticado
                println("✅ Usuario autenticado:")
                println("   ID: ${authResult.user.id}")
                println("   Email: ${authResult.user.email}")
                println("   Nombre: ${authResult.user.name}")
                println("   Organization ID: ${authResult.user.organizationId}")

                if (authResult.user.organizationId == null) {
                    println("❌ Usuario sin organización asignada")
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Usuario no tiene organización asignada"))
                    return@get
                }

                val organization = organizationService.getOrganizationById(authResult.user.organizationId)

                if (organization == null) {
                    println("❌ Organización no encontrada: ${authResult.user.organizationId}")
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Organización no encontrada"))
                    return@get
                }

                // 🔍 DEBUG: Mostrar organización encontrada
                println("✅ Organización encontrada:")
                println("   ID: ${organization.organizationID}")
                println("   Nombre: ${organization.name}")
                println("   Acrónimo: ${organization.acronym}")

                call.respond(HttpStatusCode.OK, organization)

            } catch (e: Exception) {
                println("💥 Error en GET /organizations/me: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Error obteniendo organización: ${e.message}"))
            }
        }
        put("/me") {
            try {
                // 1. Verificar autenticación
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(error = "Token requerido")
                    )
                    return@put
                }

                val token = authHeader.removePrefix("Bearer ")

                // 2. Obtener usuario del token
                val authMiddleware = AuthMiddleware()
                val authResult = authMiddleware.authenticateUser(token)

                if (authResult == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(error = "Token inválido")
                    )
                    return@put
                }

                // 3. Verificar permisos (solo ADMIN y SUPER_ADMIN)
                if (authResult.permissions.role !in listOf(UserRole.ADMIN, UserRole.SUPER_ADMIN)) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse(error = "Sin permisos para editar organización")
                    )
                    return@put
                }

                // 4. Verificar que el usuario tenga una organización asignada
                if (authResult.user.id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "Usuario no tiene organización asignada")
                    )
                    return@put
                }

                // 5. Recibir datos de actualización
                val updateRequest = call.receive<UpdateOrganizationRequest>()

                // 6. Validar datos básicos
                if (updateRequest.name.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "El nombre es obligatorio")
                    )
                    return@put
                }

                if (updateRequest.acronym.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "El acrónimo es obligatorio")
                    )
                    return@put
                }

                // 7. Actualizar organización
                val updatedOrganization = organizationService.updateOrganization(
                    organizationId = authResult.user.id,
                    updateRequest = updateRequest
                )

                if (updatedOrganization == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(error = "Error actualizando organización")
                    )
                    return@put
                }

                call.respond(HttpStatusCode.OK, updatedOrganization)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error actualizando organización: ${e.message}")
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