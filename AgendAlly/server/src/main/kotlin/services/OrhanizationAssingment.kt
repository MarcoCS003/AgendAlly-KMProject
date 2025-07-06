package services

import com.example.*
import database.Organizations
import database.UserSubscriptions
import database.Channels
import database.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * ✅ SISTEMA SIMPLE: UN EMAIL = UNA ORGANIZACIÓN
 *
 * Para lanzar rápido, mapeamos emails a organizaciones de forma simple
 * Más adelante se puede expandir a múltiples organizaciones por usuario
 */
class OrganizationAssignmentService {

    /**
     * Mapeo simple de dominios de email a organizaciones
     * TODO: Mover esto a base de datos en versión futura
     */
    private val emailToOrganizationMap = mapOf(
        // ITP - Instituto Tecnológico de Puebla
        "@puebla.tecnm.mx" to "ITP",
        "@itp.mx" to "ITP",

        // ITT - Instituto Tecnológico de Tijuana
        "@tijuana.tecnm.mx" to "ITT",
        "@itt.mx" to "ITT",

        // Emails genéricos para desarrollo
        "@gmail.com" to "ITP",  // Por defecto para desarrollo
        "@outlook.com" to "ITP",
        "@hotmail.com" to "ITP"
    )

    /**
     * Determina la organización de un usuario basado en su email
     */
    fun getOrganizationByEmail(email: String): Organization? {
        return transaction {
            // 1. Buscar por dominio exacto
            val domain = "@" + email.split("@").getOrNull(1)
            val orgAcronym = emailToOrganizationMap[domain]

            if (orgAcronym != null) {
                // Buscar organización por acrónimo
                Organizations.select {
                    (Organizations.acronym eq orgAcronym) and (Organizations.isActive eq true)
                }.singleOrNull()?.let { row ->
                    mapRowToOrganization(row)
                }
            } else {
                // Si no hay mapeo específico, asignar a ITP por defecto (para desarrollo)
                getDefaultOrganization()
            }
        }
    }

    /**
     * Obtiene organización por defecto (ITP)
     */
    private fun getDefaultOrganization(): Organization? {
        return transaction {
            Organizations.select {
                (Organizations.acronym eq "ITP") and (Organizations.isActive eq true)
            }.singleOrNull()?.let { row ->
                mapRowToOrganization(row)
            }
        }
    }

    /**
     * Asigna usuario a organización y lo suscribe a canales básicos
     */
    fun assignUserToOrganization(userId: Int, email: String): OrganizationAssignmentResult {
        return transaction {
            val organization = getOrganizationByEmail(email)

            if (organization == null) {
                return@transaction OrganizationAssignmentResult(
                    success = false,
                    message = "No se encontró organización para el email $email",
                    organization = null
                )
            }

            // Obtener usuario
            val user = Users.select { Users.id eq userId }.singleOrNull()
            if (user == null) {
                return@transaction OrganizationAssignmentResult(
                    success = false,
                    message = "Usuario no encontrado",
                    organization = null
                )
            }

            val userRole = UserRole.valueOf(user[Users.role])

            // Auto-suscribir según el rol
            val subscriptions = when (userRole) {
                UserRole.STUDENT -> autoSubscribeStudent(userId, organization.organizationID)
                UserRole.ADMIN -> autoSubscribeAdmin(userId, organization.organizationID)
                UserRole.SUPER_ADMIN -> emptyList() // Super admin no necesita suscripciones automáticas
            }

            OrganizationAssignmentResult(
                success = true,
                message = "Usuario asignado a ${organization.name}",
                organization = organization,
                subscriptionsCreated = subscriptions.size
            )
        }
    }

    /**
     * Auto-suscribe estudiante a canales básicos
     */
    private fun autoSubscribeStudent(userId: Int, organizationId: Int): List<Int> {
        val subscriptions = mutableListOf<Int>()

        // Obtener canales administrativos básicos para estudiantes
        val basicChannels = Channels.select {
            (Channels.organizationId eq organizationId) and
                    (Channels.type eq "ADMINISTRATIVE") and
                    (Channels.isActive eq true)
        }.limit(3) // Solo 3 canales básicos

        basicChannels.forEach { channel ->
            try {
                val subscriptionId = UserSubscriptions.insert {
                    it[this.userId] = userId
                    it[channelId] = channel[Channels.id]
                    it[isActive] = true
                    it[notificationsEnabled] = true
                    it[subscribedAt] = LocalDateTime.now()
                } get UserSubscriptions.id

                subscriptions.add(subscriptionId)
                println("✅ Estudiante $userId suscrito a canal ${channel[Channels.name]}")
            } catch (e: Exception) {
                println("⚠️ Error suscribiendo a canal ${channel[Channels.id]}: ${e.message}")
            }
        }

        return subscriptions
    }

