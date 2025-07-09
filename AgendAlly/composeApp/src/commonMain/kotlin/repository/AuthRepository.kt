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
     * 🧪 Login de testing (modo desarrollo)
     * Mantiene compatibilidad con Fase 2
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


    suspend fun setupOrganization(
        name: String,
        acronym: String,
        description: String = "",
        address: String,
        email: String,
        phone: String,
        studentNumber: Int = 0,
        teacherNumber: Int = 0,
        website: String? = null,
        facebook: String? = null,
        instagram: String? = null,
        twitter: String? = null,
        youtube: String? = null,
        linkedin: String? = null,
        userToken: String? = null
    ): AuthResult {
        return try {
            delay(Constants.UI.LOADING_DELAY_MS)

            val setupResult = authApiService.setupOrganization(
                name = name,
                acronym = acronym,
                description = description,
                address = address,
                email = email,
                phone = phone,
                studentNumber = studentNumber,
                teacherNumber = teacherNumber,
                website = website,
                facebook = facebook,
                instagram = instagram,
                twitter = twitter,
                youtube = youtube,
                linkedin = linkedin,
                authToken = userToken ?: "temp-token"
            )

            if (setupResult.isSuccess) {
                val response = setupResult.getOrThrow()

                if (response.success && response.user != null) {
                    val userData = response.user.toUserData()

                    AuthResult.Success(
                        user = userData,
                        requiresOrganizationSetup = false,
                        message = response.message
                    )
                } else {
                    AuthResult.Error("Setup failed: ${response.message}")
                }
            } else {
                val exception = setupResult.exceptionOrNull()
                AuthResult.Error("Network error: ${exception?.message}")
            }

        } catch (e: Exception) {
            AuthResult.Error("Setup error: ${e.message}")
        }
    }
}


/**
 * 📊 Resultado de autenticación
 * Sealed class que representa todos los posibles estados del login
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