package routes

import com.example.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import services.AuthMiddleware
import services.FileUploadService

fun Route.fileUploadRoutes() {
    val fileUploadService = FileUploadService()
    val authMiddleware = AuthMiddleware()

    // ===== RUTAS PÚBLICAS (Sin autenticación) =====

    // GET /api/images/{filename} - Servir imagen (PÚBLICO)
    get("/api/images/{filename}") {
        try {
            val filename = call.parameters["filename"]
            if (filename.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Nombre de archivo requerido")
                return@get
            }

            val file = fileUploadService.getImageFile(filename)
            if (file == null) {
                call.respond(HttpStatusCode.NotFound, "Imagen no encontrada")
                return@get
            }

            // Determinar content type
            val contentType = when (file.extension.lowercase()) {
                "jpg", "jpeg" -> ContentType.Image.JPEG
                "png" -> ContentType.Image.PNG
                "gif" -> ContentType.Image.GIF
                "webp" -> ContentType("image", "webp")
                else -> ContentType.Application.OctetStream
            }

            call.response.header(HttpHeaders.ContentType, contentType.toString())
            call.response.header(HttpHeaders.CacheControl, "public, max-age=31536000") // Cache 1 año
            call.respondFile(file)

        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error sirviendo imagen: ${e.message}")
        }
    }

    // GET /api/images - Listar imágenes disponibles (PÚBLICO)
    get("/api/images") {
        try {
            val images = fileUploadService.listImages()
            val response = ListImagesResponse(
                images = images,
                total = images.size
            )
            call.respond(HttpStatusCode.OK, response)

        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(error = "Error listando imágenes: ${e.message}")
            )
        }
    }

    route("/api") {
        // ===== RUTAS PROTEGIDAS (Requieren autenticación) =====

        // POST /api/upload/image - Subir imagen (PROTEGIDO)
        post("/upload/image") {
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
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Sin permisos para subir archivos"))
                    return@post
                }

                // 3. Procesar upload
                val multipart = call.receiveMultipart()
                val result = fileUploadService.uploadImage(multipart)

                when (result) {
                    is UploadResult.Success -> {
                        val response = UploadResponse(
                            success = true,
                            message = "Imagen subida exitosamente",
                            fileName = result.fileName,
                            url = result.url,
                            size = result.size
                        )
                        call.respond(HttpStatusCode.Created, response)
                    }
                    is UploadResult.Error -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            UploadResponse(success = false, message = result.message)
                        )
                    }

                    else -> {}
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    UploadResponse(success = false, message = "Error subiendo imagen: ${e.message}")
                )
            }
        }

        // DELETE /api/images/{filename} - Eliminar imagen (PROTEGIDO)
        delete("/images/{filename}") {
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

                // 2. Verificar permisos
                if (!authResult.permissions.canCreateEvents) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Sin permisos para eliminar archivos"))
                    return@delete
                }

                // 3. Eliminar imagen
                val filename = call.parameters["filename"]
                if (filename.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Nombre de archivo requerido"))
                    return@delete
                }

                val success = fileUploadService.deleteImage(filename)
                val response = DeleteImageResponse(
                    success = success,
                    message = if (success) "Imagen eliminada exitosamente" else "Error eliminando imagen"
                )

                val statusCode = if (success) HttpStatusCode.OK else HttpStatusCode.NotFound
                call.respond(statusCode, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    DeleteImageResponse(success = false, message = "Error eliminando imagen: ${e.message}")
                )
            }
        }
    }
}