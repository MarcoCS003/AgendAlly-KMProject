package com.example

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ============== ENUMS ==============

@Serializable
enum class ClientType {
    ANDROID_STUDENT,    // App móvil para estudiantes
    DESKTOP_ADMIN,      // App escritorio para administradores
    WEB_ADMIN,          // Web dashboard para administradores
    UNKNOWN             // Por defecto/desarrollo
}

@Serializable
enum class EventItemType {
    // Información temporal
    SCHEDULE, DEADLINE, DURATION,

    // Enlaces y archivos
    ATTACHMENT, WEBSITE, REGISTRATION_LINK, LIVE_STREAM, RECORDING,

    // Redes sociales
    FACEBOOK, INSTAGRAM, TWITTER, YOUTUBE, LINKEDIN,

    // Contacto
    PHONE, EMAIL, WHATSAPP,

    // Ubicación
    MAPS_LINK, ROOM_NUMBER, BUILDING,

    // Información adicional
    REQUIREMENTS, PRICE, CAPACITY, ORGANIZER
}

@Serializable
enum class UserRole {
    SUPER_ADMIN,    // Administrador general del sistema
    ADMIN,          // Administrador de organización específica
    STUDENT         // Estudiante que consume eventos
}


// ============== MODELOS PRINCIPALES ==============

