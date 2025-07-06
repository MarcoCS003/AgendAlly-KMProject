package services

import com.example.*
import database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChannelsService {

    /**
     * Obtener todos los canales activos
     */
    fun getAllChannels(): ChannelsResponse = transaction {
        val channels = Channels
            .join(Organizations, JoinType.INNER, Channels.organizationId, Organizations.id)
            .select {
                (Channels.isActive eq true) and (Organizations.isActive eq true)
            }
            .orderBy(Organizations.name to SortOrder.ASC, Channels.name to SortOrder.ASC)
            .map { mapRowToChannel(it) }

        ChannelsResponse(
            channels = channels,
            total = channels.size
        )
    }

    /**
     * Obtener canales por organización
     */
    fun getChannelsByOrganization(organizationId: Int): ChannelsResponse = transaction {
        val channels = Channels
            .join(Organizations, JoinType.INNER, Channels.organizationId, Organizations.id)
            .select {
                (Channels.organizationId eq organizationId) and
                        (Channels.isActive eq true) and
                        (Organizations.isActive eq true)
            }
            .orderBy(Channels.type to SortOrder.ASC, Channels.name to SortOrder.ASC)
            .map { mapRowToChannel(it) }

        ChannelsResponse(
            channels = channels,
            total = channels.size,
            organizationId = organizationId
        )
    }

    /**
     * Obtener canales por tipo
     */
    fun getChannelsByType(type: ChannelType, organizationId: Int? = null): ChannelsResponse = transaction {
        val query = Channels
            .join(Organizations, JoinType.INNER, Channels.organizationId, Organizations.id)
            .select {
                (Channels.type eq type.name) and
                        (Channels.isActive eq true) and
                        (Organizations.isActive eq true)
            }

        val finalQuery = if (organizationId != null) {
            query.andWhere { Channels.organizationId eq organizationId }
        } else {
            query
        }

        val channels = finalQuery
            .orderBy(Organizations.name to SortOrder.ASC, Channels.name to SortOrder.ASC)
            .map { mapRowToChannel(it) }

        ChannelsResponse(
            channels = channels,
            total = channels.size,
            organizationId = organizationId
        )
    }

    /**
     * Obtener canal por ID
     */
    fun getChannelById(channelId: Int): Channel? = transaction {
        Channels
            .join(Organizations, JoinType.INNER, Channels.organizationId, Organizations.id)
            .select {
                (Channels.id eq channelId) and
                        (Channels.isActive eq true) and
                        (Organizations.isActive eq true)
            }
            .singleOrNull()?.let { mapRowToChannel(it) }
    }

    /**
     * Buscar canales por nombre o acrónimo
     */
    fun searchChannels(query: String, organizationId: Int? = null): ChannelsResponse = transaction {
        val searchTerm = "%${query.lowercase()}%"

        val baseQuery = Channels
            .join(Organizations, JoinType.INNER, Channels.organizationId, Organizations.id)
            .select {
                ((Channels.name.lowerCase() like searchTerm) or
                        (Channels.acronym.lowerCase() like searchTerm)) and
                        (Channels.isActive eq true) and
                        (Organizations.isActive eq true)
            }

        val finalQuery = if (organizationId != null) {
            baseQuery.andWhere { Channels.organizationId eq organizationId }
        } else {
            baseQuery
        }

        val channels = finalQuery
            .orderBy(Organizations.name to SortOrder.ASC, Channels.name to SortOrder.ASC)
            .map { mapRowToChannel(it) }

        ChannelsResponse(
            channels = channels,
            total = channels.size,
            organizationId = organizationId
        )
    }

    /**
     * Mapear row de BD a modelo Channel
     */
    private fun mapRowToChannel(row: ResultRow): Channel {
        return Channel(
            id = row[Channels.id],
            organizationId = row[Channels.organizationId],
            organizationName = row[Organizations.name],
            name = row[Channels.name],
            acronym = row[Channels.acronym],
            description = row[Channels.description],
            type = ChannelType.valueOf(row[Channels.type]), // ✅ CORREGIDO
            email = row[Channels.email],
            phone = row[Channels.phone],
            isActive = row[Channels.isActive],
            createdAt = row[Channels.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            updatedAt = row[Channels.updatedAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
}
