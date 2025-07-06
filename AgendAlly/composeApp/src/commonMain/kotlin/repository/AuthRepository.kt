// composeApp/src/commonMain/kotlin/repository/AuthRepository.kt
package repository

import kotlinx.coroutines.delay
import models.*
import network.AuthApi
import utils.Constants

/**
 * 🔐 Repositorio de autenticación que maneja la lógica de login
 * Soporta tanto OAuth real como modo testing
 */
class AuthRepository {

    private val authApiService = AuthApi.instance

    /**
     * 🎯 Login real con Google OAuth (para Desktop)
     * Este método debe ser llamado desde desktop con OAuth real
     */
    suspend fun signInWithGoogleReal(idToken: String): AuthResult {
        return try {
            delay(Constants.UI.LOADING_DELAY_MS)

            // 🌐 Llamada real al backend con idToken
            val loginResult = authApiService.login(idToken)

            if (loginResult.isSuccess) {
                val loginResponse = loginResult.getOrThrow()

                if (loginResponse.success && loginResponse.user != null) {
                    val userData = loginResponse.user.toUserData()

                    AuthResult.Success(
                        user = userData,
                        requiresOrganizationSetup = loginResponse.requiresOrganizationSetup,
                        message = loginResponse.message
                    )
                } else {
                    AuthResult.Error("Login failed: ${loginResponse.message}")
                }
            } else {
                val exception = loginResult.exceptionOrNull()
                AuthResult.Error("Network error: ${exception?.message}")
            }

        } catch (e: Exception) {
            AuthResult.Error("Unexpected error: ${e.message}")
        }
    }

    /**
     * 🏥 Verificar estado del servidor
     */
    suspend fun checkServerStatus(): Result<StatusResponse> {
        return authApiService.getStatus()
    }

    /**
     * 🔍 Validar token (para futuro uso)
     */
    suspend fun validateToken(token: String): Boolean {
        return authApiService.validateToken(token).getOrDefault(false)
    }
}.user.toUserData()

        AuthResult.Success(
        user = userData,
        requiresOrganizationSetup = loginResponse.requiresOrganizationSetup,
        message = loginResponse.message
        )
        } else {
            AuthResult.Error("Login failed: ${loginResponse.message}")
        }
        } else {
            val exception = loginResult.exceptionOrNull()
            AuthResult.Error("Network error: ${exception?.message}")
        }

        } catch (e: Exception) {
            AuthResult.Error("Unexpected error: ${e.message}")
        }
}

/**
 * 🎯 Simula el login con Google (Fase 2 - modo testing)
 * Mantiene la misma interfaz que handleGoogleSignIn original
 */
suspend fun signInWithGoogle(): AuthResult {
    return signInWithGoogleTesting()
}

/**
 * 🧪 Login de testing (modo desarrollo)
 */
suspend fun signInWithGoogleTesting(): AuthResult {
    return try {
        delay(Constants.UI.LOADING_DELAY_MS)

        // 📧 Email de testing
        val testEmail = Constants.Development.TEST_ADMIN_EMAIL

        // 🌐 Llamada real al backend (endpoint de testing)
        val loginResult = authApiService.testLogin(testEmail)

        if (loginResult.isSuccess) {
            val loginResponse = loginResult.getOrThrow()

            if (loginResponse.success && loginResponse.user != null) {
                val userData = loginResponse.user.toUserData()

                AuthResult.Success(
                    user = userData,
                    requiresOrganizationSetup = loginResponse.requiresOrganizationSetup,
                    message = loginResponse.message
                )
            } else {
                AuthResult.Error("Login failed: ${loginResponse.message}")
            }
        } else {
            val exception = loginResult.exceptionOrNull()
            AuthResult.Error("Network error: ${exception?.message}")
        }

    } catch (e: Exception) {
        AuthResult.Error("Unexpected error: ${e.message}")
    }
}

/**
 * 🧪 Login de testing con email específico
 * Útil para testing de diferentes scenarios
 */
suspend fun testLoginWithEmail(email: String): AuthResult {
    return try {
        delay(Constants.UI.LOADING_DELAY_MS)

        val loginResult = authApiService.testLogin(email)

        if (loginResult.isSuccess) {
            val loginResponse = loginResult.getOrThrow()

            if (loginResponse.success && loginResponse.user != null) {
                val userData = loginResponse.user.toUserData()

                AuthResult.Success(
                    user = userData,
                    requiresOrganizationSetup = loginResponse.requiresOrganizationSetup,
                    message = loginResponse.message
                )
            } else {
                AuthResult.Error("Login failed: ${loginResponse.message}")
            }
        } else {
            val exception = loginResult.exceptionOrNull()
            AuthResult.Error("Network error: ${exception?.message}")
        }

    } catch (e: Exception) {
        AuthResult.Error("Unexpected error: ${e.message}")
    }
}

/**
 * 🏥 Verificar estado del servidor
 */
suspend fun checkServerStatus(): Result<StatusResponse> {
    return authApiService.getStatus()
}

/**
 * 🔍 Validar token (para futuro uso)
 */
suspend fun validateToken(token: String): Boolean {
    return authApiService.validateToken(token).getOrDefault(false)
}
}.user.toUserData()

AuthResult.Success(
user = userData,
requiresOrganizationSetup = loginResponse.requiresOrganizationSetup,
message = loginResponse.message
)
} else {
    AuthResult.Error("Login failed: ${loginResponse.message}")
}
} else {
    val exception = loginResult.exceptionOrNull()
    AuthResult.Error("Network error: ${exception?.message}")
}

} catch (e: Exception) {
    AuthResult.Error("Unexpected error: ${e.message}")
}
}

/**
 * 🏥 Verificar estado del servidor
 */
suspend fun checkServerStatus(): Result<StatusResponse> {
    return authApiService.getStatus()
}

/**
 * 🔍 Validar token (para futuro uso)
 */
suspend fun validateToken(token: String): Boolean {
    return authApiService.validateToken(token).getOrDefault(false)
}
}

/**
 * 📊 Resultado de autenticación
 */
sealed class AuthResult {
    data class Success(
        val user: UserData,
        val requiresOrganizationSetup: Boolean = false,
        val message: String? = null
    ) : AuthResult()

    data class Error(
        val message: String
    ) : AuthResult()

    data object Loading : AuthResult()
}

/**
 * 🌟 Singleton instance
 */
object AuthRepo {
    val instance: AuthRepository by lazy { AuthRepository() }
}