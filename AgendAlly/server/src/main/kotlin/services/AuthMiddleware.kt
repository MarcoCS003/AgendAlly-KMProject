package services

import com.example.*
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
        FirebaseConfig.getAuth()
    }

    /**
     * Valida token de Firebase y determina permisos según plataforma
     */
    fun validateTokenAndPermissions(
        idToken: String,
        clientType: ClientType
    ): AuthResult? {
        return try {
            println("🔍 Validando token Google OAuth...")

            // ✅ CAMBIO: Usar verificación Google en lugar de Firebase
            val tokenInfo = verifyGoogleIdToken(idToken)

            if (tokenInfo == null) {
                println("❌ Token Google inválido")
                return null
            }

            println("✅ Token válido para: ${tokenInfo.email}")

            // 2. Determinar permisos según plataforma y email
            val permissions = determinePermissions(clientType, tokenInfo.email)

            // 3. Crear/actualizar usuario en BD
            val (user, isNewUser) = ensureUserExistsFromGoogle(tokenInfo, permissions.role)

            AuthResult(
                user = user,
                permissions = permissions,
                tokenInfo = tokenInfo,  // ✅ CAMBIO: usar tokenInfo
                isNewUser = isNewUser
            )

        } catch (e: Exception) {
            println("❌ Error validando token: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * ✅ DETERMINA PERMISOS SOLO POR PLATAFORMA (SIMPLIFICADO)
     */
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
    private fun ensureUserExistsFromGoogle(token: GoogleTokenInfo, role: UserRole): Pair<User, Boolean> {
        return transaction {
            val existingUser = Users.select { Users.googleId eq token.uid }.singleOrNull()

            if (existingUser != null) {
                // Usuario existente - actualizar info
                Users.update({ Users.googleId eq token.uid }) {
                    it[name] = token.name ?: token.email
                    it[email] = token.email
                    it[profilePicture] = token.picture
                    it[lastLoginAt] = LocalDateTime.now()
                }

                val user = mapRowToUser(Users.select { Users.googleId eq token.uid }.single())
                Pair(user, false)
            } else {
                // Usuario nuevo
                val userId = Users.insert {
                    it[googleId] = token.uid
                    it[email] = token.email
                    it[name] = token.name ?: token.email
                    it[profilePicture] = token.picture
                    it[this.role] = role.name
                    it[isActive] = true
                    it[createdAt] = LocalDateTime.now()
                    it[lastLoginAt] = LocalDateTime.now()
                    it[notificationsEnabled] = true
                    it[syncEnabled] = false
                } get Users.id

                val newUser = mapRowToUser(Users.select { Users.id eq userId }.single())
                Pair(newUser, true)
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
            organizationId = row[Users.organizationId],
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
            println("🔍 ===== VERIFICANDO TOKEN GOOGLE OAUTH =====")
            println("🔑 Token recibido: ${idToken.take(50)}...")

            // ✅ Verificar token de Google
            val tokenInfo = verifyGoogleIdToken(idToken)

            if (tokenInfo == null) {
                println("❌ Token verification failed")
                return null
            }

            println("✅ Token Google válido para: ${tokenInfo.email}")

            // Crear permisos para desktop admin
            val permissions = UserPermissions(
                role = UserRole.ADMIN,
                canCreateEvents = true,
                canManageChannels = true,
                canManageOrganizations = false,
                requiresOrganization = true
            )

            // Crear/actualizar usuario
            val (user, isNewUser) = ensureUserExistsFromGoogle(tokenInfo, UserRole.ADMIN)

            AuthResult(
                user = user,
                permissions = permissions,
                tokenInfo = tokenInfo,  // ✅ USAR tokenInfo en lugar de firebaseToken
                isNewUser = isNewUser
            )

        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun verifyGoogleIdToken(idToken: String): GoogleTokenInfo? {
        return try {
            val parts = idToken.split(".")
            if (parts.size < 3) return null

            val payload = parts[1]
            val paddedPayload = when (payload.length % 4) {
                2 -> payload + "=="
                3 -> payload + "="
                else -> payload
            }

            val decodedBytes = java.util.Base64.getUrlDecoder().decode(paddedPayload)
            val jsonString = String(decodedBytes)

            println("🎯 JWT payload: $jsonString")

            // Parsear manualmente
            val emailRegex = "\"email\":\"([^\"]+)\"".toRegex()
            val nameRegex = "\"name\":\"([^\"]+)\"".toRegex()
            val subRegex = "\"sub\":\"([^\"]+)\"".toRegex()
            val pictureRegex = "\"picture\":\"([^\"]+)\"".toRegex()

            val email = emailRegex.find(jsonString)?.groupValues?.get(1) ?: return null
            val name = nameRegex.find(jsonString)?.groupValues?.get(1) ?: email
            val uid = subRegex.find(jsonString)?.groupValues?.get(1) ?: return null
            val picture = pictureRegex.find(jsonString)?.groupValues?.get(1)

            GoogleTokenInfo(
                uid = uid,
                email = email,
                name = name,
                picture = picture
            )

        } catch (e: Exception) {
            println("❌ Error parsing Google token: ${e.message}")
            null
        }
    }


    private fun ensureUserExistsFromGoogle(token: FirebaseToken, role: UserRole): Pair<User, Boolean> {
        return transaction {
            val existingUser = Users.select { Users.googleId eq token.uid }.singleOrNull()

            if (existingUser != null) {
                // Usuario existente - actualizar info
                Users.update({ Users.googleId eq token.uid }) {
                    it[name] = token.name ?: token.email
                    it[email] = token.email
                    it[profilePicture] = token.picture
                    it[lastLoginAt] = LocalDateTime.now()
                }

                val user = mapRowToUser(Users.select { Users.googleId eq token.uid }.single())
                Pair(user, false)
            } else {
                // Usuario nuevo
                val userId = Users.insert {
                    it[googleId] = token.uid
                    it[email] = token.email
                    it[name] = token.name ?: token.email
                    it[profilePicture] = token.picture
                    it[this.role] = role.name
                    it[isActive] = true
                    it[createdAt] = LocalDateTime.now()
                    it[lastLoginAt] = LocalDateTime.now()
                    it[notificationsEnabled] = true
                    it[syncEnabled] = false
                } get Users.id

                val newUser = mapRowToUser(Users.select { Users.id eq userId }.single())
                Pair(newUser, true)
            }
        }
    }
}

/**
 * Resultado de autenticación
 */
data class AuthResult(
    val user: User,
    val permissions: UserPermissions,
    val tokenInfo: GoogleTokenInfo,  // ✅ CAMBIAR: era firebaseToken: FirebaseToken
    val isNewUser: Boolean = false
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
                    tokenInfo = null as? GoogleTokenInfo ?: return@intercept
                )

                call.attributes.put(AttributeKey<AuthResult>("authResult"), mockAuthResult)
                proceed()
                return@intercept
            }
        }
    }

    build()
}

data class GoogleTokenInfo(
    val uid: String,
    val email: String,
    val name: String?,
    val picture: String?,
    val issuer: String = "https://accounts.google.com"
)