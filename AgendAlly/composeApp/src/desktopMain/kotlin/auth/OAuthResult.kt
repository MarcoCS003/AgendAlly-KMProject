// composeApp/src/desktopMain/kotlin/auth/OAuthResult.kt
package auth

/**
 * 🎫 Resultado del proceso de autenticación OAuth
 */
sealed class OAuthResult {

    /**
     * ✅ Autenticación exitosa
     */
    data class Success(
        val idToken: String,
        val email: String,
        val accessToken: String? = null,
        val refreshToken: String? = null,
        val expiresIn: Long? = null
    ) : OAuthResult()

    /**
     * ❌ Error en la autenticación
     */
    data class Error(
        val message: String,
        val code: String? = null,
        val cause: Throwable? = null
    ) : OAuthResult()

    /**
     * ❌ Usuario canceló la autenticación
     */
    data object Cancelled : OAuthResult()

    /**
     * ⏱️ Timeout en la autenticación
     */
    data object Timeout : OAuthResult()
}

/**
 * 🔍 Extensions para facilitar el manejo de resultados
 */
fun OAuthResult.isSuccess(): Boolean = this is OAuthResult.Success

fun OAuthResult.isError(): Boolean = this is OAuthResult.Error

fun OAuthResult.getSuccessOrNull(): OAuthResult.Success? = this as? OAuthResult.Success

fun OAuthResult.getErrorOrNull(): OAuthResult.Error? = this as? OAuthResult.Error

/**
 * 🎯 Convertir a mensaje legible para el usuario
 */
fun OAuthResult.toUserMessage(): String {
    return when (this) {
        is OAuthResult.Success -> "Autenticación exitosa para $email"
        is OAuthResult.Error -> "Error: $message"
        is OAuthResult.Cancelled -> "Autenticación cancelada por el usuario"
        is OAuthResult.Timeout -> "Tiempo de espera agotado. Intenta nuevamente."
    }
}

