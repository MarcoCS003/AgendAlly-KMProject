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

@Serializable
enum class ChannelType {
    CAREER,         // Canal de carrera (TICS, Industrial, etc.)
    DEPARTMENT,     // Departamento (Biblioteca, Centro Cómputo)
    ADMINISTRATIVE  // Administrativo (Servicios Escolares, etc.)
}

@Serializable
enum class EventType {
    PERSONAL,       // Evento creado por el estudiante
    SUBSCRIBED,     // Evento de canal suscrito
    HIDDEN         // Evento suscrito pero oculto por el estudiante
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
    val type: ChannelType,
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
    val lastSyncAt: String? = null
)

// ✅ SUSCRIPCIÓN DE USUARIO (sin cambios)
@Serializable
data class UserSubscription(
    val id: Int,
    val userId: Int,
    val channelId: Int,
    val channelName: String,
    val channelType: ChannelType,
    val organizationName: String, // ✅ CAMBIO: instituteName -> organizationName
    val subscribedAt: String,
    val isActive: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val syncedAt: String? = null
)

// ============== REQUESTS ==============

// ✅ AUTENTICACIÓN CON FIREBASE
@Serializable
data class GoogleAuthRequest(
    val idToken: String,
    val clientType: ClientType = ClientType.UNKNOWN,
    val organizationId: Int? = null  // Para admins: organización que van a administrar
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val user: User? = null,
    val token: String? = null,
    val expiresAt: String? = null,
    val message: String? = null,
    val assignedRole: String? = null,  // Información del rol asignado
    val requiresOrganization: Boolean = false  // Si necesita seleccionar organización
)

@Serializable
data class TokenValidationRequest(
    val token: String
)

@Serializable
data class AdminSetupRequest(
    val organizationId: Int
)

// ✅ REQUEST PARA CREAR EVENTO (actualizado)
@Serializable
data class CreateEventRequest(
    val title: String,
    val shortDescription: String = "",
    val longDescription: String = "",
    val location: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val category: String = "INSTITUTIONAL",
    val imagePath: String = "",
    val organizationId: Int, // ✅ CAMBIO: instituteId -> organizationId
    val channelId: Int? = null, // ✅ NUEVO: Para eventos específicos de canal
    val items: List<EventItemBlog> = emptyList()
)

// ✅ REQUEST PARA AGREGAR ORGANIZACIÓN (antes AddInstituteRequest)
@Serializable
data class AddOrganizationRequest(
    val organizationID: Int,
    val channelID: Int // ✅ CAMBIO: careerID -> channelID
)

// ✅ SUSCRIPCIONES (sin cambios)
@Serializable
data class SubscribeToChannelRequest(
    val channelId: Int,
    val notificationsEnabled: Boolean = true
)

@Serializable
data class UpdateSubscriptionRequest(
    val notificationsEnabled: Boolean
)

// ============== RESPUESTAS DEL API ==============

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

// ============== RESPUESTAS GENÉRICAS ==============

@Serializable
data class EventsListResponse(
    val events: List<EventInstituteBlog>,
    val total: Int
)

@Serializable
data class EventSearchResponse(
    val events: List<EventInstituteBlog>,
    val total: Int,
    val query: String
)

@Serializable
data class EventsByCategoryResponse(
    val events: List<EventInstituteBlog>,
    val total: Int,
    val category: String
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



// ============== CONFIGURACIÓN DE ROLES POR PLATAFORMA ==============

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