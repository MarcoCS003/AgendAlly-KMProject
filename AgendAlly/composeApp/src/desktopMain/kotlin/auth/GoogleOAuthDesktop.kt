// composeApp/src/desktopMain/kotlin/auth/GoogleOAuthDesktop.kt
package auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import utils.Constants
import java.awt.Desktop
import java.net.URI
import java.util.*

/**
 * 🔐 Google OAuth Manager for Desktop Applications
 * Maneja el flujo completo de autenticación OAuth para aplicaciones desktop
 */
class GoogleOAuthDesktop {

    private var server: NettyApplicationEngine? = null
    private var authResult: CompletableDeferred<OAuthResult>? = null

    /**
     * 🚀 Iniciar flujo de autenticación OAuth
     */
    suspend fun authenticate(): OAuthResult {
        return withTimeoutOrNull(Constants.Network.TIMEOUT_MILLIS) {
            try {
                // 1. Iniciar servidor local para callback
                startLocalServer()

                // 2. Generar URL de autenticación
                val authUrl = buildAuthUrl()

                // 3. Abrir ventana de Google OAuth
                openBrowserWindow(authUrl)

                // 4. Esperar resultado del callback
                val result = authResult?.await() ?: OAuthResult.Error("No response received")

                // 5. Limpiar servidor
                stopLocalServer()

                result

            } catch (e: Exception) {
                stopLocalServer()
                OAuthResult.Error("OAuth failed: ${e.message}")
            }
        } ?: run {
            stopLocalServer()
            OAuthResult.Error("OAuth timeout after ${Constants.Network.TIMEOUT_MILLIS / 1000}s")
        }
    }

    /**
     * 🌐 Iniciar servidor local temporal para recibir callback
     */
    private fun startLocalServer() {
        authResult = CompletableDeferred()

        server = embeddedServer(
            Netty,
            port = Constants.GoogleOAuth.CALLBACK_PORT,
            host = "localhost"
        ) {
            routing {
                get(Constants.GoogleOAuth.CALLBACK_PATH) {
                    handleCallback(call)
                }

                // Página de error para manejo de errores
                get("/error") {
                    call.respondText(
                        """
                        <html>
                        <head><title>AgendAlly - Error</title></head>
                        <body style="font-family: Arial; text-align: center; padding: 50px;">
                        <h2>❌ Error de Autenticación</h2>
                        <p>Hubo un problema con la autenticación. Puedes cerrar esta ventana.</p>
                        <script>setTimeout(() => window.close(), 3000);</script>
                        </body>
                        </html>
                        """.trimIndent(),
                        ContentType.Text.Html
                    )
                }
            }
        }

        server?.start(wait = false)
        println("🌐 OAuth server started on http://localhost:${Constants.GoogleOAuth.CALLBACK_PORT}")
    }

    /**
     * 🛑 Detener servidor local
     */
    private fun stopLocalServer() {
        server?.stop(1000, 2000)
        server = null
        println("🛑 OAuth server stopped")
    }

