package services

import com.example.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import database.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AuthMiddleware {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseService.getAuth()
    }

    /**
     * Valida token de Firebase y determina permisos según plataforma
     */
    fun validateTokenAndPermissions(
        idToken: String,
        clientType: ClientType
    ): AuthResult? {
        return try {
            println("🔍 VALIDANDO TOKEN...")
            println("   Client Type: $clientType")
            println("   Token (primeros 50 chars): ${idToken.take(50)}...")

            // 1. Validar token con Firebase
            val decodedToken = firebaseAuth.verifyIdToken(idToken, false) // No verificar audience automáticamente
            // Validar manualmente el audience

            if (decodedToken == null) {
                println("❌ Token verification failed")
                return null
            }
            println("✅ Token válido para: ${decodedToken.email}")

            // 2. Determinar permisos según plataforma y email
            val permissions = determinePermissions(clientType, decodedToken.email)
            println("✅ Permisos determinados: ${permissions.role}")

            // 3. Crear/actualizar usuario en BD si es necesario
            val (user, isNewUser) = ensureUserExists(decodedToken, permissions.role)
            println("✅ Usuario procesado: ${user.email}, Nuevo: $isNewUser")

            AuthResult(
                user = user,
                permissions = permissions,
                firebaseToken = decodedToken,
                isNewUser = isNewUser
            )

        } catch (e: FirebaseAuthException) {
            println("❌ Firebase Auth Error: ${e.message}")
            println("❌ Error Code: ${e.authErrorCode}")
            e.printStackTrace()
            null
        } catch (e: Exception) {
            println("❌ General Error: ${e.message}")
            e.printStackTrace()
            null
        }
    }


    private fun determinePermissions(clientType: ClientType, email: String): UserPermissions {
        return when (clientType) {
            ClientType.ANDROID_STUDENT -> {
                // 📱 Android = Siempre STUDENT (cualquier email)
                UserPermissions(
                    role = UserRole.STUDENT,
                    canCreateEvents = false,
                    canManageChannels = false,
                    canManageOrganizations = false,
                    requiresOrganization = false
                )
            }

            ClientType.DESKTOP_ADMIN, ClientType.WEB_ADMIN -> {
                // 💻 Desktop/Web = Siempre ADMIN (cualquier email)
                UserPermissions(
                    role = UserRole.ADMIN,
                    canCreateEvents = true,
                    canManageChannels = true,
                    canManageOrganizations = false, // Solo SUPER_ADMIN puede gestionar orgs
                    requiresOrganization = true  // Debe seleccionar organización
                )
            }

            else -> {
                // 🤷 Unknown = STUDENT por defecto
                UserPermissions(
                    role = UserRole.STUDENT,
                    canCreateEvents = false,
                    canManageChannels = false,
                    canManageOrganizations = false,
                    requiresOrganization = false
                )
            }
        }
    }

    /**
     * ✅ Crea usuario y lo asigna automáticamente a organización
     */
    private fun ensureUserExists(firebaseToken: FirebaseToken, role: UserRole): Pair<User, Boolean> {
        return transaction {
            val existingUser = Users.select { Users.googleId eq firebaseToken.uid }
                .singleOrNull()

            if (existingUser != null) {
                // ✅ USUARIO EXISTENTE - Solo actualizar info
                Users.update({ Users.googleId eq firebaseToken.uid }) {
                    it[name] = firebaseToken.name ?: firebaseToken.email
                    it[email] = firebaseToken.email
                    it[profilePicture] = firebaseToken.picture
                    it[lastLoginAt] = LocalDateTime.now()
                }

                val user = mapRowToUser(Users.select { Users.googleId eq firebaseToken.uid }.single())
                Pair(user, false) // false = NO es usuario nuevo

            } else {
                // ✅ USUARIO COMPLETAMENTE NUEVO
                val userId = Users.insert {
                    it[googleId] = firebaseToken.uid
                    it[email] = firebaseToken.email
                    it[name] = firebaseToken.name ?: firebaseToken.email
                    it[profilePicture] = firebaseToken.picture
                    it[this.role] = role.name
                    it[isActive] = true
                    it[createdAt] = LocalDateTime.now()
                    it[lastLoginAt] = LocalDateTime.now()
                    it[notificationsEnabled] = true
                    it[syncEnabled] = false
                } get Users.id

                val newUser = mapRowToUser(Users.select { Users.id eq userId }.single())
                Pair(newUser, true) // true = SÍ es usuario nuevo
            }
        }
    }

    private fun mapRowToUser(row: ResultRow): User {
        return User(
            id = row[Users.id],
            googleId = row[Users.googleId],
            email = row[Users.email],
            name = row[Users.name],
            profilePicture = row[Users.profilePicture],
            role = UserRole.valueOf(row[Users.role]),
            isActive = row[Users.isActive],
            notificationsEnabled = row[Users.notificationsEnabled],
            syncEnabled = row[Users.syncEnabled],
            createdAt = row[Users.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            lastLoginAt = row[Users.lastLoginAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            lastSyncAt = row[Users.lastSyncAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    fun authenticateUser(idToken: String): AuthResult? {
        return try {
            println("🔍 ===== DEBUG AUTHMIDDLEWARE =====")
            println("🔑 Token recibido: ${idToken.take(50)}...")

            // 1. Verificar estado de Firebase
            println("🔥 Estado Firebase:")
            println("   - App inicializada: ${FirebaseApp.getApps().isNotEmpty()}")
            if (FirebaseApp.getApps().isNotEmpty()) {
                val app = FirebaseApp.getInstance()
                println("   - Project ID: ${app.options.projectId}")
                println("   - Firebase Auth: ${FirebaseAuth.getInstance(app) != null}")
            }

            // 2. Intentar verificar token
            println("🔍 Verificando token con Firebase...")
            val decodedToken = try {
                FirebaseAuth.getInstance().verifyIdToken(idToken)
            } catch (e: Exception) {
                println("❌ Error en verifyIdToken: ${e.message}")
                println("❌ Tipo de excepción: ${e.javaClass.simpleName}")
                if (e is FirebaseAuthException) {
                    println("❌ Código de error Firebase: ${e.errorCode}")
                    println("❌ Mensaje Firebase: ${e.message}")
                }
                throw e
            }

            println("✅ Token verificado exitosamente:")
            println("   - UID: ${decodedToken.uid}")
            println("   - Email: ${decodedToken.email}")
            println("   - Issuer: ${decodedToken.issuer}")

            // 3. Determinar permisos
            println("🔧 Determinando permisos para DESKTOP_ADMIN...")
            val permissions = UserPermissions(
                role = UserRole.ADMIN,
                canCreateEvents = true,
                canManageChannels = true,
                canManageOrganizations = false,
                requiresOrganization = true
            )
            println("   - Role asignado: ${permissions.role}")

            // 4. Crear/actualizar usuario
            println("👤 Creando/actualizando usuario...")
            val (user) = ensureUserExists(decodedToken, UserRole.ADMIN)
            println("   - Usuario ID: ${user.id}")
            println("   - Email: ${user.email}")

            val result = AuthResult(
                user = user,
                permissions = permissions,
                firebaseToken = decodedToken
            )

            println("✅ AuthResult creado exitosamente")
            println("🔍 ===== FIN DEBUG AUTHMIDDLEWARE =====")

            result

        } catch (e: FirebaseAuthException) {
            println("❌ ===== ERROR FIREBASE AUTH =====")
            println("❌ Código: ${e.errorCode}")
            println("❌ Mensaje: ${e.message}")
            println("❌ Causa: ${e.cause?.message}")
            println("❌ ===== FIN ERROR =====")
            null
        } catch (e: Exception) {
            println("❌ ===== ERROR GENERAL =====")
            println("❌ Tipo: ${e.javaClass.simpleName}")
            println("❌ Mensaje: ${e.message}")
            println("❌ Stack trace:")
            e.printStackTrace()
            println("❌ ===== FIN ERROR =====")
            null
        }
    }

}

/**
 * Resultado de autenticación
 */
data class AuthResult(
    val user: User,
    val permissions: UserPermissions,
    val firebaseToken: FirebaseToken,
    val isNewUser: Boolean = false  // ✅ NUEVO CAMPO
)

/**
 * Permisos de usuario según plataforma
 */
data class UserPermissions(
    val role: UserRole,
    val canCreateEvents: Boolean,
    val canManageChannels: Boolean,
    val canManageOrganizations: Boolean,
    val requiresOrganization: Boolean
)

/**
 * ✅ MIDDLEWARE PARA RUTAS PROTEGIDAS
 */
fun Route.requireAuth(build: Route.() -> Unit) {
    intercept(ApplicationCallPipeline.Call) {
        val authHeader = call.request.headers["Authorization"]
        val clientTypeHeader = call.request.headers["X-Client-Type"]

        // Validar headers
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(error = "Token de Firebase requerido: Authorization: Bearer <token>")
            )
            finish()
            return@intercept
        }

        val clientType = try {
            ClientType.valueOf(clientTypeHeader ?: "UNKNOWN")
        } catch (e: Exception) {
            ClientType.UNKNOWN
        }

        // Extraer token
        val idToken = authHeader.substring(7)

        // Validar con Firebase y determinar permisos
        val authMiddleware = AuthMiddleware()
        val authResult = authMiddleware.validateTokenAndPermissions(idToken, clientType)

        if (authResult == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(error = "Token inválido o permisos insuficientes")
            )
            finish()
            return@intercept
        }

        // Agregar al contexto para uso en rutas
        call.attributes.put(AttributeKey<AuthResult>("authResult"), authResult)
        proceed()
    }

    build()
}

/**
 * Middleware que requiere permisos específicos
 */
fun Route.requirePermission(
    canCreateEvents: Boolean = false,
    canManageChannels: Boolean = false,
    canManageOrganizations: Boolean = false,
    build: Route.() -> Unit
) {
    requireAuth {
        intercept(ApplicationCallPipeline.Call) {
            val authResult = call.attributes[AttributeKey<AuthResult>("authResult")]
            val permissions = authResult.permissions

            val hasPermission = when {
                canCreateEvents && !permissions.canCreateEvents -> false
                canManageChannels && !permissions.canManageChannels -> false
                canManageOrganizations && !permissions.canManageOrganizations -> false
                else -> true
            }

            if (!hasPermission) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(error = "Permisos insuficientes para esta operación")
                )
                finish()
                return@intercept
            }

            proceed()
        }

        build()
    }
}

