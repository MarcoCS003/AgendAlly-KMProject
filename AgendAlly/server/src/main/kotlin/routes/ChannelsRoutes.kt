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
    val authMiddleware = AuthMiddleware()

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
                if (!authResult.permissions.canManageChannels) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Sin permisos para crear canales"))
                    return@post
                }

                // 3. Verificar que el usuario tenga organización
                if (authResult.user.organizationId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Usuario debe tener organización asignada"))
                    return@post
                }

                // 4. Recibir datos del canal
                val request = call.receive<CreateChannelRequest>()

                // 5. Validar datos básicos
                if (request.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "El nombre es obligatorio"))
                    return@post
                }

                if (request.acronym.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "El acrónimo es obligatorio"))
                    return@post
                }

                // 6. Verificar que el canal pertenezca a la organización del usuario
                if (request.organizationId != authResult.user.organizationId) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Solo puedes crear canales en tu organización"))
                    return@post
                }

                // 7. Crear canal
                val channelId = channelsService.createChannel(request)

                if (channelId == null) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Error creando canal"))
                    return@post
                }

                // 8. Obtener canal creado
                val createdChannel = channelsService.getChannelById(channelId)

                val response = CreateChannelResponse(
                    success = true,
                    message = "Canal creado exitosamente",
                    channelId = channelId,
                    channel = createdChannel
                )

                call.respond(HttpStatusCode.Created, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error creando canal: ${e.message}")
                )
            }
        }

        // PUT /api/channels/{id} - Actualizar canal
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

                // 2. Obtener ID del canal
                val channelId = call.parameters["id"]?.toIntOrNull()
                if (channelId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID inválido"))
                    return@put
                }

                // 3. Verificar que el canal existe
                val existingChannel = channelsService.getChannelById(channelId)
                if (existingChannel == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Canal no encontrado"))
                    return@put
                }

                // 4. Verificar permisos (solo puede editar canales de su organización)
                if (existingChannel.organizationId != authResult.user.organizationId) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Solo puedes editar canales de tu organización"))
                    return@put
                }

                // 5. Recibir datos de actualización
                val request = call.receive<UpdateChannelRequest>()

                // 6. Validar datos
                if (request.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "El nombre es obligatorio"))
                    return@put
                }

                if (request.acronym.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "El acrónimo es obligatorio"))
                    return@put
                }

                // 7. Actualizar canal
                val success = channelsService.updateChannel(channelId, request)

                if (!success) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Error actualizando canal"))
                    return@put
                }

                // 8. Obtener canal actualizado
                val updatedChannel = channelsService.getChannelById(channelId)

                val response = UpdateChannelResponse(
                    success = true,
                    message = "Canal actualizado exitosamente",
                    channel = updatedChannel
                )

                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error actualizando canal: ${e.message}")
                )
            }
        }

        // DELETE /api/channels/{id} - Eliminar canal (soft delete)
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

                // 2. Obtener ID del canal
                val channelId = call.parameters["id"]?.toIntOrNull()
                if (channelId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "ID inválido"))
                    return@delete
                }

                // 3. Verificar que el canal existe
                val existingChannel = channelsService.getChannelById(channelId)
                if (existingChannel == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Canal no encontrado"))
                    return@delete
                }

                // 4. Verificar permisos
                if (existingChannel.organizationId != authResult.user.organizationId) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Solo puedes eliminar canales de tu organización"))
                    return@delete
                }

                // 5. Verificar que no sea un canal crítico
                if (existingChannel.acronym in listOf("GEN", "GENERAL")) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "No se puede eliminar el canal General"))
                    return@delete
                }

                // 6. Eliminar canal (soft delete)
                val success = channelsService.deleteChannel(channelId)

                if (!success) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Error eliminando canal"))
                    return@delete
                }

                val response = DeleteChannelResponse(
                    success = true,
                    message = "Canal eliminado exitosamente"
                )

                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error eliminando canal: ${e.message}")
                )
            }
        }
    }
}