    /**
     * Auto-suscribe admin a todos los canales de su organización
     */
    private fun autoSubscribeAdmin(userId: Int, organizationId: Int): List<Int> {
        val subscriptions = mutableListOf<Int>()

        // Suscribir a TODOS los canales de la organización
        val allChannels = Channels.select {
            (Channels.organizationId eq organizationId) and (Channels.isActive eq true)
        }

        allChannels.forEach { channel ->
            try {
                val subscriptionId = UserSubscriptions.insert {
                    it[this.userId] = userId
                    it[channelId] = channel[Channels.id]
                    it[isActive] = true
                    it[notificationsEnabled] = true
                    it[subscribedAt] = LocalDateTime.now()
                } get UserSubscriptions.id

                subscriptions.add(subscriptionId)
                println("✅ Admin $userId suscrito a canal ${channel[Channels.name]}")
            } catch (e: Exception) {
                println("⚠️ Error suscribiendo admin a canal ${channel[Channels.id]}: ${e.message}")
            }
        }

        return subscriptions
    }

    /**
     * Lista todas las organizaciones disponibles
     */
    fun getAllAvailableOrganizations(): List<Organization> {
        return transaction {
            Organizations.select { Organizations.isActive eq true }
                .map { row -> mapRowToOrganization(row) }
        }
    }

    /**
     * Busca organizaciones por nombre o acrónimo (para futuro selector manual)
     */
    fun searchOrganizations(query: String): List<Organization> {
        return transaction {
            val searchTerm = "%${query.lowercase()}%"
            Organizations.select {
                (Organizations.isActive eq true) and
                        ((Organizations.name.lowerCase() like searchTerm) or
                                (Organizations.acronym.lowerCase() like searchTerm))
            }.map { row -> mapRowToOrganization(row) }
        }
    }

    private fun mapRowToOrganization(row: ResultRow): Organization {
        val organizationId = row[Organizations.id]

        // Obtener canales de la organización
        val channels = Channels.select {
            (Channels.organizationId eq organizationId) and (Channels.isActive eq true)
        }.map { channelRow ->
            Channel(
                id = channelRow[Channels.id],
                organizationId = channelRow[Channels.organizationId],
                organizationName = row[Organizations.name],
                name = channelRow[Channels.name],
                acronym = channelRow[Channels.acronym],
                description = channelRow[Channels.description],
                type = ChannelType.valueOf(channelRow[Channels.type]),
                email = channelRow[Channels.email],
                phone = channelRow[Channels.phone],
                isActive = channelRow[Channels.isActive],
                createdAt = channelRow[Channels.createdAt].toString(),
                updatedAt = channelRow[Channels.updatedAt].toString()
            )
        }

        return Organization(
            organizationID = row[Organizations.id],
            acronym = row[Organizations.acronym],
            name = row[Organizations.name],
            description = row[Organizations.description],
            address = row[Organizations.address],
            email = row[Organizations.email],
            phone = row[Organizations.phone],
            studentNumber = row[Organizations.studentNumber],
            teacherNumber = row[Organizations.teacherNumber],
            logoUrl = row[Organizations.logoUrl],
            webSite = row[Organizations.webSite],
            facebook = row[Organizations.facebook],
            instagram = row[Organizations.instagram],
            twitter = row[Organizations.twitter],
            youtube = row[Organizations.youtube],
            linkedin = row[Organizations.linkedin],
            channels = channels,
            isActive = row[Organizations.isActive],
            createdAt = row[Organizations.createdAt].toString(),
            updatedAt = row[Organizations.updatedAt]?.toString()
        )
    }
}

/**
 * Resultado de asignación de organización
 */
data class OrganizationAssignmentResult(
    val success: Boolean,
    val message: String,
    val organization: Organization?,
    val subscriptionsCreated: Int = 0
)