/**
 * Extensión para obtener resultado de autenticación desde el contexto
 */
fun ApplicationCall.getAuthResult(): AuthResult? {
    return try {
        attributes[AttributeKey<AuthResult>("authResult")]
    } catch (e: Exception) {
        null
    }
}

/**
 * Extensión para obtener usuario autenticado
 */
fun ApplicationCall.getAuthenticatedUser(): User? {
    return getAuthResult()?.user
}


/**
 * Extensión para obtener permisos del usuario
 */
fun ApplicationCall.getUserPermissions(): UserPermissions? {
    return getAuthResult()?.permissions
}

/**
 * Middleware solo para desarrollo - permite bypass de autenticación
 */
fun Route.developmentBypass(build: Route.() -> Unit) {
    val isDevelopment = System.getenv("ENVIRONMENT") == "development"

    if (isDevelopment) {
        intercept(ApplicationCallPipeline.Call) {
            val bypassHeader = call.request.headers["X-Dev-Bypass"]

            if (bypassHeader == "true") {
                println("🚧 DESARROLLO - Bypass de autenticación activado")
                // Crear usuario mock para desarrollo
                val mockUser = User(
                    id = 1,
                    googleId = "dev_user_123",
                    email = "dev@test.com",
                    name = "Usuario Desarrollo",
                    role = UserRole.STUDENT,
                    isActive = true,
                    notificationsEnabled = true,
                    syncEnabled = false,
                    createdAt = "2025-01-01T00:00:00"
                )

                val mockPermissions = UserPermissions(
                    role = UserRole.STUDENT,
                    canCreateEvents = false,
                    canManageChannels = false,
                    canManageOrganizations = false,
                    requiresOrganization = false
                )

                val mockAuthResult = AuthResult(
                    user = mockUser,
                    permissions = mockPermissions,
                    firebaseToken = null as? FirebaseToken ?: return@intercept
                )

                call.attributes.put(AttributeKey<AuthResult>("authResult"), mockAuthResult)
                proceed()
                return@intercept
            }
        }
    }

    build()
}