    /**
     * 📞 Manejar callback de Google OAuth
     */
    private suspend fun handleCallback(call: ApplicationCall) {
        try {
            val parameters = call.request.queryParameters

            when {
                // ✅ Éxito: código de autorización recibido
                parameters.contains("code") -> {
                    val authCode = parameters["code"]!!
                    val state = parameters["state"]

                    println("✅ Authorization code received: ${authCode.take(20)}...")

                    // Intercambiar código por tokens
                    val tokenResult = exchangeCodeForTokens(authCode)

                    // Responder con página de éxito
                    call.respondText(
                        buildSuccessPage(),
                        ContentType.Text.Html
                    )

                    // Completar el resultado
                    authResult?.complete(tokenResult)
                }

                // ❌ Error: usuario canceló o error de OAuth
                parameters.contains("error") -> {
                    val error = parameters["error"] ?: "unknown_error"
                    val errorDescription = parameters["error_description"] ?: "No description"

                    println("❌ OAuth error: $error - $errorDescription")

                    // Responder con página de error
                    call.respondText(
                        buildErrorPage(errorDescription),
                        ContentType.Text.Html
                    )

                    // Completar con error
                    authResult?.complete(OAuthResult.Error("OAuth error: $errorDescription"))
                }

                // ❓ Callback inesperado
                else -> {
                    println("❓ Unexpected callback parameters: ${parameters.entries()}")
                    call.respondText(
                        buildErrorPage("Unexpected callback"),
                        ContentType.Text.Html
                    )
                    authResult?.complete(OAuthResult.Error("Unexpected callback"))
                }
            }

        } catch (e: Exception) {
            println("💥 Error handling callback: ${e.message}")
            call.respondText(
                buildErrorPage("Server error: ${e.message}"),
                ContentType.Text.Html
            )
            authResult?.complete(OAuthResult.Error("Callback error: ${e.message}"))
        }
    }

    /**
     * 🔗 Construir URL de autenticación de Google
     */
    private fun buildAuthUrl(): String {
        val state = UUID.randomUUID().toString()

        val params = mapOf(
            "client_id" to Constants.GoogleOAuth.CLIENT_ID,
            "redirect_uri" to Constants.GoogleOAuth.REDIRECT_URI,
            "scope" to Constants.GoogleOAuth.SCOPE,
            "response_type" to "code",
            "state" to state,
            "access_type" to "offline",
            "prompt" to "select_account" // Forzar selección de cuenta
        )

        val queryString = params.entries.joinToString("&") { (key, value) ->
            "$key=${value.encodeURLParameter()}"
        }

        return "${Constants.GoogleOAuth.AUTH_URL}?$queryString"
    }

