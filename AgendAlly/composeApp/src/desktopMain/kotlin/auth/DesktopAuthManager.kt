// composeApp/src/desktopMain/kotlin/auth/DesktopAuthManager.kt
package auth

import repository.AuthRepo
import repository.AuthResult
import utils.Constants

/**
 * 🖥️ Manager de autenticación específico para Desktop
 * Combina GoogleOAuthDesktop con AuthRepository
 */
class DesktopAuthManager {

    private val googleOAuth = GoogleOAuthDesktop()
    private val authRepository = AuthRepo.instance

    /**
     * 🔐 Autenticación completa con Google OAuth Real
     */
    suspend fun signInWithGoogleOAuth(): AuthResult {
        return try {
            println("🚀 Starting Google OAuth flow...")

            // 1. Ejecutar flujo OAuth para obtener idToken
            val oauthResult = googleOAuth.authenticate()

            when (oauthResult) {
                is OAuthResult.Success -> {
                    println("✅ OAuth successful for: ${oauthResult.email}")

                    // 2. Usar idToken real para autenticar con backend
                    val authResult = authRepository.signInWithGoogleReal(oauthResult.idToken)

                    when (authResult) {
                        is AuthResult.Success -> {
                            println("✅ Backend authentication successful")
                            authResult
                        }
                        is AuthResult.Error -> {
                            println("❌ Backend authentication failed: ${authResult.message}")
                            authResult
                        }
                        else -> authResult
                    }
                }

                is OAuthResult.Error -> {
                    println("❌ OAuth failed: ${oauthResult.message}")
                    AuthResult.Error("OAuth failed: ${oauthResult.message}")
                }

                is OAuthResult.Cancelled -> {
                    println("⏹️ OAuth cancelled by user")
                    AuthResult.Error("Authentication cancelled")
                }

                is OAuthResult.Timeout -> {
                    println("⏱️ OAuth timeout")
                    AuthResult.Error("Authentication timeout")
                }
            }

        } catch (e: Exception) {
            println("💥 Authentication error: ${e.message}")
            AuthResult.Error("Authentication failed: ${e.message}")
        }
    }

    /**
     * 🧪 Autenticación en modo testing (usando email hardcodeado)
     */
    suspend fun signInWithGoogleTesting(): AuthResult {
        println("🧪 Using testing mode authentication...")
        return authRepository.signInWithGoogleTesting()
    }

    /**
     * 🎯 Autenticación principal - selecciona modo según configuración
     */
    suspend fun signInWithGoogle(useRealOAuth: Boolean = !Constants.Development.IS_DEBUG): AuthResult {
        return if (useRealOAuth) {
            signInWithGoogleOAuth()
        } else {
            signInWithGoogleTesting()
        }
    }
}

/**
 * 🌟 Singleton instance
 */
object DesktopAuth {
    val instance: DesktopAuthManager by lazy { DesktopAuthManager() }
}