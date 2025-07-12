package network


import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientConfig {

    private const val BASE_URL = "http://localhost:8080"

    /**
     * Cliente HTTP configurado para AgendAlly API
     */
    val client = HttpClient {

        // 📄 JSON Serialization
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        // 🔧 Default Request Configuration
        install(DefaultRequest) {
            url(BASE_URL)
            contentType(ContentType.Application.Json)

            // Headers por defecto
            header("X-Client-Type", "DESKTOP_ADMIN")
            header("User-Agent", "AgendAlly-Desktop/1.0.0")
        }

        // 📝 Logging (solo en desarrollo)
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
        }

        // ⏱️ Timeouts
        engine {
            // Timeout específico por plataforma se configura en desktopMain
        }
    }

    /**
     * URLs de endpoints
     */
    object Endpoints {
        const val AUTH_STATUS = "/api/auth/status"
        const val AUTH_CLIENT_INFO = "/api/auth/client-info"
        const val AUTH_LOGIN = "/api/auth/login"
        const val ORGANIZATIONS_ME = "/api/organizations/me"
        const val AUTH_TEST_LOGIN = "/api/auth/test-login"
        const val AUTH_ORGANIZATION_SETUP = "/api/auth/organization-setup"
    }

    /**
     * Headers comunes
     */
    object Headers {
        const val CLIENT_TYPE = "X-Client-Type"
        const val AUTHORIZATION = "Authorization"
        const val CONTENT_TYPE = "Content-Type"
    }

    /**
     * Valores de client types
     */
    object ClientTypes {
        const val DESKTOP_ADMIN = "DESKTOP_ADMIN"
        const val ANDROID_STUDENT = "ANDROID_STUDENT"
        const val WEB_ADMIN = "WEB_ADMIN"
    }
}

/**
 * Extension para agregar Authorization header
 */
fun HttpRequestBuilder.bearerAuth(token: String) {
    header(HttpClientConfig.Headers.AUTHORIZATION, "Bearer $token")
}

/**
 * Extension para agregar Client-Type header
 */
fun HttpRequestBuilder.clientType(type: String) {
    header(HttpClientConfig.Headers.CLIENT_TYPE, type)
}