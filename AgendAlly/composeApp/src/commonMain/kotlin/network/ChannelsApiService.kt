package network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import models.*

class ChannelsApiService {

    private val client = HttpClientConfig.client

    /**
     * 🔍 Obtener canales de mi organización
     */
    suspend fun getMyChannels(authToken: String, organizationId: Int): Result<List<Channel>> {
        return try {
            val response = client.get(HttpClientConfig.Endpoints.CHANNELS) {
                bearerAuth(authToken)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
                parameter("organizationId", organizationId)  // ✅ AGREGAR FILTRO
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val channelsResponse = response.body<ChannelsResponse>()
                    Result.success(channelsResponse.channels)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized: Please login again"))
                }
                else -> {
                    Result.failure(Exception("Failed to get channels: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * ➕ Crear nuevo canal
     */
    suspend fun createChannel(
        name: String,
        acronym: String,
        description: String = "",
        email: String? = null,
        phone: String? = null,
        organizationId: Int,
        authToken: String
    ): Result<Channel> {
        return try {
            val request = CreateChannelRequest(
                name = name,
                acronym = acronym,
                description = description,
                email = email,
                phone = phone,
                organizationId = organizationId
            )

            val response = client.post(HttpClientConfig.Endpoints.CHANNELS) {
                setBody(request)
                bearerAuth(authToken)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.Created -> {
                    val createResponse = response.body<CreateChannelResponse>()
                    createResponse.channel?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("No channel data received"))
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized: Please login again"))
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse = response.body<ErrorResponse>()
                    Result.failure(Exception("Invalid data: ${errorResponse.error}"))
                }
                HttpStatusCode.Forbidden -> {
                    Result.failure(Exception("No permission to create channels"))
                }
                else -> {
                    Result.failure(Exception("Create failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Create error: ${e.message}"))
        }
    }

    /**
     * ✏️ Actualizar canal
     */
    suspend fun updateChannel(
        channelId: Int,
        name: String,
        acronym: String,
        description: String = "",
        email: String? = null,
        phone: String? = null,
        authToken: String
    ): Result<Channel> {
        return try {
            val request = UpdateChannelRequest(
                name = name,
                acronym = acronym,
                description = description,
                email = email,
                phone = phone
            )

            val response = client.put("${HttpClientConfig.Endpoints.CHANNELS}/$channelId") {
                setBody(request)
                bearerAuth(authToken)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val updateResponse = response.body<UpdateChannelResponse>()
                    updateResponse.channel?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("No channel data received"))
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized: Please login again"))
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("Channel not found"))
                }
                HttpStatusCode.Forbidden -> {
                    Result.failure(Exception("No permission to edit this channel"))
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
     * 🗑️ Eliminar canal
     */
    suspend fun deleteChannel(
        channelId: Int,
        authToken: String
    ): Result<Boolean> {
        return try {
            val response = client.delete("${HttpClientConfig.Endpoints.CHANNELS}/$channelId") {
                bearerAuth(authToken)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    Result.success(true)
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized: Please login again"))
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("Channel not found"))
                }
                HttpStatusCode.Forbidden -> {
                    Result.failure(Exception("No permission to delete this channel"))
                }
                HttpStatusCode.BadRequest -> {
                    val errorResponse = response.body<ErrorResponse>()
                    Result.failure(Exception(errorResponse.error))
                }
                else -> {
                    Result.failure(Exception("Delete failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Delete error: ${e.message}"))
        }
    }
}

/**
 * 🌟 Singleton instance
 */
object ChannelsApi {
    val instance: ChannelsApiService by lazy { ChannelsApiService() }
}

// ================================
// AGREGAR AL HttpClientConfig.Endpoints:
// ================================

/*
object Endpoints {
    // ... endpoints existentes ...
    const val CHANNELS = "/api/channels"
}
*/