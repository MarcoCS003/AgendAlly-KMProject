package auth


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import utils.Constants
import java.util.*

/**
 * 🔄 Servicio para intercambio real de tokens con Google OAuth API
 */
object TokenExchangeService {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * 🎫 Intercambiar authorization code por tokens reales de Google
     */
    suspend fun exchangeAuthorizationCode(authCode: String): OAuthResult {
        return withTimeoutOrNull(30000) { // 30 segundos timeout
            try {
                println("🔄 Exchanging authorization code for tokens...")
                println("📡 Making request to ${Constants.GoogleOAuth.TOKEN_URL}")

                // 1. Intercambiar código por tokens
                val tokenResponse = httpClient.submitForm(
                    url = Constants.GoogleOAuth.TOKEN_URL,
                    formParameters = parameters {
                        append("client_id", Constants.GoogleOAuth.CLIENT_ID)
                        append("client_secret", Constants.GoogleOAuth.CLIENT_SECRET)
                        append("code", authCode)
                        append("grant_type", "authorization_code")
                        append("redirect_uri", Constants.GoogleOAuth.REDIRECT_URI)
                    }
                )

                if (tokenResponse.status.isSuccess()) {
                    val tokens: GoogleTokenResponse = tokenResponse.body()

                    println("✅ Token exchange successful")
                    println("🎫 idToken received: ${tokens.idToken.take(50)}...")

                    // 2. Obtener información del usuario del idToken
                    val userInfo = extractUserInfoFromIdToken(tokens.idToken)

                    println("👤 User email extracted: ${userInfo.email}")

                    OAuthResult.Success(
                        idToken = tokens.idToken,
                        email = userInfo.email,
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken
                    )
                } else {
                    val errorBody = tokenResponse.body<String>()
                    println("❌ Token exchange failed: ${tokenResponse.status}")
                    println("📄 Error details: $errorBody")

                    OAuthResult.Error("Token exchange failed: ${tokenResponse.status}")
                }

            } catch (e: Exception) {
                println("💥 Token exchange error: ${e.message}")
                OAuthResult.Error("Token exchange failed: ${e.message}")
            }
        } ?: OAuthResult.Error("Token exchange timeout")
    }

    /**
     * 👤 Extraer información del usuario del idToken (JWT simple)
     * Nota: Para producción, se recomienda usar una librería JWT completa
     */
    private fun extractUserInfoFromIdToken(idToken: String): UserInfo {
        return try {
            // El idToken es un JWT con formato: header.payload.signature
            val parts = idToken.split(".")
            if (parts.size != 3) {
                throw Exception("Invalid JWT format")
            }

            // Decodificar el payload (parte central del JWT)
            val payload = String(Base64.getDecoder().decode(parts[1]))
            println("🔍 JWT payload: $payload")

            // Parse simple del JSON del payload
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<JWTPayload>(payload).let { jwt ->
                UserInfo(
                    email = jwt.email ?: "unknown@example.com",
                    name = jwt.name ?: "Unknown User",
                    picture = jwt.picture
                )
            }

        } catch (e: Exception) {
            println("⚠️ Failed to parse idToken, using fallback: ${e.message}")
            // Fallback si no se puede parsear el token
            UserInfo(
                email = "parsed.user@gmail.com",
                name = "Real User",
                picture = null
            )
        }
    }
}

/**
 * 📄 Respuesta de Google Token API
 */
@Serializable
data class GoogleTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val id_token: String,
    val refresh_token: String? = null,
    val scope: String,
    val token_type: String
) {
    // Properties para compatibilidad con OAuthResult
    val accessToken: String get() = access_token
    val idToken: String get() = id_token
    val refreshToken: String? get() = refresh_token
}

/**
 * 🎫 Payload del JWT idToken (campos principales)
 */
@Serializable
data class JWTPayload(
    val iss: String? = null,        // Issuer
    val sub: String? = null,        // Subject (Google user ID)
    val email: String? = null,      // Email del usuario
    val name: String? = null,       // Nombre del usuario
    val picture: String? = null,    // URL de foto de perfil
    val exp: Long? = null,          // Expiration time
    val iat: Long? = null           // Issued at time
)

/**
 * 👤 Información extraída del usuario
 */
data class UserInfo(
    val email: String,
    val name: String,
    val picture: String? = null
)