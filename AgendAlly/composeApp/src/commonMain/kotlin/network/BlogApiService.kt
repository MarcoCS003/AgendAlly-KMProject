package network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import models.*

class BlogApiService {

    private val client = HttpClientConfig.client

    /**
     * 📖 Obtener todos los eventos del blog
     */
    suspend fun getAllEvents(): Result<BlogEventsResponse> {
        return try {
            val response = client.get("${HttpClientConfig.Endpoints.BLOG_EVENTS}")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val blogResponse = response.body<BlogEventsResponse>()
                    Result.success(blogResponse)
                }
                else -> {
                    Result.failure(Exception("Error getting events: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * 📖 Obtener eventos por organización
     */
    suspend fun getEventsByOrganization(organizationId: Int): Result<BlogEventsResponse> {
        return try {
            val response = client.get("${HttpClientConfig.Endpoints.BLOG_EVENTS}/organization/$organizationId")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val blogResponse = response.body<BlogEventsResponse>()
                    Result.success(blogResponse)
                }
                else -> {
                    Result.failure(Exception("Error getting organization events: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * 📖 Obtener eventos por canal
     */
    suspend fun getEventsByChannel(channelId: Int): Result<BlogEventsResponse> {
        return try {
            val response = client.get("${HttpClientConfig.Endpoints.BLOG_EVENTS}/channel/$channelId")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val blogResponse = response.body<BlogEventsResponse>()
                    Result.success(blogResponse)
                }
                else -> {
                    Result.failure(Exception("Error getting channel events: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * ➕ Crear nuevo evento
     */
    suspend fun createEvent(
        request: CreateEventRequest,
        authToken: String
    ): Result<BlogEvent> {
        return try {
            val response = client.post(HttpClientConfig.Endpoints.BLOG_EVENTS) {
                bearerAuth(authToken)
                contentType(ContentType.Application.Json)
                setBody(request)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.Created, HttpStatusCode.OK -> {
                    // Intentar parsear como CreateEventResponse primero
                    try {
                        val createResponse = response.body<CreateEventResponse>()
                        if (createResponse.success && createResponse.event != null) {
                            Result.success(createResponse.event)
                        } else {
                            Result.failure(Exception(createResponse.message))
                        }
                    } catch (e: Exception) {
                        // Si falla, intentar parsear directamente como BlogEvent
                        try {
                            val event = response.body<BlogEvent>()
                            Result.success(event)
                        } catch (e2: Exception) {
                            // Si ambos fallan, mostrar el texto de respuesta para debug
                            val responseText = response.body<String>()
                            println("🔍 Response body: $responseText")
                            Result.failure(Exception("Error parsing response: ${e.message}"))
                        }
                    }
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized: Please login again"))
                }
                HttpStatusCode.Forbidden -> {
                    Result.failure(Exception("No permission to create events"))
                }
                HttpStatusCode.BadRequest -> {
                    try {
                        val errorResponse = response.body<ErrorResponse>()
                        Result.failure(Exception(errorResponse.error))
                    } catch (e: Exception) {
                        val responseText = response.body<String>()
                        Result.failure(Exception("Bad request: $responseText"))
                    }
                }
                else -> {
                    val responseText = response.body<String>()
                    Result.failure(Exception("Create failed (${response.status}): $responseText"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * ✏️ Actualizar evento
     */
    suspend fun updateEvent(
        eventId: Int,
        request: UpdateEventRequest,
        authToken: String
    ): Result<BlogEvent> {
        return try {
            val response = client.put("${HttpClientConfig.Endpoints.BLOG_EVENTS}/$eventId") {
                bearerAuth(authToken)
                contentType(ContentType.Application.Json)
                setBody(request)
                clientType(HttpClientConfig.ClientTypes.DESKTOP_ADMIN)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    try {
                        val event = response.body<BlogEvent>()
                        Result.success(event)
                    } catch (e: Exception) {
                        // 🆕 Si falla el parsing, crear un BlogEvent dummy con éxito
                        println("🔍 Update parsing failed, but operation succeeded")
                        val dummyEvent = BlogEvent(
                            id = eventId,
                            title = request.title,
                            shortDescription = request.shortDescription,
                            longDescription = request.longDescription,
                            location = request.location,
                            startDate = request.startDate,
                            endDate = request.endDate,
                            category = request.category,
                            imagePath = "",
                            organizationId = 0, // Se actualizará al recargar
                            channelId = request.channelId
                        )
                        Result.success(dummyEvent)
                    }
                }
                HttpStatusCode.Unauthorized -> {
                    Result.failure(Exception("Unauthorized: Please login again"))
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("Event not found"))
                }
                HttpStatusCode.Forbidden -> {
                    Result.failure(Exception("No permission to edit this event"))
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
     * 🗑️ Eliminar evento
     */
    suspend fun deleteEvent(
        eventId: Int,
        authToken: String
    ): Result<Boolean> {
        return try {
            val response = client.delete("${HttpClientConfig.Endpoints.BLOG_EVENTS}/$eventId") {
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
                    Result.failure(Exception("Event not found"))
                }
                HttpStatusCode.Forbidden -> {
                    Result.failure(Exception("No permission to delete this event"))
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
object BlogApi {
    val instance: BlogApiService by lazy { BlogApiService() }
}