    /**
     * 🌐 Abrir ventana del navegador para OAuth
     */
    private fun openBrowserWindow(url: String) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
                println("🌐 Opening browser window for OAuth...")
            } else {
                println("⚠️ Desktop not supported, manual navigation required: $url")
            }
        } catch (e: Exception) {
            println("❌ Failed to open browser: ${e.message}")
            throw Exception("Cannot open browser for OAuth")
        }
    }

    /**
     * 🎫 Intercambiar código de autorización por tokens
     */

    private suspend fun exchangeCodeForTokens(authCode: String): OAuthResult {
        return try {
            println("🔄 Exchanging authorization code for tokens...")

            val client = HttpClient {
                install(ContentNegotiation) { json() }
            }

            // ⚠️ CAMBIO IMPORTANTE: NO incluir client_secret para desktop apps
            val response = client.submitForm(
                url = Constants.GoogleOAuth.TOKEN_URL,
                formParameters = Parameters.build {
                    append("client_id", Constants.GoogleOAuth.CLIENT_ID)
                    append("client_secret", Constants.GoogleOAuth.CLIENT_SECRET)
                    append("code", authCode)
                    append("grant_type", "authorization_code")
                    append("redirect_uri", Constants.GoogleOAuth.REDIRECT_URI)
                }
            )

            client.close()

            if (response.status == HttpStatusCode.OK) {
                val tokenResponse = response.body<TokenResponse>()
                println("✅ Token exchange successful")

                val userInfo = decodeJWTPayload(tokenResponse.id_token)
                val email = userInfo["email"] as? String ?: "unknown@example.com"

                OAuthResult.Success(
                    idToken = tokenResponse.id_token,
                    email = email,
                    accessToken = tokenResponse.access_token ?: ""
                )
            } else {
                val errorBody = response.bodyAsText()
                println("❌ Token exchange failed: ${response.status} - $errorBody")
                OAuthResult.Error("Token exchange failed: ${response.status}")
            }
        } catch (e: Exception) {
            println("❌ Exception: ${e.message}")
            OAuthResult.Error("Failed to exchange tokens: ${e.message}")
        }
    }
    /**
     * 📄 Página HTML de éxito
     */
    private fun buildSuccessPage(): String {
        return """
    <html>
    <head>
        <title>AgendAlly - Autenticación Exitosa</title>
        <style>
            body { 
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                text-align: center; padding: 50px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white; margin: 0; height: 100vh; display: flex; align-items: center; justify-content: center;
            }
            .container { max-width: 400px; }
            .success { font-size: 48px; margin-bottom: 20px; animation: bounce 1s; }
            .title { font-size: 24px; margin-bottom: 10px; font-weight: 600; }
            .message { font-size: 16px; opacity: 0.9; line-height: 1.5; }
            .timer { font-size: 14px; margin-top: 20px; opacity: 0.7; }
            @keyframes bounce { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.1); } }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="success">✅</div>
            <div class="title">¡Autenticación Exitosa!</div>
            <div class="message">
                Has iniciado sesión correctamente en AgendAlly.<br>
                Esta ventana se cerrará automáticamente.
            </div>
            <div class="timer">Cerrando en <span id="countdown">3</span> segundos...</div>
        </div>
        <script>
            let seconds = 3;
            const countdown = document.getElementById('countdown');
            const timer = setInterval(() => {
                seconds--;
                countdown.textContent = seconds;
                if (seconds <= 0) {
                    clearInterval(timer);
                    try { window.close(); } catch(e) { }
                }
            }, 1000);
        </script>
    </body>
    </html>
    """.trimIndent()
    }

    /**
     * 📄 Página HTML de error
     */
    private fun buildErrorPage(errorMessage: String): String {
        return """
        <html>
        <head>
            <title>AgendAlly - Error de Autenticación</title>
            <style>
                body { font-family: Arial; text-align: center; padding: 50px; background: #fef2f2; }
                .error { color: #dc2626; font-size: 24px; margin-bottom: 20px; }
                .message { color: #374151; font-size: 16px; }
            </style>
        </head>
        <body>
            <div class="error">❌ Error de Autenticación</div>
            <div class="message">
                $errorMessage<br>
                Puedes cerrar esta ventana e intentar nuevamente.
            </div>
            <script>
                setTimeout(() => {
                    try { window.close(); } catch(e) { }
                }, 5000);
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}

@Serializable
data class TokenResponse(
    val access_token: String? = null,
    val id_token: String,
    val token_type: String? = null,
    val expires_in: Int? = null,
    val refresh_token: String? = null,
    val scope: String? = null
)
// Función para decodificar JWT (básica)
private fun decodeJWTPayload(jwt: String): Map<String, Any> {
    return try {
        val parts = jwt.split(".")
        if (parts.size < 2) {
            println("⚠️ Invalid JWT format")
            return mapOf("email" to "unknown@example.com")
        }

        // Decodificar payload (parte 2 del JWT)
        val payload = parts[1]

        // Agregar padding si es necesario
        val paddedPayload = when (payload.length % 4) {
            2 -> payload + "=="
            3 -> payload + "="
            else -> payload
        }

        val decodedBytes = Base64.getUrlDecoder().decode(paddedPayload)
        val jsonString = String(decodedBytes)

        println("🎯 JWT payload: $jsonString")

        // Parsear JSON
        val json = Json { ignoreUnknownKeys = true }
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject

        // Convertir a Map<String, Any>
        return jsonObject.mapValues { entry ->
            when (val element = entry.value) {
                is JsonPrimitive -> {
                    when {
                        element.isString -> element.content
                        element.booleanOrNull != null -> element.boolean
                        element.longOrNull != null -> element.long
                        element.doubleOrNull != null -> element.double
                        else -> element.content
                    }
                }
                else -> element.toString()
            }
        }

    } catch (e: Exception) {
        println("❌ Error decoding JWT: ${e.message}")
        mapOf("email" to "unknown@example.com")
    }
}