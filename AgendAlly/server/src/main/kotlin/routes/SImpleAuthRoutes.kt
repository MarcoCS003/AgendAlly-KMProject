// Reemplazar: src/main/kotlin/routes/SimpleAuthRoutes.kt
package routes



import com.example.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import services.AuthMiddleware
import services.AuthAdapter
import services.AuthResult
import services.UserPermissions
import java.time.LocalDateTime

// ✨ MODELOS PARA RESPUESTAS SERIALIZABLES
@Serializable
data class StatusResponse(
    val firebase_initialized: Boolean,
    val environment: String,
    val auth_flow: String,
    val client_types: List<String>
)

@Serializable
data class ClientTypeInfo(
    val role: String,
    val description: String,
    val permissions: List<String>
)

@Serializable
data class ClientInfoResponse(
    val client_types: Map<String, ClientTypeInfo>,
    val flow: List<String>
)

@Serializable
data class HelpResponse(
    val message: String,
    val test_endpoints: Map<String, String>,
    val example_test_request: ExampleRequest
)

@Serializable
data class ExampleRequest(
    val url: String,
    val body: LoginRequest
)

fun Route.authRoutes() {
    val authMiddleware = AuthMiddleware()

    route("/api/auth") {
        post("/test-login") {
            try {
                val request = call.receive<LoginRequest>()

                // Simular respuesta directa sin AuthResult complejo
                val mockResponse = LoginResponse(
                    success = true,
                    user = UserResponse(
                        id = 999,
                        firebaseUid = "test_user_123",
                        email = request.idToken, // Usar el "token" como email para pruebas
                        name = "Usuario de Prueba",
                        profilePicture = null,
                        role = "ADMIN",
                        organizationId = null,
                        createdAt = LocalDateTime.now().toString(),
                        lastLoginAt = LocalDateTime.now().toString()
                    ),
                    organization = null,
                    requiresOrganizationSetup = true,
                    message = "Debe configurar su organización"
                )

                call.respond(HttpStatusCode.OK, mockResponse)

            } catch (e: Exception) {
                println("❌ Error en test login: ${e.message}")
                e.printStackTrace() // Para ver el error completo
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error en prueba: ${e.message}")
                )
            }
        }
        // POST /api/auth/login - Login con Firebase token
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                if (request.idToken.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "Token de Firebase requerido")
                    )
                    return@post
                }

                // Usar tu AuthMiddleware existente
                val authResult = authMiddleware.authenticateUser(request.idToken)

                if (authResult == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(error = "Token inválido o expirado")
                    )
                    return@post
                }

                // Convertir a formato de respuesta
                val response = AuthAdapter.convertToLoginResponse(authResult)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                println("❌ Error en login: ${e.message}")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = "Error interno del servidor ${e.message}")
                )
            }
        }

        // GET /api/auth/status - Estado de Firebase
        get("/status") {
            try {
                val firebaseReady = FirebaseConfig.isReady()

                val response = StatusResponse(
                    firebase_initialized = firebaseReady,
                    environment = "development",
                    auth_flow = "Desktop → Firebase → Backend",
                    client_types = listOf("DESKTOP_ADMIN", "ANDROID_STUDENT", "WEB_ADMIN")
                )

                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "Firebase no inicializado: ${e.message}"
                    )
                )
            }
        }

        // GET /api/auth/client-info - Información sobre tipos de cliente
        get("/client-info") {
            val response = ClientInfoResponse(
                client_types = mapOf(
                    "DESKTOP_ADMIN" to ClientTypeInfo(
                        role = "SUPER_ADMIN",
                        description = "App desktop para administradores",
                        permissions = listOf("Crear eventos", "Gestionar canales", "Gestionar organización")
                    ),
                    "ANDROID_STUDENT" to ClientTypeInfo(
                        role = "STUDENT",
                        description = "App móvil para estudiantes",
                        permissions = listOf("Ver eventos", "Suscribirse a canales")
                    ),
                    "WEB_ADMIN" to ClientTypeInfo(
                        role = "ADMIN",
                        description = "Dashboard web para administradores",
                        permissions = listOf("Gestión completa", "Reportes avanzados")
                    )
                ),
                flow = listOf(
                    "1. App autentica con Firebase Auth",
                    "2. App envía idToken al backend",
                    "3. Backend valida token y retorna usuario/organización",
                    "4. App maneja estado basado en respuesta"
                )
            )

            call.respond(HttpStatusCode.OK, response)
        }
    }

    // 🛠️ RUTAS DE DESARROLLO (solo en development)
    if (System.getProperty("ENVIRONMENT") == "development")  {
        route("/api/dev/auth") {

            // POST /api/dev/auth/test-login - Login de prueba SIN Firebase
            post("/test-login") {
                try {
                    val request = call.receive<LoginRequest>()

                    // Simular usuario de prueba
                    val mockUser = com.example.User(
                        id = 999,
                        googleId = "test_user_123",
                        email = request.idToken, // Usar el "token" como email para pruebas
                        name = "Usuario de Prueba",
                        profilePicture = null,
                        role = com.example.UserRole.ADMIN,
                        isActive = true,
                        notificationsEnabled = true,
                        syncEnabled = false,
                        createdAt = java.time.LocalDateTime.now().toString(),
                        lastLoginAt = java.time.LocalDateTime.now().toString()
                    )

                    val mockPermissions = UserPermissions(
                        role = UserRole.ADMIN,
                        canCreateEvents = true,
                        canManageChannels = true,
                        canManageOrganizations = true,
                        requiresOrganization = false
                    )

                    // Crear mock AuthResult simplificado
                    val mockAuthResult = AuthResult(
                        user = mockUser,
                        permissions = mockPermissions,
                        firebaseToken = null!! // Solo para pruebas
                    )

                    // Usar el adapter para convertir
                    val response = AuthAdapter.convertToLoginResponse(mockAuthResult)

                    call.respond(HttpStatusCode.OK, response)

                } catch (e: Exception) {
                    println("❌ Error en test login: ${e.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(error = "Error en prueba: ${e.message}")
                    )
                }
            }

            get("/help") {
                val response = HelpResponse(
                    message = "Ayuda para desarrollo de autenticación",
                    test_endpoints = mapOf(
                        "test_login" to "POST /api/dev/auth/test-login",
                        "status" to "GET /api/auth/status",
                        "client_info" to "GET /api/auth/client-info"
                    ),
                    example_test_request = ExampleRequest(
                        url = "POST /api/dev/auth/test-login",
                        body = LoginRequest(
                            idToken = "test@ejemplo.com",
                            clientType = "DESKTOP_ADMIN"
                        )
                    )
                )

                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}