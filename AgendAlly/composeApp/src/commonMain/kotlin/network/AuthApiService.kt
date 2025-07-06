package network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import models.*

class AuthApiService {

    private val client = HttpClientConfig.client

    /**
     * 🏥 Health check del servidor
     */
    suspend fun getStatus(): Result<StatusResponse> {
        return try {
            val response = client.get(HttpClientConfig.Endpoints.AUTH_STATUS)

            if (response.status == HttpStatusCode.OK) {
                val statusResponse = response.body<StatusResponse>()
                Result.success(statusResponse)
            } else {
                Result.failure(Exception("Server returned ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 📱 Obtener información de tipos de cliente
     */
    suspend fun getClientInfo(): Result<String> {
        return try {
            val response = client.get(HttpClientConfig.Endpoints.AUTH_CLIENT_INFO)

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<String>()
                Result.success(body)
            } else {
                Result.failure(Exception("Server returned ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 🧪 Login de prueba (para desarrollo)
     */
    suspend fun testLogin(email: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(
                email = email,
                idToken = email, // En test mode, usamos email como token
                clientType = HttpClientConfig.ClientTypes.DESKTOP_ADMIN
            )

            val response = client.post(HttpClientConfig.Endpoints.AUTH_TEST_LOGIN) {
                setBody(request)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val loginResponse = response.body<LoginResponse>()
                    Result.success(loginResponse)
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse = response.body<ErrorResponse>()
                    Result.failure(Exception(errorResponse.error))
                }
                HttpStatusCode.InternalServerError -> {
                    val errorResponse = response.body<ErrorResponse>()
                    Result.failure(Exception("Server error: ${errorResponse.error}"))
                }
                else -> {
                    Result.failure(Exception("Unexpected status: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * 🔐 Login real con Firebase token
     */
    suspend fun login(idToken: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(
                email = "", // Se extrae del token en el backend
                idToken = idToken,
                clientType = HttpClientConfig.ClientTypes.DESKTOP_ADMIN
            )

            val response = client.post(HttpClientConfig.Endpoints.AUTH_LOGIN) {
                setBody(request)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val loginResponse = response.body<LoginResponse>()
                    Result.success(loginResponse)
                }
                HttpStatusCode.Unauthorized -> {
                    val errorResponse = response.body<ErrorResponse>()
                    Result.failure(Exception("Unauthorized: ${errorResponse.error}"))
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse = response.body<ErrorResponse>()
                    Result.failure(Exception("Bad request: ${errorResponse.error}"))
                }
                else -> {
                    Result.failure(Exception("Login failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Login error: ${e.message}"))
        }
    }

    /**
     * 🏢 Configurar organización (para usuarios nuevos)
     */
    suspend fun setupOrganization(
        name: String,
        acronym: String,
        contactEmail: String,
        authToken: String
    ): Result<OrganizationSetupResponse> {
        return try {
            val request = OrganizationSetupRequest(
                name = name,
                acronym = acronym,
                contactEmail = contactEmail
            )

            val response = client.post(HttpClientConfig.Endpoints.AUTH_ORGANIZATION_SETUP) {
                setBody(request)
                bearerAuth(authToken)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val setupResponse = response.body<OrganizationSetupResponse>()
                    Result.success(setupResponse)
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse = response.body<ErrorResponse>()
                    Result.failure(Exception("Invalid data: ${errorResponse.error}"))
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized: Please login again"))
                }
                else -> {
                    Result.failure(Exception("Setup failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Setup error: ${e.message}"))
        }
    }

    /**
     * 🔍 Validar token JWT
     */
    suspend fun validateToken(token: String): Result<Boolean> {
        return try {
            val response = client.get("/api/auth/me") {
                bearerAuth(token)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            Result.success(response.status == HttpStatusCode.OK)
        } catch (e: Exception) {
            Result.success(false) // Token inválido
        }
    }
}

/**
 * 🌟 Singleton instance
 */
object AuthApi {
    val instance: AuthApiService by lazy { AuthApiService() }
}