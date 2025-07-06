package com.academically


import com.example.ClientType
import com.example.routes.eventsRoutes
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import database.initDatabase
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import routes.*
import java.io.FileInputStream

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val host = "0.0.0.0"

    println("🚀 Academic Ally Backend - PRODUCTION READY")
    println("📡 Host: $host")
    println("🔌 Port: $port")
    println("🔥 Auth: Firebase Direct")
    println("🏢 Organizations: Auto-assignment by email")

    // 🖼️ Verificar carpeta de imágenes al inicio
    checkStaticFilesSetup()
    System.setProperty("ENVIRONMENT", "development")
    initializeFirebase()

    embeddedServer(Netty, port = port, host = host, module = Application::module).start(wait = true)
}

fun checkStaticFilesSetup() {
    println("\n🖼️ === VERIFICACIÓN DE ARCHIVOS EN RESOURCES ===")

    // Verificar recursos en classpath
    val resourcesAvailable = object {}.javaClass.getResource("/images") != null
    println("📁 Directorio /resources/images existe: $resourcesAvailable")

    if (resourcesAvailable) {
        // Verificar las imágenes específicas de la BD
        val requiredImages = listOf("InnovaTecNM.jpg", "conferencia_ia.jpg", "concurso_programacion.jpg")
        println("\n✅ Verificación de imágenes requeridas en resources:")
        requiredImages.forEach { imageName ->
            val resource = object {}.javaClass.getResource("/images/$imageName")
            val exists = resource != null
            println("   - $imageName: ${if (exists) "✅ EXISTE" else "❌ FALTA"}")
            if (exists) {
                try {
                    val size = resource!!.openStream().available()
                    println("     Tamaño: $size bytes")
                } catch (e: Exception) {
                    println("     Error leyendo: ${e.message}")
                }
            }
        }
    } else {
        println("❌ Directorio de imágenes no encontrado en resources")
        println("💡 Crea la carpeta: src/main/resources/images/")
    }
    println("===========================================\n")
}

fun initializeFirebase() {
    try {
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount = FileInputStream("firebase-service-account.json")

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            FirebaseApp.initializeApp(options)
            println("✅ Firebase inicializado correctamente")
        }
    } catch (e: Exception) {
        println("⚠️ Advertencia: Firebase no inicializado - ${e.message}")
        println("📁 Asegúrate de tener firebase-service-account.json en la raíz del proyecto")
    }
}

fun Application.module() {
    configureFirebase()
    configureDatabase()
    configureSerialization()
    configureCORS()
    configureHeaders()
    configureRouting()
}

fun Application.configureFirebase() {
    try {
        println("🔥 Inicializando Firebase...")
        System.setProperty("ENVIRONMENT", "development")
        FirebaseConfig.initialize()
        println("✅ Firebase listo")
    } catch (e: Exception) {
        println("⚠️ Firebase error: ${e.message} - Continuando...")
    }
}



fun Application.configureDatabase() {
    println("📊 Inicializando BD...")
    initDatabase()
    println("✅ BD lista")
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-Client-Type")
        anyHost() // Para desarrollo
    }
}

fun Application.configureHeaders() {
    install(DefaultHeaders) {
        header("X-Engine", "Academic Ally")
        header("X-Version", "2.0.0")
    }
}

fun Application.configureRouting() {
    routing {
        // ===== RUTAS PÚBLICAS =====
        get("/") {
            call.respondText("""
                {
                    "message": "🎓 Academic Ally Backend API",
                    "version": "2.0.0",
                    "status": "running",
                    "endpoints": {
                        "public": [
                            "GET /health",
                            "GET /api/organizations", 
                            "GET /api/channels",
                            "GET /api/events",
                            "GET /api/auth/client-info",
                            "GET /images/{filename}",
                            "GET /static/{path}",
                            "GET /api/images/{filename}"
                        ],
                        "protected": [
                            "GET /api/auth/me (requires: Authorization + X-Client-Type)"
                        ]
                    },
                    "auth_flow": {
                        "step_1": "App autentica con Firebase Auth",
                        "step_2": "App obtiene idToken de Firebase",
                        "step_3": "App envía: Authorization: Bearer <idToken>, X-Client-Type: ANDROID_STUDENT|DESKTOP_ADMIN",
                        "step_4": "Backend valida y asigna permisos automáticamente"
                    },
                    "static_files": {
                        "images": "/images/{filename}",
                        "static": "/static/{path}",
                        "api_images": "/api/images/{filename}"
                    }
                }
            """.trimIndent(), ContentType.Application.Json)
        }

        get("/health") {
            call.respondText("""
                {
                    "status": "healthy",
                    "timestamp": ${System.currentTimeMillis()},
                    "firebase": "${if (FirebaseConfig.isReady()) "ready" else "not_configured"}",
                    "database": "connected",
                    "version": "2.0.0",
                    "static_files": "enabled"
                }
            """.trimIndent(), ContentType.Application.Json)
        }

        // ===== RUTAS DE DEBUG PARA IMÁGENES EN RESOURCES =====
        get("/debug/images") {
            val requiredImages = listOf("InnovaTecNM.jpg", "conferencia_ia.jpg", "concurso_programacion.jpg")
            val imagesList = requiredImages.map { imageName ->
                val resource = this::class.java.getResource("/images/$imageName")
                mapOf(
                    "name" to imageName,
                    "exists" to (resource != null),
                    "url" to "http://localhost:8080/images/$imageName",
                    "resource_path" to "/images/$imageName"
                )
            }

            call.respond(HttpStatusCode.OK, mapOf(
                "resources_directory_exists" to (this::class.java.getResource("/images") != null),
                "images_checked" to imagesList.size,
                "images" to imagesList
            ))
        }

        // ===== RUTAS MODULARES =====
        organizationRoutes()    // /api/organizations/*
        channelsRoutes()        // /api/channels/*
        eventsRoutes()          // /api/events/*
        authRoutes()            // /api/auth/*
        staticFilesRoutes()     // /images/*, /static/*, /api/images/*
    }
}