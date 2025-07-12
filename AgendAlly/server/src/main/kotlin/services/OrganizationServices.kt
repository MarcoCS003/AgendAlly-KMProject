package services

import com.example.*
import database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OrganizationService {

    /**
     * Obtener todas las organizaciones activas
     */
    fun getAllOrganizations(): List<Organization> = transaction {
        Organizations.select { Organizations.isActive eq true }
            .orderBy(Organizations.name to SortOrder.ASC)
            .map { mapRowToOrganization(it) }
    }

    /**
     * Buscar organizaciones por nombre o acrónimo
     */
    fun searchOrganizations(query: String): OrganizationSearchResponse = transaction {
        val searchTerm = "%${query.lowercase()}%"

        val organizations = Organizations.select {
            (Organizations.isActive eq true) and
                    ((Organizations.name.lowerCase() like searchTerm) or
                            (Organizations.acronym.lowerCase() like searchTerm))
        }.orderBy(Organizations.name to SortOrder.ASC)
            .map { mapRowToOrganization(it) }

        OrganizationSearchResponse(
            organizations = organizations,
            total = organizations.size
        )
    }

    /**
     * Obtener organización por ID
     */
    fun getOrganizationById(id: Int): Organization? = transaction {
        Organizations.select {
            (Organizations.id eq id) and (Organizations.isActive eq true)
        }.singleOrNull()?.let { mapRowToOrganization(it) }
    }

    /**
     * Obtener canales de una organización
     */
    fun getChannelsByOrganization(organizationId: Int): List<Channel> = transaction {
        Channels.join(Organizations, JoinType.INNER, Channels.organizationId, Organizations.id)
            .select {
                (Channels.organizationId eq organizationId) and
                        (Channels.isActive eq true) and
                        (Organizations.isActive eq true)
            }
            .orderBy(Channels.type to SortOrder.ASC, Channels.name to SortOrder.ASC)
            .map { mapRowToChannel(it) }
    }

    fun updateOrganization(organizationId: Int, updateRequest: UpdateOrganizationRequest): Organization? {
        return transaction {
            // Actualizar en la base de datos
            val updateCount = Organizations.update({ Organizations.id eq organizationId }) {
                it[name] = updateRequest.name
                it[acronym] = updateRequest.acronym
                it[description] = updateRequest.description
                it[address] = updateRequest.address
                it[email] = updateRequest.email
                it[phone] = updateRequest.phone
                it[studentNumber] = updateRequest.studentNumber
                it[teacherNumber] = updateRequest.teacherNumber
                it[logoUrl] = updateRequest.logoUrl
                it[webSite] = updateRequest.webSite
                it[facebook] = updateRequest.facebook
                it[instagram] = updateRequest.instagram
                it[twitter] = updateRequest.twitter
                it[youtube] = updateRequest.youtube
                it[linkedin] = updateRequest.linkedin
                it[updatedAt] = LocalDateTime.now()
            }

            if (updateCount > 0) {
                // Retornar la organización actualizada
                getOrganizationById(organizationId)
            } else {
                null
            }
        }
    }
    /**
     * Obtener estadísticas de organizaciones
     */
    fun getOrganizationStats(): Map<String, Any> = transaction {
        val totalOrganizations = Organizations.select { Organizations.isActive eq true }.count()

        val totalStudents = Organizations
            .slice(Organizations.studentNumber.sum())
            .select { Organizations.isActive eq true }
            .map { it[Organizations.studentNumber.sum()] }
            .firstOrNull() ?: 0

        val totalTeachers = Organizations
            .slice(Organizations.teacherNumber.sum())
            .select { Organizations.isActive eq true }
            .map { it[Organizations.teacherNumber.sum()] }
            .firstOrNull() ?: 0

        val totalChannels = Channels.select { Channels.isActive eq true }.count()

        mapOf(
            "total_organizations" to totalOrganizations,
            "total_students" to totalStudents,
            "total_teachers" to totalTeachers,
            "total_channels" to totalChannels,
            "updated_at" to System.currentTimeMillis()
        )
    }

    /**
     * Mapear row de BD a modelo Organization
     */
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
                email = channelRow[Channels.email],
                phone = channelRow[Channels.phone],
                isActive = channelRow[Channels.isActive],
                createdAt = channelRow[Channels.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                updatedAt = channelRow[Channels.updatedAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
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
            createdAt = row[Organizations.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            updatedAt = row[Organizations.updatedAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }

    /**
     * Mapear row de BD a modelo Channel (método auxiliar)
     */
    private fun mapRowToChannel(row: ResultRow): Channel {
        return Channel(
            id = row[Channels.id],
            organizationId = row[Channels.organizationId],
            organizationName = row[Organizations.name],
            name = row[Channels.name],
            acronym = row[Channels.acronym],
            description = row[Channels.description],
            email = row[Channels.email],
            phone = row[Channels.phone],
            isActive = row[Channels.isActive],
            createdAt = row[Channels.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            updatedAt = row[Channels.updatedAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
}
