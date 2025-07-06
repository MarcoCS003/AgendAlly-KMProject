package routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File


fun Route.staticFilesRoutes() {

    // Servir archivos desde resources/images (como en Android)
    static("/images") {
        resources("images")
    }

    // Ruta alternativa para servir otros recursos
    static("/static") {
        resources("static")
    }

    // Ruta manual para más control (opcional)
    get("/api/images/{filename}") {
        val filename = call.parameters["filename"]
        if (filename == null) {
            call.respond(HttpStatusCode.BadRequest, "Nombre de archivo requerido")
            return@get
        }

        val file = File("static/images/$filename")
        if (!file.exists()) {
            call.respond(HttpStatusCode.NotFound, "Imagen no encontrada")
            return@get
        }

        // Determinar tipo de contenido basado en extensión
        val contentType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> ContentType.Image.JPEG
            "png" -> ContentType.Image.PNG
            "gif" -> ContentType.Image.GIF
            "svg" -> ContentType.Image.SVG
            else -> ContentType.Application.OctetStream
        }
        
    }
}