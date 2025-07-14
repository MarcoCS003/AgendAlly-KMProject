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
    fun createChannel(request: CreateChannelRequest): Int? = transaction {
        try {
            println("📝 Creando canal: ${request.name}")

            // Verificar que no exista el acrónimo en la organización
            val existingChannel = Channels.select {
                (Channels.organizationId eq request.organizationId) and
                        (Channels.acronym eq request.acronym.uppercase()) and
                        (Channels.isActive eq true)
            }.singleOrNull()

            if (existingChannel != null) {
                println("❌ Ya existe un canal con acrónimo: ${request.acronym}")
                return@transaction null
            }

            val channelId = Channels.insert {
                it[name] = request.name
                it[acronym] = request.acronym.uppercase()
                it[description] = request.description
                it[type] = request.type
                it[email] = request.email
                it[phone] = request.phone
                it[organizationId] = request.organizationId
                it[isActive] = true
                it[createdAt] = LocalDateTime.now()
            } get Channels.id

            println("✅ Canal creado con ID: $channelId")
            channelId

        } catch (e: Exception) {
            println("❌ Error creando canal: ${e.message}")
            null
        }
    }

    /**
     * ✏️ ACTUALIZAR canal existente
     */
    fun updateChannel(channelId: Int, request: UpdateChannelRequest): Boolean = transaction {
        try {
            println("✏️ Actualizando canal ID: $channelId")

            // Verificar que no exista otro canal con el mismo acrónimo
            val existingChannel = Channels.select {
                (Channels.id neq channelId) and
                        (Channels.acronym eq request.acronym.uppercase()) and
                        (Channels.isActive eq true)
            }.singleOrNull()

            if (existingChannel != null) {
                println("❌ Ya existe otro canal con acrónimo: ${request.acronym}")
                return@transaction false
            }

            val updateCount = Channels.update({ Channels.id eq channelId }) {
                it[name] = request.name
                it[acronym] = request.acronym.uppercase()
                it[description] = request.description
                it[type] = request.type
                it[email] = request.email
                it[phone] = request.phone
                it[updatedAt] = LocalDateTime.now()
            }

            if (updateCount > 0) {
                println("✅ Canal actualizado exitosamente")
                true
            } else {
                println("❌ No se encontró canal para actualizar")
                false
            }

        } catch (e: Exception) {
            println("❌ Error actualizando canal: ${e.message}")
            false
        }
    }

    /**
     * 🗑️ ELIMINAR canal (soft delete)
     */
    fun deleteChannel(channelId: Int): Boolean = transaction {
        try {
            println("🗑️ Eliminando canal ID: $channelId")

            val updateCount = Channels.update({ Channels.id eq channelId }) {
                it[isActive] = false
                it[updatedAt] = LocalDateTime.now()
            }

            if (updateCount > 0) {
                println("✅ Canal marcado como inactivo")
                true
            } else {
                println("❌ No se encontró canal para eliminar")
                false
            }

        } catch (e: Exception) {
            println("❌ Error eliminando canal: ${e.message}")
            false
        }
    }

    /**
     * 👥 OBTENER suscriptores de un canal
     */
    fun getChannelSubscribers(channelId: Int): List<ChannelSubscriber> = transaction {
        UserSubscriptions
            .join(Users, JoinType.INNER, UserSubscriptions.userId, Users.id)
            .select {
                (UserSubscriptions.channelId eq channelId) and
                        (UserSubscriptions.isActive eq true) and
                        (Users.isActive eq true)
            }
            .orderBy(UserSubscriptions.subscribedAt to SortOrder.DESC)
            .map { row ->
                ChannelSubscriber(
                    userId = row[Users.id],
                    userName = row[Users.name],
                    userEmail = row[Users.email],
                    subscribedAt = row[UserSubscriptions.subscribedAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    notificationsEnabled = row[UserSubscriptions.notificationsEnabled]
                )
            }
    }

    /**
     * 🔍 VERIFICAR si un canal pertenece a una organización
     */
    fun isChannelFromOrganization(channelId: Int, organizationId: Int): Boolean = transaction {
        Channels.select {
            (Channels.id eq channelId) and
                    (Channels.organizationId eq organizationId) and
                    (Channels.isActive eq true)
        }.singleOrNull() != null
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
            email = row[Channels.email],
            phone = row[Channels.phone],
            isActive = row[Channels.isActive],
            createdAt = row[Channels.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            updatedAt = row[Channels.updatedAt]?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
    }
}
