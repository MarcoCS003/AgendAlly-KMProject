package network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import models.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
    suspend fun updateMyOrganization(
        name: String,
        acronym: String,
        description: String,
        address: String,
        email: String,
        phone: String,
        studentNumber: Int = 0,
        teacherNumber: Int = 0,
        logoUrl: String? = null,
        website: String? = null,
        facebook: String? = null,
        instagram: String? = null,
        twitter: String? = null,
        youtube: String? = null,
        linkedin: String? = null,
        authToken: String
    ): Result<Organization> {
        return try {
            val request = UpdateOrganizationRequest(
                name = name,
                acronym = acronym,
                description = description,
                address = address,
                email = email,
                phone = phone,
                studentNumber = studentNumber,
                teacherNumber = teacherNumber,
                logoUrl = logoUrl,
                webSite = website,
                facebook = facebook,
                instagram = instagram,
                twitter = twitter,
                youtube = youtube,
                linkedin = linkedin
            )

            val response = client.put(HttpClientConfig.Endpoints.ORGANIZATIONS_ME) {
                setBody(request)
                bearerAuth(authToken)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val organization = response.body<Organization>()
                    Result.success(organization)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized: Please login again"))
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse = response.body<ErrorResponse>()
                    Result.failure(Exception("Invalid data: ${errorResponse.error}"))
                }
                HttpStatusCode.Forbidden -> {
                    Result.failure(Exception("No permission to edit organization"))
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("Organization not found"))
                }
                else -> {
                    Result.failure(Exception("Update failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Update error: ${e.message}"))
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
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun login(idToken: String): Result<LoginResponse> {
        return try {
            println("🔍 Enviando token al backend:")
            println("   - Primeros 50 chars: ${idToken.take(50)}...")
            val parts = idToken.split(".")
            if (parts.size >= 2) {
                val payload = parts[1]
                val paddedPayload = when (payload.length % 4) {
                    2 -> payload + "=="
                    3 -> payload + "="
                    else -> payload
                }

            }

            val request = LoginRequest(
                email = "",
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
        authToken: String
    ): Result<OrganizationSetupResponse> {
        return try {
            val request = OrganizationSetupRequest(
                name = name,
                acronym = acronym,
                description = description,
                address = address,
                email = email,
                phone = phone,
                studentNumber = studentNumber,
                teacherNumber = teacherNumber,
                webSite = website,
                facebook = facebook,
                instagram = instagram,
                twitter = twitter,
                youtube = youtube,
                linkedin = linkedin
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

    suspend fun getMyOrganization(authToken: String): Result<Organization> {
        return try {
            val response = client.get(HttpClientConfig.Endpoints.ORGANIZATIONS_ME) {
                bearerAuth(authToken)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val organization = response.body<Organization>()
                    Result.success(organization)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized: Please login again"))
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse = response.body<ErrorResponse>()
                    Result.failure(Exception("Bad request: ${errorResponse.error}"))
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("Organization not found"))
                }
                else -> {
                    Result.failure(Exception("Failed to get organization: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}

/**
 * 🌟 Singleton instance
 */
object AuthApi {
    val instance: AuthApiService by lazy { AuthApiService() }
}