// ✅ ORGANIZATION (antes Institute)
@Serializable
data class Organization(
    val organizationID: Int,
    val acronym: String,
    val name: String,
    val description: String = "",
    val address: String,
    val email: String,
    val phone: String,
    val studentNumber: Int,
    val teacherNumber: Int,
    val logoUrl: String? = null,
    val webSite: String? = null,
    val facebook: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val youtube: String? = null,
    val linkedin: String? = null,
    val channels: List<Channel> = emptyList(), // ✅ CAMBIO: listCareer -> channels
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ✅ CHANNEL (antes Career)
@Serializable
data class Channel(
    val id: Int,
    val organizationId: Int, // ✅ CAMBIO: instituteId -> organizationId
    val organizationName: String, // Para mostrar en frontend
    val name: String,
    val acronym: String,
    val description: String = "",
    val email: String? = null,
    val phone: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String? = null
)

// ✅ EVENTOS DEL BLOG (actualizado)
@Serializable
data class EventInstituteBlog(
    val id: Int,
    val title: String,
    val shortDescription: String = "",
    val longDescription: String = "",
    val location: String = "",
    val startDate: String? = null, // Formato ISO: "2025-11-28"
    val endDate: String? = null,
    val category: String = "INSTITUTIONAL", // "INSTITUTIONAL", "CAREER", "DEPARTMENT"
    val imagePath: String = "",
    val organizationId: Int, // ✅ CAMBIO: instituteId -> organizationId
    val channelId: Int? = null, // ✅ NUEVO: Para eventos específicos de canal
    val items: List<EventItemBlog> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isActive: Boolean = true
)

// ✅ ITEM DE EVENTO (sin cambios)
@Serializable
data class EventItemBlog(
    val id: Int,
    val type: EventItemType,
    val title: String,
    val value: String,
    val isClickable: Boolean = false,
    val iconName: String? = null
)

// ✅ USUARIO (sin cambios)
@Serializable
data class User(
    val id: Int,
    val googleId: String,
    val email: String,
    val name: String,
    val profilePicture: String? = null,
    val role: UserRole = UserRole.STUDENT,
    val isActive: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val syncEnabled: Boolean = false,
    val createdAt: String,
    val lastLoginAt: String? = null,
    val lastSyncAt: String? = null,
    val organizationId: Int? = null
)

// ✅ SUSCRIPCIÓN DE USUARIO (sin cambios)
@Serializable
data class UserSubscription(
    val id: Int,
    val userId: Int,
    val channelId: Int,
    val channelName: String,
    val organizationName: String, // ✅ CAMBIO: instituteName -> organizationName
    val subscribedAt: String,
    val isActive: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val syncedAt: String? = null
)

// ✅ RESPUESTA DE ORGANIZACIONES (antes InstituteSearchResponse)
@Serializable
data class OrganizationSearchResponse(
    val organizations: List<Organization>, // ✅ CAMBIO: institutes -> organizations
    val total: Int
)

// ✅ RESPUESTA DE CANALES
@Serializable
data class ChannelsResponse(
    val channels: List<Channel>,
    val total: Int,
    val organizationId: Int? = null // ✅ CAMBIO: instituteId -> organizationId
)

// ✅ RESPUESTA DE EVENTOS DEL BLOG (actualizada)
@Serializable
data class BlogEventsResponse(
    val events: List<EventInstituteBlog>,
    val total: Int,
    val organizationInfo: Organization? = null // ✅ CAMBIO: instituteInfo -> organizationInfo
)

// ✅ RESPUESTA DE SUSCRIPCIONES
@Serializable
data class UserSubscriptionsResponse(
    val subscriptions: List<UserSubscription>,
    val total: Int,
    val userId: Int
)
@Serializable
data class UpdateOrganizationRequest(
    val name: String,
    val acronym: String,
    val description: String = "",
    val address: String,
    val email: String,
    val phone: String,
    val studentNumber: Int = 0,
    val teacherNumber: Int = 0,
    val logoUrl: String? = null,
    val webSite: String? = null,
    val facebook: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val youtube: String? = null,
    val linkedin: String? = null
)
// ============== RESPUESTAS GENÉRICAS ==============

@Serializable
data class EventsListResponse(
    val events: List<EventInstituteBlog>,
    val total: Int
)


@Serializable
data class SuccessResponse(
    val success: Boolean,
    val message: String,
    val data: String? = null
)

@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class MessageResponse(
    val message: String
)
// log con firebase

@Serializable
data class LoginRequest(
    val idToken: String,
    val email: String = "",
    val clientType: String = "DESKTOP_ADMIN"
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val user: UserResponse? = null,
    val organization: OrganizationResponse? = null,
    val requiresOrganizationSetup: Boolean = false,
    val message: String? = null
)

@Serializable
data class UserResponse(
    val id: Int,
    val firebaseUid: String,
    val email: String,
    val name: String,
    val profilePicture: String? = null,
    val role: String,
    val organizationId: Int? = null,
    val createdAt: String,
    val lastLoginAt: String? = null
)

@Serializable
data class OrganizationResponse(
    val id: Int,
    val acronym: String,
    val name: String,
    val description: String,
    val email: String,
    val phone: String,
    val address: String,
    val logoUrl: String? = null,
    val website: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class CreateOrganizationRequest(
    val name: String,
    val acronym: String,
    val description: String = "",
    val email: String,
    val phone: String = "",
    val address: String = ""
)


// AGREGAR estos modelos al final del archivo:

@Serializable
data class OrganizationSetupRequest(
    val name: String,
    val acronym: String,
    val description: String = "",
    val address: String,
    val email: String,
    val phone: String,
    val studentNumber: Int = 0,
    val teacherNumber: Int = 0,
    val webSite: String? = null,
    val facebook: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val youtube: String? = null,
    val linkedin: String? = null
)

@Serializable
data class OrganizationSetupResponse(
    val success: Boolean,
    val message: String,
    val organizationId: Int,
    val name: String,
    val acronym: String
)

@Serializable
data class CreateEventRequest(
    val title: String,
    val shortDescription: String = "",
    val longDescription: String = "",
    val location: String = "",
    val startDate: String? = null, // "2025-07-15"
    val endDate: String? = null,
    val category: String = "INSTITUTIONAL",
    val imagePath: String = "",
    val organizationId: Int,
    val channelId: Int? = null,
    val items: List<CreateEventItemRequest> = emptyList()
)

@Serializable
data class CreateEventItemRequest(
    val type: String, // "SCHEDULE", "PHONE", etc.
    val title: String,
    val value: String,
    val isClickable: Boolean = false,
    val iconName: String? = null,
    val sortOrder: Int = 0
)

@Serializable
data class UpdateEventRequest(
    val title: String,
    val shortDescription: String = "",
    val longDescription: String = "",
    val location: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val category: String = "INSTITUTIONAL",
    val imagePath: String = "",
    val channelId: Int? = null,
    val items: List<CreateEventItemRequest> = emptyList()
)

@Serializable
data class CreateEventResponse(
    val success: Boolean,
    val message: String,
    val eventId: Int,
    val event: EventInstituteBlog? = null
)

@Serializable
data class UpdateEventResponse(
    val success: Boolean,
    val message: String,
    val event: EventInstituteBlog? = null
)

@Serializable
data class DeleteEventResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class EventSearchResponse(
    val events: List<EventInstituteBlog>,
    val total: Int,
    val query: String
)

@Serializable
data class UpcomingEventsResponse(
    val events: List<EventInstituteBlog>,
    val total: Int,
    val description: String
)

@Serializable
data class EventStatsResponse(
    val totalEvents: Long,
    val eventsByCategory: Map<String, Long>,
    val lastUpdated: String
)

@Serializable
data class SearchEventsResult(
    val events: List<EventInstituteBlog>,
    val total: Long,
    val limit: Int,
    val offset: Int,
    val hasMore: Boolean
)

// ================================
// 📋 MODELOS DE RESPUESTA
// ================================

sealed class UploadResult {
    data class Success(
        val fileName: String,
        val originalName: String,
        val size: Long,
        val url: String,
        val path: String
    ) : UploadResult()

    data class Error(val message: String) : UploadResult()
}

@Serializable
data class ImageInfo(
    val fileName: String,
    val size: Long,
    val url: String,
    val lastModified: Long
)

@Serializable
data class UploadResponse(
    val success: Boolean,
    val message: String,
    val fileName: String? = null,
    val url: String? = null,
    val size: Long? = null
)

@Serializable
data class DeleteImageResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class ListImagesResponse(
    val images: List<ImageInfo>,
    val total: Int
)


@Serializable
data class CreateChannelRequest(
    val name: String,
    val acronym: String,
    val description: String = "",
    val type: String = "DEPARTMENT", // CAREER, DEPARTMENT, ADMINISTRATIVE
    val email: String? = null,
    val phone: String? = null,
    val organizationId: Int
)

@Serializable
data class UpdateChannelRequest(
    val name: String,
    val acronym: String,
    val description: String = "",
    val type: String = "DEPARTMENT",
    val email: String? = null,
    val phone: String? = null
)

@Serializable
data class CreateChannelResponse(
    val success: Boolean,
    val message: String,
    val channelId: Int,
    val channel: Channel? = null
)

@Serializable
data class UpdateChannelResponse(
    val success: Boolean,
    val message: String,
    val channel: Channel? = null
)

@Serializable
data class DeleteChannelResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class ChannelSubscriber(
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val subscribedAt: String,
    val notificationsEnabled: Boolean
)

@Serializable
data class ChannelSubscribersResponse(
    val channelId: Int,
    val subscribers: List<ChannelSubscriber>,
    val total: Int
)
// ============== CONFIGURACIÓN DE ROLES POR PLATAFORMA ==============
@Serializable
data class ChannelStatsResponse(
    val totalChannels: Long,
    val channelsByType: Map<String, Long>,
    val totalSubscriptions: Long,
    val organizationId: Int
)

@Serializable
data class ChannelTypeInfo(
    val id: String,
    val name: String,
    val description: String,
    val icon: String
)
object RoleAssignmentConfig {

    /**
     * Mapeo de plataforma cliente a rol predeterminado
     */
    private val CLIENT_TYPE_TO_ROLE = mapOf(
        ClientType.ANDROID_STUDENT to UserRole.STUDENT,
        ClientType.DESKTOP_ADMIN to UserRole.ADMIN,
        ClientType.WEB_ADMIN to UserRole.ADMIN,
        ClientType.UNKNOWN to UserRole.STUDENT  // Por defecto estudiante
    )

    /**
     * Determina el rol basado en el tipo de cliente
     */
    fun determineRoleByClient(clientType: ClientType, email: String): UserRole {
        // Priorizar rol por cliente sobre email
        val roleByClient = CLIENT_TYPE_TO_ROLE[clientType] ?: UserRole.STUDENT

        // Validación adicional por email para seguridad
        val roleByEmail = determineRoleByEmail(email)

        return when {
            // Si viene de app móvil, siempre estudiante
            clientType == ClientType.ANDROID_STUDENT -> UserRole.STUDENT

            // Si viene de desktop/web admin, verificar email
            clientType in listOf(ClientType.DESKTOP_ADMIN, ClientType.WEB_ADMIN) -> {
                if (isValidAdminEmail(email)) {
                    UserRole.ADMIN
                } else {
                    throw IllegalArgumentException(
                        "Email $email no autorizado para acceso administrativo. " +
                                "Debe ser un email institucional (@tecnm.mx, @admin.tecnm.mx)"
                    )
                }
            }

            // Por defecto, basarse en email
            else -> roleByEmail
        }
    }

    /**
     * Determina rol basado en el email (método anterior como respaldo)
     */
    private fun determineRoleByEmail(email: String): UserRole {
        return when {
            email.endsWith("@admin.tecnm.mx") ||
                    email.endsWith("@director.tecnm.mx") -> UserRole.SUPER_ADMIN

            email.endsWith("@tecnm.mx") -> UserRole.ADMIN

            email.endsWith("@estudiante.tecnm.mx") ||
                    email.contains("estudiante") -> UserRole.STUDENT

            else -> UserRole.STUDENT  // Por defecto
        }
    }

    /**
     * Valida si un email puede tener permisos de administrador
     */
    private fun isValidAdminEmail(email: String): Boolean {
        val adminDomains = listOf(
            "@tecnm.mx",
            "@admin.tecnm.mx",
            "@director.tecnm.mx",
            "@puebla.tecnm.mx",
            "@tijuana.tecnm.mx"
            // Agregar más dominios institucionales según necesites
        )

        return adminDomains.any { domain -> email.endsWith(domain) }
    }

    /**
     * Obtiene información sobre los requisitos del rol
     */
    fun getRoleRequirements(role: UserRole): Map<String, Any> {
        return when (role) {
            UserRole.STUDENT -> mapOf(
                "requires_organization" to false,
                "can_create_events" to false,
                "can_manage_channels" to false,
                "description" to "Estudiante - puede suscribirse a canales y ver eventos"
            )

            UserRole.ADMIN -> mapOf(
                "requires_organization" to true,
                "can_create_events" to true,
                "can_manage_channels" to true,
                "description" to "Administrador - puede gestionar eventos de su organización"
            )

            UserRole.SUPER_ADMIN -> mapOf(
                "requires_organization" to false,
                "can_create_events" to true,
                "can_manage_channels" to true,
                "can_manage_organizations" to true,
                "description" to "Super Administrador - acceso completo al sistema"
            )
        }
    }
}

// ============== UTILIDADES ==============

object EventItemHelper {
    fun createScheduleItem(schedule: String): EventItemBlog {
        return EventItemBlog(
            id = 0,
            type = EventItemType.SCHEDULE,
            title = "Horario",
            value = schedule,
            isClickable = false,
            iconName = "schedule"
        )
    }

    fun createAttachmentItem(filename: String, url: String): EventItemBlog {
        return EventItemBlog(
            id = 0,
            type = EventItemType.ATTACHMENT,
            title = filename,
            value = url,
            isClickable = true,
            iconName = "attachment"
        )
    }

    fun createRegistrationLinkItem(url: String): EventItemBlog {
        return EventItemBlog(
            id = 0,
            type = EventItemType.REGISTRATION_LINK,
            title = "Registro",
            value = url,
            isClickable = true,
            iconName = "link"
        )
    }

    fun createPhoneItem(phone: String): EventItemBlog {
        return EventItemBlog(
            id = 0,
            type = EventItemType.PHONE,
            title = "Contacto",
            value = phone,
            isClickable = true,
            iconName = "phone"
        )
    }

    fun createEmailItem(email: String): EventItemBlog {
        return EventItemBlog(
            id = 0,
            type = EventItemType.EMAIL,
            title = "Email",
            value = email,
            isClickable = true,
            iconName = "email"
        )
    }

    fun createWebsiteItem(url: String): EventItemBlog {
        return EventItemBlog(
            id = 0,
            type = EventItemType.WEBSITE,
            title = "Más información",
            value = url,
            isClickable = true,
            iconName = "web"
        )
    }

    fun createMapsItem(location: String, mapsUrl: String): EventItemBlog {
        return EventItemBlog(
            id = 0,
            type = EventItemType.MAPS_LINK,
            title = "Ver en Maps",
            value = mapsUrl,
            isClickable = true,
            iconName = "location"
        )
    }

    fun createWhatsAppItem(phone: String): EventItemBlog {
        val whatsappUrl = "https://wa.me/${phone.replace("+", "").replace(" ", "")}"
        return EventItemBlog(
            id = 0,
            type = EventItemType.WHATSAPP,
            title = "WhatsApp",
            value = whatsappUrl,
            isClickable = true,
            iconName = "whatsapp"
        )
    }

    fun createSocialMediaItem(type: EventItemType, url: String): EventItemBlog {
        val (title, icon) = when (type) {
            EventItemType.FACEBOOK -> "Facebook" to "facebook"
            EventItemType.INSTAGRAM -> "Instagram" to "instagram"
            EventItemType.TWITTER -> "Twitter" to "twitter"
            EventItemType.YOUTUBE -> "YouTube" to "youtube"
            EventItemType.LINKEDIN -> "LinkedIn" to "linkedin"
            else -> "Red Social" to "web"
        }

        return EventItemBlog(
            id = 0,
            type = type,
            title = title,
            value = url,
            isClickable = true,
            iconName = icon
        )
    }
}

// Utilidad para formatear fechas
object DateUtils {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun formatDate(date: LocalDate?): String? {
        return date?.format(formatter)
    }

    fun parseDate(dateString: String?): LocalDate? {
        return try {
            dateString?.let { LocalDate.parse(it, formatter) }
        } catch (e: Exception) {
            null
        }
    }
}