package routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File


fun Route.staticFilesRoutes() {

    // Servir archivos desde resources/images (como en Android)
    static("/static/images") {
        resources("images")
    }

    // Otros archivos estáticos
    static("/static") {
        resources("static")
    }
}